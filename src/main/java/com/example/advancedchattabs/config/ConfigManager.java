package com.example.advancedchattabs.config;

import com.example.advancedchattabs.window.WindowsConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class ConfigManager implements AutoCloseable {
    private final Logger logger;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final ConfigMigration migration = new ConfigMigration();
    private final Path directory;
    private final Path globalFile;
    private final Path serversFile;
    private final Path windowsFile;
    private final Path historiesDirectory;
    private final ScheduledExecutorService saver = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "advanced-chat-tabs-config");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> pendingSave;
    private GlobalConfig global = new GlobalConfig();
    private ServersConfig servers = new ServersConfig();
    private WindowsConfig windows = new WindowsConfig();

    public ConfigManager(Logger logger) {
        this.logger = logger;
        directory = FabricLoader.getInstance().getConfigDir().resolve("advanced_chat_tabs");
        globalFile = directory.resolve("global.json");
        serversFile = directory.resolve("servers.json");
        windowsFile = directory.resolve("windows.json");
        historiesDirectory = directory.resolve("histories");
    }

    public synchronized void load() {
        try {
            Files.createDirectories(historiesDirectory);
        } catch (IOException exception) {
            logger.error("Unable to create Advanced Chat Tabs configuration directory", exception);
        }
        global = migration.migrate(read(globalFile, GlobalConfig.class, new GlobalConfig()));
        servers = read(serversFile, ServersConfig.class, new ServersConfig());
        servers.getServers();
        windows = read(windowsFile, WindowsConfig.class, new WindowsConfig());
        windows.getWindows();
    }

    public synchronized ServerConfig server(String key, String displayName) {
        ServerConfig existing = servers.getServers().get(key);
        ServerConfig migrated = migration.migrate(existing, key, displayName);
        servers.getServers().put(key, migrated);
        return migrated;
    }

    public synchronized void requestSave() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        pendingSave = saver.schedule(this::saveNow, 750, TimeUnit.MILLISECONDS);
    }

    public synchronized void saveNow() {
        writeAtomic(globalFile, global);
        writeAtomic(serversFile, servers);
        writeAtomic(windowsFile, windows);
    }

    private <T> T read(Path file, Class<T> type, T fallback) {
        if (!Files.exists(file)) {
            return fallback;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            T value = gson.fromJson(reader, type);
            return value == null ? fallback : value;
        } catch (IOException | JsonParseException exception) {
            logger.warn("Failed to read {}; trying backup", file, exception);
            Path backup = backupOf(file);
            if (Files.exists(backup)) {
                try (Reader reader = Files.newBufferedReader(backup)) {
                    T value = gson.fromJson(reader, type);
                    return value == null ? fallback : value;
                } catch (IOException | JsonParseException backupException) {
                    logger.error("Backup configuration is also unreadable: {}", backup, backupException);
                }
            }
            return fallback;
        }
    }

    private void writeAtomic(Path file, Object value) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Path backup = backupOf(file);
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                gson.toJson(value, writer);
            }
            if (Files.exists(file)) {
                Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            logger.error("Failed to save {}", file, exception);
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
                logger.debug("Failed to remove temporary config {}", temporary);
            }
        }
    }

    private Path backupOf(Path file) {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    public GlobalConfig global() { return global; }
    public ServersConfig servers() { return servers; }
    public WindowsConfig windows() { return windows; }
    public Gson gson() { return gson; }
    public Path historiesDirectory() { return historiesDirectory; }

    @Override
    public synchronized void close() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
        }
        saveNow();
        saver.shutdown();
    }
}

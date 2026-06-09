package com.example.advancedchattabs.config;

import com.example.advancedchattabs.chat.ChatHistory;
import com.example.advancedchattabs.chat.ChatMessage;
import com.example.advancedchattabs.chat.ChatMessageType;
import com.example.advancedchattabs.chat.TrustStatus;
import com.example.advancedchattabs.chat.VanillaChatPayload;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.tab.TabSettings;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Persists the optional per-tab history as safe, bounded JSON snapshots. Network signatures are
 * deliberately not serialized. Restored entries are local visual history and therefore unsigned.
 */
public final class HistoryStore implements AutoCloseable {
    private static final int SCHEMA_VERSION = 1;

    private final Path root;
    private final Gson gson;
    private final Logger logger;
    private final ScheduledExecutorService writer = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "advanced-chat-tabs-history");
        thread.setDaemon(true);
        return thread;
    });
    private ScheduledFuture<?> pendingWrite;
    private PendingSnapshot pendingSnapshot;

    public HistoryStore(Path root, Gson gson, Logger logger) {
        this.root = root;
        this.gson = gson;
        this.logger = logger;
    }

    public void load(String serverKey, List<ChatTab> tabs, TabSettings globalSettings) {
        for (ChatTab tab : tabs) {
            tab.ensureRuntime();
            TabSettings settings = tab.resolvedSettings(globalSettings);
            if (!settings.isPersistHistory()) {
                continue;
            }
            Path file = fileFor(serverKey, tab.getId());
            HistoryFile data = read(file);
            if (data == null || data.entries == null) {
                continue;
            }
            tab.getHistory().clear();
            int limit = Math.max(0, settings.getMaximumPersistedHistory());
            int start = Math.max(0, data.entries.size() - limit);
            for (int index = start; index < data.entries.size(); index++) {
                PersistedEntry entry = data.entries.get(index);
                if (entry == null || entry.text == null) {
                    continue;
                }
                Instant received = instant(entry.receivedAtEpochMillis);
                ChatMessage<VanillaChatPayload> message = new ChatMessage<>(
                        entry.id == null ? UUID.randomUUID() : entry.id,
                        new VanillaChatPayload(Text.literal(entry.text), null, null),
                        entry.plainText == null ? entry.text : entry.plainText,
                        "",
                        entry.sender,
                        entry.type,
                        TrustStatus.UNSIGNED,
                        received
                );
                tab.getHistory().addEntry(new ChatHistory.Entry<>(
                        message,
                        Math.max(1, entry.repeatCount),
                        instant(entry.firstSeenEpochMillis),
                        instant(entry.lastSeenEpochMillis)
                ));
            }
        }
    }

    public synchronized void requestSave(String serverKey, List<ChatTab> tabs, TabSettings globalSettings) {
        pendingSnapshot = snapshot(serverKey, tabs, globalSettings);
        if (pendingWrite != null) {
            pendingWrite.cancel(false);
        }
        pendingWrite = writer.schedule(this::flushPending, 1500L, TimeUnit.MILLISECONDS);
    }

    public synchronized void saveNow(String serverKey, List<ChatTab> tabs, TabSettings globalSettings) {
        pendingSnapshot = snapshot(serverKey, tabs, globalSettings);
        if (pendingWrite != null) {
            pendingWrite.cancel(false);
            pendingWrite = null;
        }
        flushPending();
    }

    private synchronized void flushPending() {
        PendingSnapshot snapshot = pendingSnapshot;
        pendingSnapshot = null;
        if (snapshot == null) {
            return;
        }
        for (TabSnapshot tab : snapshot.tabs) {
            Path file = fileFor(snapshot.serverKey, tab.tabId);
            if (!tab.persist) {
                try {
                    Files.deleteIfExists(file);
                    Files.deleteIfExists(backupOf(file));
                } catch (IOException exception) {
                    logger.debug("Unable to remove disabled history {}", file, exception);
                }
                continue;
            }
            writeAtomic(file, tab.file);
        }
    }

    private PendingSnapshot snapshot(String serverKey, List<ChatTab> tabs, TabSettings globalSettings) {
        List<TabSnapshot> result = new ArrayList<>();
        for (ChatTab tab : tabs) {
            TabSettings settings = tab.resolvedSettings(globalSettings);
            boolean persist = settings.isPersistHistory();
            HistoryFile file = new HistoryFile();
            if (persist) {
                List<ChatHistory.Entry<VanillaChatPayload>> entries = tab.getHistory().snapshot();
                int limit = Math.max(0, settings.getMaximumPersistedHistory());
                int start = Math.max(0, entries.size() - limit);
                for (int index = start; index < entries.size(); index++) {
                    ChatHistory.Entry<VanillaChatPayload> entry = entries.get(index);
                    ChatMessage<VanillaChatPayload> message = entry.message();
                    PersistedEntry persisted = new PersistedEntry();
                    persisted.id = message.id();
                    persisted.text = message.richText().text().getString();
                    persisted.plainText = message.plainText();
                    persisted.sender = message.sender();
                    persisted.type = message.type();
                    persisted.receivedAtEpochMillis = message.receivedAt().toEpochMilli();
                    persisted.repeatCount = entry.repeatCount();
                    persisted.firstSeenEpochMillis = entry.firstSeen().toEpochMilli();
                    persisted.lastSeenEpochMillis = entry.lastSeen().toEpochMilli();
                    file.entries.add(persisted);
                }
            }
            result.add(new TabSnapshot(tab.getId(), persist, file));
        }
        return new PendingSnapshot(serverKey, result);
    }

    private HistoryFile read(Path file) {
        if (!Files.exists(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            return gson.fromJson(reader, HistoryFile.class);
        } catch (IOException | JsonParseException exception) {
            logger.warn("Failed to read chat history {}; trying backup", file, exception);
            Path backup = backupOf(file);
            if (Files.exists(backup)) {
                try (Reader reader = Files.newBufferedReader(backup)) {
                    return gson.fromJson(reader, HistoryFile.class);
                } catch (IOException | JsonParseException backupException) {
                    logger.error("Backup chat history is unreadable: {}", backup, backupException);
                }
            }
            return null;
        }
    }

    private void writeAtomic(Path file, HistoryFile value) {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        Path backup = backupOf(file);
        try {
            Files.createDirectories(file.getParent());
            try (Writer output = Files.newBufferedWriter(temporary)) {
                gson.toJson(value, output);
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
            logger.error("Failed to save chat history {}", file, exception);
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException cleanupException) {
                logger.debug("Unable to remove temporary history {}", temporary, cleanupException);
            }
        }
    }

    private Path fileFor(String serverKey, UUID tabId) {
        UUID serverId = UUID.nameUUIDFromBytes(serverKey.getBytes(StandardCharsets.UTF_8));
        return root.resolve(serverId.toString()).resolve(tabId + ".json");
    }

    private Path backupOf(Path file) {
        return file.resolveSibling(file.getFileName() + ".bak");
    }

    private Instant instant(long epochMillis) {
        return epochMillis <= 0L ? Instant.now() : Instant.ofEpochMilli(epochMillis);
    }

    @Override
    public synchronized void close() {
        if (pendingWrite != null) {
            pendingWrite.cancel(false);
        }
        flushPending();
        writer.shutdown();
    }

    private record PendingSnapshot(String serverKey, List<TabSnapshot> tabs) {}
    private record TabSnapshot(UUID tabId, boolean persist, HistoryFile file) {}

    private static final class HistoryFile {
        private int schemaVersion = SCHEMA_VERSION;
        private List<PersistedEntry> entries = new ArrayList<>();
    }

    private static final class PersistedEntry {
        private UUID id;
        private String text;
        private String plainText;
        private String sender;
        private ChatMessageType type = ChatMessageType.UNKNOWN;
        private long receivedAtEpochMillis;
        private int repeatCount = 1;
        private long firstSeenEpochMillis;
        private long lastSeenEpochMillis;
    }
}

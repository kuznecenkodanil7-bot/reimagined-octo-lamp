package com.example.advancedchattabs.config;

import com.example.advancedchattabs.tab.ChatTab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ServerConfig {
    private int schemaVersion = ConfigMigration.CURRENT_SCHEMA;
    private String serverKey = "disconnected";
    private UUID activeTabId;
    private List<ChatTab> tabs = new ArrayList<>();

    public static ServerConfig create(String serverKey, String displayName) {
        ServerConfig config = new ServerConfig();
        config.serverKey = serverKey;
        ChatTab main = new ChatTab(displayName, true, 0);
        config.tabs.add(main);
        config.activeTabId = main.getId();
        return config;
    }

    public void normalize(String displayName) {
        if (tabs == null) {
            tabs = new ArrayList<>();
        }
        tabs.removeIf(tab -> tab == null);
        ChatTab main = tabs.stream().filter(ChatTab::isSystemTab).findFirst().orElse(null);
        if (main == null) {
            main = new ChatTab(displayName, true, 0);
            tabs.add(main);
        }
        main.setName(displayName);
        tabs.sort(Comparator.comparing(ChatTab::isPinned).reversed().thenComparingInt(ChatTab::getOrder));
        for (int index = 0; index < tabs.size(); index++) {
            tabs.get(index).setOrder(index);
            tabs.get(index).ensureRuntime();
        }
        if (activeTabId == null || tabs.stream().noneMatch(tab -> tab.getId().equals(activeTabId))) {
            activeTabId = main.getId();
        }
    }

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int value) { schemaVersion = value; }
    public String getServerKey() { return serverKey; }
    public void setServerKey(String value) { serverKey = value; }
    public UUID getActiveTabId() { return activeTabId; }
    public void setActiveTabId(UUID value) { activeTabId = value; }
    public List<ChatTab> getTabs() { return tabs; }
}

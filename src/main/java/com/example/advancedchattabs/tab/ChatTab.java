package com.example.advancedchattabs.tab;

import com.example.advancedchattabs.chat.ChatHistory;
import com.example.advancedchattabs.filter.ChatFilter;
import net.minecraft.client.gui.hud.ChatHud;
import com.example.advancedchattabs.chat.VanillaChatPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatTab {
    private UUID id = UUID.randomUUID();
    private String name = "Tab";
    private List<ChatFilter> filters = new ArrayList<>();
    private TabSettings settings = new TabSettings();
    private boolean systemTab;
    private int unreadCount;
    private boolean pinned;
    private int order;
    private UUID windowId;
    private transient ChatHud.ChatState vanillaState;
    private transient ChatHistory<VanillaChatPayload> history = new ChatHistory<>(100);

    public ChatTab() {}

    public ChatTab(String name, boolean systemTab, int order) {
        this.name = name;
        this.systemTab = systemTab;
        this.order = order;
    }

    public ChatTab duplicate() {
        ChatTab copy = new ChatTab(name + " copy", false, order + 1);
        copy.settings = settings.copy();
        for (ChatFilter filter : filters) {
            copy.filters.add(filter.copy());
        }
        copy.pinned = pinned;
        return copy;
    }

    public void ensureRuntime() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (filters == null) {
            filters = new ArrayList<>();
        }
        filters.removeIf(filter -> filter == null);
        if (settings == null) {
            settings = new TabSettings();
        }
        if (history == null) {
            history = new ChatHistory<>(settings.getMaximumLines());
        }
    }

    public TabSettings resolvedSettings(TabSettings global) {
        ensureRuntime();
        return settings.isUseGlobalSettings() && global != null ? global : settings;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String value) { name = value == null || value.isBlank() ? "Tab" : value; }
    public List<ChatFilter> getFilters() { ensureRuntime(); return filters; }
    public TabSettings getSettings() { ensureRuntime(); return settings; }
    public boolean isSystemTab() { return systemTab; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int value) { unreadCount = Math.max(0, value); }
    public void incrementUnread() { unreadCount++; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean value) { pinned = value; }
    public int getOrder() { return order; }
    public void setOrder(int value) { order = value; }
    public UUID getWindowId() { return windowId; }
    public void setWindowId(UUID value) { windowId = value; }
    public ChatHud.ChatState getVanillaState() { return vanillaState; }
    public void setVanillaState(ChatHud.ChatState value) { vanillaState = value; }
    public ChatHistory<VanillaChatPayload> getHistory() { ensureRuntime(); return history; }
}

package com.example.advancedchattabs.window;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ChatWindow {
    private UUID id = UUID.randomUUID();
    private String name = "Chat window";
    private int x = 20;
    private int y = 20;
    private int width = 320;
    private int height = 140;
    private int minimumWidth = 160;
    private int minimumHeight = 80;
    private int maximumWidth = 1000;
    private int maximumHeight = 800;
    private float opacity = 0.65F;
    private boolean minimized;
    private boolean pinned;
    private boolean closed;
    private List<UUID> tabIds = new ArrayList<>();
    private UUID activeTabId;

    public ChatWindow() {}

    public ChatWindow(String name, UUID tabId, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        if (tabId != null) {
            tabIds.add(tabId);
            activeTabId = tabId;
        }
    }

    public void normalize() {
        if (id == null) id = UUID.randomUUID();
        if (name == null || name.isBlank()) name = "Chat window";
        if (tabIds == null) tabIds = new ArrayList<>();
        tabIds.removeIf(tabId -> tabId == null);
        if (activeTabId == null && !tabIds.isEmpty()) activeTabId = tabIds.getFirst();
        minimumWidth = Math.max(80, minimumWidth);
        minimumHeight = Math.max(40, minimumHeight);
        maximumWidth = Math.max(minimumWidth, maximumWidth);
        maximumHeight = Math.max(minimumHeight, maximumHeight);
        opacity = Math.max(0.05F, Math.min(1.0F, opacity));
    }

    public void clamp(int screenWidth, int screenHeight) {
        normalize();
        width = Math.max(minimumWidth, Math.min(Math.min(maximumWidth, screenWidth), width));
        height = Math.max(minimumHeight, Math.min(Math.min(maximumHeight, screenHeight), height));
        x = Math.max(0, Math.min(Math.max(0, screenWidth - 24), x));
        y = Math.max(0, Math.min(Math.max(0, screenHeight - 16), y));
        if (x + width < 24) x = 0;
        if (y + height < 16) y = 0;
    }

    public void snap(int screenWidth, int screenHeight, int threshold) {
        if (Math.abs(x) <= threshold) x = 0;
        if (Math.abs(y) <= threshold) y = 0;
        if (Math.abs((x + width) - screenWidth) <= threshold) x = screenWidth - width;
        if (Math.abs((y + height) - screenHeight) <= threshold) y = screenHeight - height;
        clamp(screenWidth, screenHeight);
    }

    public UUID getId() { normalize(); return id; }
    public String getName() { normalize(); return name; }
    public void setName(String value) { name = value == null || value.isBlank() ? "Chat window" : value; }
    public int getX() { return x; }
    public void setX(int value) { x = value; }
    public int getY() { return y; }
    public void setY(int value) { y = value; }
    public int getWidth() { return width; }
    public void setWidth(int value) { width = Math.max(minimumWidth, Math.min(maximumWidth, value)); }
    public int getHeight() { return height; }
    public void setHeight(int value) { height = Math.max(minimumHeight, Math.min(maximumHeight, value)); }
    public int getMinimumWidth() { return minimumWidth; }
    public void setMinimumWidth(int value) { minimumWidth = Math.max(80, value); }
    public int getMinimumHeight() { return minimumHeight; }
    public void setMinimumHeight(int value) { minimumHeight = Math.max(40, value); }
    public int getMaximumWidth() { return maximumWidth; }
    public void setMaximumWidth(int value) { maximumWidth = Math.max(minimumWidth, value); }
    public int getMaximumHeight() { return maximumHeight; }
    public void setMaximumHeight(int value) { maximumHeight = Math.max(minimumHeight, value); }
    public float getOpacity() { return opacity; }
    public void setOpacity(float value) { opacity = Math.max(0.05F, Math.min(1.0F, value)); }
    public boolean isMinimized() { return minimized; }
    public void setMinimized(boolean value) { minimized = value; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean value) { pinned = value; }
    public boolean isClosed() { return closed; }
    public void setClosed(boolean value) { closed = value; }
    public List<UUID> getTabIds() { normalize(); return tabIds; }
    public UUID getActiveTabId() { return activeTabId; }
    public void setActiveTabId(UUID value) { activeTabId = value; }
}

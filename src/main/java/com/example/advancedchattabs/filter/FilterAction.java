package com.example.advancedchattabs.filter;

import java.util.UUID;

public final class FilterAction {
    private FilterActionType type = FilterActionType.ADD_TO_CURRENT_TAB;
    private UUID targetTabId;
    private UUID targetWindowId;
    private int color = 0xFFFFFFFF;
    private float alpha = 0.5F;
    private String soundId = "minecraft:ui.button.click";
    private float volume = 1.0F;
    private float pitch = 1.0F;
    private long cooldownMillis = 1000L;
    private String icon = "!";
    private boolean globalHide;

    public FilterAction() {}

    public FilterAction(FilterActionType type) {
        this.type = type;
    }

    public static FilterAction addCurrent() { return new FilterAction(FilterActionType.ADD_TO_CURRENT_TAB); }
    public static FilterAction hide(boolean global) {
        FilterAction action = new FilterAction(FilterActionType.HIDE_MESSAGE);
        action.globalHide = global;
        return action;
    }

    public FilterActionType getType() { return type; }
    public void setType(FilterActionType type) { this.type = type; }
    public UUID getTargetTabId() { return targetTabId; }
    public void setTargetTabId(UUID targetTabId) { this.targetTabId = targetTabId; }
    public UUID getTargetWindowId() { return targetWindowId; }
    public void setTargetWindowId(UUID targetWindowId) { this.targetWindowId = targetWindowId; }
    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
    public float getAlpha() { return alpha; }
    public void setAlpha(float alpha) { this.alpha = Math.max(0.0F, Math.min(1.0F, alpha)); }
    public String getSoundId() { return soundId; }
    public void setSoundId(String soundId) { this.soundId = soundId; }
    public float getVolume() { return volume; }
    public void setVolume(float volume) { this.volume = Math.max(0.0F, volume); }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = Math.max(0.01F, pitch); }
    public long getCooldownMillis() { return cooldownMillis; }
    public void setCooldownMillis(long cooldownMillis) { this.cooldownMillis = Math.max(0L, cooldownMillis); }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public boolean isGlobalHide() { return globalHide; }
    public void setGlobalHide(boolean globalHide) { this.globalHide = globalHide; }
}

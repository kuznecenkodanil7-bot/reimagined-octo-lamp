package com.example.advancedchattabs.tab;

public final class TabSettings {
    private boolean useGlobalSettings = true;
    private int maximumLines = 100;
    private boolean mergeIdenticalMessages;
    private boolean protectFromClear = true;
    private boolean showTrustIndicators = true;
    private boolean textShadow = true;
    private boolean messageBackground = true;
    private float backgroundOpacity = 0.5F;
    private float textScale = 1.0F;
    private float lineSpacing = 0.0F;
    private int displayTimeSeconds = 10;
    private boolean smoothFade = true;
    private boolean showTimestamps;
    private String timestampFormat = "HH:mm";
    private int timestampColor = 0xFFAAAAAA;
    private boolean timestampParticipatesInFilters;
    private boolean showRepeatCounter = true;
    private long mergeIntervalMillis = 10_000L;
    private boolean clearOnServerChange;
    private boolean persistHistory;
    private int maximumPersistedHistory = 1000;

    public TabSettings copy() {
        TabSettings copy = new TabSettings();
        copy.useGlobalSettings = useGlobalSettings;
        copy.maximumLines = maximumLines;
        copy.mergeIdenticalMessages = mergeIdenticalMessages;
        copy.protectFromClear = protectFromClear;
        copy.showTrustIndicators = showTrustIndicators;
        copy.textShadow = textShadow;
        copy.messageBackground = messageBackground;
        copy.backgroundOpacity = backgroundOpacity;
        copy.textScale = textScale;
        copy.lineSpacing = lineSpacing;
        copy.displayTimeSeconds = displayTimeSeconds;
        copy.smoothFade = smoothFade;
        copy.showTimestamps = showTimestamps;
        copy.timestampFormat = timestampFormat;
        copy.timestampColor = timestampColor;
        copy.timestampParticipatesInFilters = timestampParticipatesInFilters;
        copy.showRepeatCounter = showRepeatCounter;
        copy.mergeIntervalMillis = mergeIntervalMillis;
        copy.clearOnServerChange = clearOnServerChange;
        copy.persistHistory = persistHistory;
        copy.maximumPersistedHistory = maximumPersistedHistory;
        return copy;
    }

    public void resetToDefaults() {
        TabSettings defaults = new TabSettings();
        useGlobalSettings = defaults.useGlobalSettings;
        maximumLines = defaults.maximumLines;
        mergeIdenticalMessages = defaults.mergeIdenticalMessages;
        protectFromClear = defaults.protectFromClear;
        showTrustIndicators = defaults.showTrustIndicators;
        textShadow = defaults.textShadow;
        messageBackground = defaults.messageBackground;
        backgroundOpacity = defaults.backgroundOpacity;
        textScale = defaults.textScale;
        lineSpacing = defaults.lineSpacing;
        displayTimeSeconds = defaults.displayTimeSeconds;
        smoothFade = defaults.smoothFade;
        showTimestamps = defaults.showTimestamps;
        timestampFormat = defaults.timestampFormat;
        timestampColor = defaults.timestampColor;
        timestampParticipatesInFilters = defaults.timestampParticipatesInFilters;
        showRepeatCounter = defaults.showRepeatCounter;
        mergeIntervalMillis = defaults.mergeIntervalMillis;
        clearOnServerChange = defaults.clearOnServerChange;
        persistHistory = defaults.persistHistory;
        maximumPersistedHistory = defaults.maximumPersistedHistory;
    }

    public boolean isUseGlobalSettings() { return useGlobalSettings; }
    public void setUseGlobalSettings(boolean value) { useGlobalSettings = value; }
    public int getMaximumLines() { return maximumLines; }
    public void setMaximumLines(int value) { maximumLines = Math.max(10, value); }
    public boolean isMergeIdenticalMessages() { return mergeIdenticalMessages; }
    public void setMergeIdenticalMessages(boolean value) { mergeIdenticalMessages = value; }
    public boolean isProtectFromClear() { return protectFromClear; }
    public void setProtectFromClear(boolean value) { protectFromClear = value; }
    public boolean isShowTrustIndicators() { return showTrustIndicators; }
    public void setShowTrustIndicators(boolean value) { showTrustIndicators = value; }
    public boolean isTextShadow() { return textShadow; }
    public void setTextShadow(boolean value) { textShadow = value; }
    public boolean isMessageBackground() { return messageBackground; }
    public void setMessageBackground(boolean value) { messageBackground = value; }
    public float getBackgroundOpacity() { return backgroundOpacity; }
    public void setBackgroundOpacity(float value) { backgroundOpacity = Math.max(0.0F, Math.min(1.0F, value)); }
    public float getTextScale() { return textScale; }
    public void setTextScale(float value) { textScale = Math.max(0.5F, Math.min(2.0F, value)); }
    public float getLineSpacing() { return lineSpacing; }
    public void setLineSpacing(float value) { lineSpacing = Math.max(0.0F, Math.min(10.0F, value)); }
    public int getDisplayTimeSeconds() { return displayTimeSeconds; }
    public void setDisplayTimeSeconds(int value) { displayTimeSeconds = Math.max(1, value); }
    public boolean isSmoothFade() { return smoothFade; }
    public void setSmoothFade(boolean value) { smoothFade = value; }
    public boolean isShowTimestamps() { return showTimestamps; }
    public void setShowTimestamps(boolean value) { showTimestamps = value; }
    public String getTimestampFormat() { return timestampFormat; }
    public void setTimestampFormat(String value) { timestampFormat = value == null || value.isBlank() ? "HH:mm" : value; }
    public int getTimestampColor() { return timestampColor; }
    public void setTimestampColor(int value) { timestampColor = value; }
    public boolean isTimestampParticipatesInFilters() { return timestampParticipatesInFilters; }
    public void setTimestampParticipatesInFilters(boolean value) { timestampParticipatesInFilters = value; }
    public boolean isShowRepeatCounter() { return showRepeatCounter; }
    public void setShowRepeatCounter(boolean value) { showRepeatCounter = value; }
    public long getMergeIntervalMillis() { return mergeIntervalMillis; }
    public void setMergeIntervalMillis(long value) { mergeIntervalMillis = Math.max(0L, value); }
    public boolean isClearOnServerChange() { return clearOnServerChange; }
    public void setClearOnServerChange(boolean value) { clearOnServerChange = value; }
    public boolean isPersistHistory() { return persistHistory; }
    public void setPersistHistory(boolean value) { persistHistory = value; }
    public int getMaximumPersistedHistory() { return maximumPersistedHistory; }
    public void setMaximumPersistedHistory(int value) { maximumPersistedHistory = Math.max(0, value); }
}

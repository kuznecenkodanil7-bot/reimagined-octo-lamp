package com.example.advancedchattabs.config;

import com.example.advancedchattabs.tab.TabSettings;

public final class GlobalConfig {
    private int schemaVersion = ConfigMigration.CURRENT_SCHEMA;
    private boolean debug;
    private boolean floatingWindowsVisible = true;
    private TabSettings defaultTabSettings = new TabSettings();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int value) { schemaVersion = value; }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean value) { debug = value; }
    public boolean isFloatingWindowsVisible() { return floatingWindowsVisible; }
    public void setFloatingWindowsVisible(boolean value) { floatingWindowsVisible = value; }
    public TabSettings getDefaultTabSettings() {
        if (defaultTabSettings == null) {
            defaultTabSettings = new TabSettings();
        }
        return defaultTabSettings;
    }
}

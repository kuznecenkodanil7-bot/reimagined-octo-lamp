package com.example.advancedchattabs.window;

import com.example.advancedchattabs.config.ConfigMigration;
import java.util.ArrayList;
import java.util.List;

public final class WindowsConfig {
    private int schemaVersion = ConfigMigration.CURRENT_SCHEMA;
    private List<ChatWindow> windows = new ArrayList<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int value) { schemaVersion = value; }
    public List<ChatWindow> getWindows() {
        if (windows == null) windows = new ArrayList<>();
        windows.removeIf(window -> window == null);
        for (ChatWindow window : windows) window.normalize();
        return windows;
    }
}

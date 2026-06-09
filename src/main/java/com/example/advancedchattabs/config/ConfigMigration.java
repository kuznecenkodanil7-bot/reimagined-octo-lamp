package com.example.advancedchattabs.config;

public final class ConfigMigration {
    public static final int CURRENT_SCHEMA = 2;

    public GlobalConfig migrate(GlobalConfig config) {
        if (config == null) {
            return new GlobalConfig();
        }
        if (config.getSchemaVersion() < 1) {
            config.setSchemaVersion(1);
        }
        if (config.getSchemaVersion() == 1) {
            config.getDefaultTabSettings().setMergeIntervalMillis(10_000L);
            config.setSchemaVersion(2);
        }
        if (config.getSchemaVersion() > CURRENT_SCHEMA) {
            return config;
        }
        config.setSchemaVersion(CURRENT_SCHEMA);
        return config;
    }

    public ServerConfig migrate(ServerConfig config, String key, String displayName) {
        if (config == null) {
            return ServerConfig.create(key, displayName);
        }
        config.setSchemaVersion(CURRENT_SCHEMA);
        config.setServerKey(key);
        config.normalize(displayName);
        return config;
    }
}

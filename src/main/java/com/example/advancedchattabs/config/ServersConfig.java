package com.example.advancedchattabs.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ServersConfig {
    private int schemaVersion = ConfigMigration.CURRENT_SCHEMA;
    private Map<String, ServerConfig> servers = new LinkedHashMap<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int value) { schemaVersion = value; }
    public Map<String, ServerConfig> getServers() {
        if (servers == null) servers = new LinkedHashMap<>();
        return servers;
    }
}

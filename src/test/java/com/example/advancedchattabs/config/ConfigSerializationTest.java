package com.example.advancedchattabs.config;

import com.example.advancedchattabs.tab.ChatTab;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSerializationTest {
    private final Gson gson = new GsonBuilder().create();

    @Test
    void serializesAndDeserializesServerConfiguration() {
        ServerConfig config = ServerConfig.create("example.org", "Example");
        config.getTabs().add(new ChatTab("Private", false, 1));
        String json = gson.toJson(config);
        ServerConfig restored = gson.fromJson(json, ServerConfig.class);
        restored.normalize("Example");
        assertEquals(2, restored.getTabs().size());
        assertEquals("Example", restored.getTabs().getFirst().getName());
        assertEquals("Private", restored.getTabs().get(1).getName());
    }

    @Test
    void ignoresUnknownFields() {
        GlobalConfig restored = gson.fromJson("{\"schemaVersion\":1,\"futureOption\":true}", GlobalConfig.class);
        assertNotNull(restored);
        assertEquals(1, restored.getSchemaVersion());
    }

    @Test
    void migratesSchemaVersion() {
        GlobalConfig old = new GlobalConfig();
        old.setSchemaVersion(1);
        GlobalConfig migrated = new ConfigMigration().migrate(old);
        assertEquals(ConfigMigration.CURRENT_SCHEMA, migrated.getSchemaVersion());
        assertEquals(10_000L, migrated.getDefaultTabSettings().getMergeIntervalMillis());
    }
}

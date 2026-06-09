package com.example.advancedchattabs.util;

import net.minecraft.client.MinecraftClient;

public record ServerIdentity(String key, String displayName) {
    public static ServerIdentity current(MinecraftClient client) {
        if (client.isInSingleplayer()) {
            return new ServerIdentity("singleplayer", "Singleplayer");
        }
        if (client.getCurrentServerEntry() != null) {
            String address = client.getCurrentServerEntry().address;
            String name = client.getCurrentServerEntry().name;
            return new ServerIdentity(address.toLowerCase(), name == null || name.isBlank() ? address : name);
        }
        return new ServerIdentity("disconnected", "Chat");
    }
}

package com.example.advancedchattabs;

import com.example.advancedchattabs.chat.ChatMessageRouter;
import com.example.advancedchattabs.command.ClientCommands;
import com.example.advancedchattabs.config.ConfigManager;
import com.example.advancedchattabs.filter.FilterEngine;
import com.example.advancedchattabs.input.KeyBindingManager;
import com.example.advancedchattabs.render.ChatOverlayRenderer;
import com.example.advancedchattabs.render.TabBarRenderer;
import com.example.advancedchattabs.tab.ChatTabManager;
import com.example.advancedchattabs.window.ChatWindowManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdvancedChatTabsClient implements ClientModInitializer {
    public static final String MOD_ID = "advanced_chat_tabs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static AdvancedChatTabsClient instance;

    private ConfigManager config;
    private FilterEngine filterEngine;
    private ChatTabManager tabs;
    private ChatWindowManager windows;
    private TabBarRenderer tabBar;
    private ChatOverlayRenderer overlay;

    @Override
    public void onInitializeClient() {
        instance = this;
        MinecraftClient client = MinecraftClient.getInstance();
        config = new ConfigManager(LOGGER);
        config.load();
        filterEngine = new FilterEngine();
        tabs = new ChatTabManager(client, config, new ChatMessageRouter(filterEngine), LOGGER);
        windows = new ChatWindowManager(client, config, tabs);
        tabBar = new TabBarRenderer(client, tabs);
        overlay = new ChatOverlayRenderer(tabBar, windows);

        new KeyBindingManager().registerTickHandler();
        ClientCommands.register();
        ClientTickEvents.END_CLIENT_TICK.register(ignored -> tabs.tick());
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.of(MOD_ID, "floating_windows"),
                (context, tickCounter) -> overlay.renderHud(context)
        );
        ClientLifecycleEvents.CLIENT_STOPPING.register(ignored -> {
            tabs.shutdown();
            config.close();
        });
        LOGGER.info("Advanced Chat Tabs initialized");
    }

    public static AdvancedChatTabsClient get() {
        if (instance == null) throw new IllegalStateException("Advanced Chat Tabs is not initialized");
        return instance;
    }

    public ConfigManager config() { return config; }
    public FilterEngine filterEngine() { return filterEngine; }
    public ChatTabManager tabs() { return tabs; }
    public ChatWindowManager windows() { return windows; }
    public TabBarRenderer tabBar() { return tabBar; }
    public ChatOverlayRenderer overlay() { return overlay; }
}

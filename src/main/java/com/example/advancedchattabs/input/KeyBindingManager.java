package com.example.advancedchattabs.input;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.screen.ChatTabSettingsScreen;
import com.example.advancedchattabs.screen.RenameTabScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class KeyBindingManager {
    private final KeyBinding next;
    private final KeyBinding previous;
    private final KeyBinding settings;
    private final KeyBinding create;
    private final KeyBinding toggleWindows;

    public KeyBindingManager() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("advanced_chat_tabs", "main"));
        next = register("key.advanced_chat_tabs.next_tab", GLFW.GLFW_KEY_RIGHT_BRACKET, category);
        previous = register("key.advanced_chat_tabs.previous_tab", GLFW.GLFW_KEY_LEFT_BRACKET, category);
        settings = register("key.advanced_chat_tabs.settings", GLFW.GLFW_KEY_APOSTROPHE, category);
        create = register("key.advanced_chat_tabs.new_tab", GLFW.GLFW_KEY_K, category);
        toggleWindows = register("key.advanced_chat_tabs.toggle_windows", GLFW.GLFW_KEY_O, category);
    }

    private KeyBinding register(String id, int key, KeyBinding.Category category) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(id, InputUtil.Type.KEYSYM, key, category));
    }

    public void registerTickHandler() {
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        while (next.wasPressed()) AdvancedChatTabsClient.get().tabs().nextTab(1);
        while (previous.wasPressed()) AdvancedChatTabsClient.get().tabs().nextTab(-1);
        while (settings.wasPressed()) client.setScreen(new ChatTabSettingsScreen(client.currentScreen, AdvancedChatTabsClient.get().tabs().activeTab()));
        while (create.wasPressed()) client.setScreen(new RenameTabScreen(client.currentScreen, AdvancedChatTabsClient.get().tabs().createTab("New tab")));
        while (toggleWindows.wasPressed()) AdvancedChatTabsClient.get().windows().toggleVisible();
    }
}

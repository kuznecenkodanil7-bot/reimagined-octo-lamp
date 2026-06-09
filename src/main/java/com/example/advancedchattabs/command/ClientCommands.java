package com.example.advancedchattabs.command;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.screen.AdvancedChatTabsScreen;
import com.example.advancedchattabs.screen.FilterListScreen;
import com.example.advancedchattabs.screen.TabListScreen;
import com.example.advancedchattabs.screen.WindowEditorScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public final class ClientCommands {
    private ClientCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("act")
                        .executes(context -> openMain())
                        .then(literal("tabs").executes(context -> openTabs()))
                        .then(literal("filters").executes(context -> openFilters()))
                        .then(literal("windows").executes(context -> openWindows()))
                        .then(literal("reload").executes(context -> reload()))
                        .then(literal("debug").executes(context -> debug()))
        ));
    }

    private static int openMain() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new AdvancedChatTabsScreen(client.currentScreen));
        return 1;
    }

    private static int openTabs() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new TabListScreen(client.currentScreen));
        return 1;
    }

    private static int openFilters() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new FilterListScreen(client.currentScreen, AdvancedChatTabsClient.get().tabs().activeTab()));
        return 1;
    }

    private static int openWindows() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new WindowEditorScreen(client.currentScreen));
        return 1;
    }

    private static int reload() {
        AdvancedChatTabsClient.get().config().load();
        AdvancedChatTabsClient.get().tabs().reloadProfile();
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("message.advanced_chat_tabs.reloaded"));
        return 1;
    }

    private static int debug() {
        boolean value = !AdvancedChatTabsClient.get().config().global().isDebug();
        AdvancedChatTabsClient.get().config().global().setDebug(value);
        AdvancedChatTabsClient.get().config().requestSave();
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable(value ? "message.advanced_chat_tabs.debug_on" : "message.advanced_chat_tabs.debug_off"));
        return 1;
    }
}

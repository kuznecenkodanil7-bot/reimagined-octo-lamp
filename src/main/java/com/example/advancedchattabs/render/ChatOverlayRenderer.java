package com.example.advancedchattabs.render;

import com.example.advancedchattabs.window.ChatWindowManager;
import net.minecraft.client.gui.DrawContext;

public final class ChatOverlayRenderer {
    private final TabBarRenderer tabBarRenderer;
    private final ChatWindowManager windowManager;

    public ChatOverlayRenderer(TabBarRenderer tabBarRenderer, ChatWindowManager windowManager) {
        this.tabBarRenderer = tabBarRenderer;
        this.windowManager = windowManager;
    }

    public void renderHud(DrawContext context) {
        windowManager.render(context);
    }

    public void renderChatScreen(DrawContext context) {
        tabBarRenderer.render(context, true);
        windowManager.render(context);
    }
}

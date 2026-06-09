package com.example.advancedchattabs.window;

import com.example.advancedchattabs.config.ConfigManager;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.tab.ChatTabManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ChatWindowManager {
    private final MinecraftClient client;
    private final ConfigManager configManager;
    private final ChatTabManager tabManager;

    public ChatWindowManager(MinecraftClient client, ConfigManager configManager, ChatTabManager tabManager) {
        this.client = client;
        this.configManager = configManager;
        this.tabManager = tabManager;
    }

    public ChatWindow createFor(ChatTab tab) {
        int offset = windows().size() * 18;
        ChatWindow window = new ChatWindow(tab.getName(), tab.getId(), 20 + offset, 20 + offset);
        configManager.windows().getWindows().add(window);
        tab.setWindowId(window.getId());
        clamp(window);
        configManager.requestSave();
        return window;
    }

    public void remove(UUID id) {
        configManager.windows().getWindows().removeIf(window -> window.getId().equals(id));
        configManager.requestSave();
    }

    public List<ChatWindow> windows() {
        return new ArrayList<>(configManager.windows().getWindows());
    }

    public Optional<ChatWindow> find(UUID id) {
        return configManager.windows().getWindows().stream().filter(window -> window.getId().equals(id)).findFirst();
    }

    public void render(DrawContext context) {
        if (!configManager.global().isFloatingWindowsVisible() || client.options.hudHidden) {
            return;
        }
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        for (ChatWindow window : windows().stream().sorted(Comparator.comparing(ChatWindow::isPinned)).toList()) {
            if (window.isClosed()) continue;
            window.clamp(screenWidth, screenHeight);
            int alpha = Math.round(window.getOpacity() * 255.0F);
            int background = (alpha << 24) | 0x101010;
            context.fill(window.getX(), window.getY(), window.getX() + window.getWidth(), window.getY() + (window.isMinimized() ? 14 : window.getHeight()), background);
            context.fill(window.getX(), window.getY(), window.getX() + window.getWidth(), window.getY() + 14, 0xCC202020);
            context.drawTextWithShadow(client.textRenderer, Text.literal(window.getName()), window.getX() + 4, window.getY() + 3, 0xFFFFFFFF);
            context.drawTextWithShadow(client.textRenderer, Text.literal(window.isMinimized() ? "+" : "−"), window.getX() + window.getWidth() - 20, window.getY() + 3, 0xFFAAAAAA);
            context.drawTextWithShadow(client.textRenderer, Text.literal("×"), window.getX() + window.getWidth() - 10, window.getY() + 3, 0xFFFF7777);
            if (!window.isMinimized()) {
                renderMessages(context, window);
            }
        }
    }

    private void renderMessages(DrawContext context, ChatWindow window) {
        UUID tabId = window.getActiveTabId();
        ChatTab tab = tabManager.find(tabId).orElse(null);
        if (tab == null) return;
        List<com.example.advancedchattabs.chat.ChatHistory.Entry<com.example.advancedchattabs.chat.VanillaChatPayload>> entries = tab.getHistory().snapshot();
        int lineHeight = client.textRenderer.fontHeight + 1;
        int available = Math.max(1, (window.getHeight() - 20) / lineHeight);
        int start = Math.max(0, entries.size() - available);
        int y = window.getY() + 17;
        for (int index = start; index < entries.size(); index++) {
            var entry = entries.get(index);
            var payload = entry.message().richText();
            Text text = entry.repeatCount() > 1
                    ? Text.empty().append(payload.text().copy()).append(Text.literal(" ×" + entry.repeatCount()))
                    : payload.text();
            Integer configuredColor = payload.backgroundColor();
            Float configuredAlpha = payload.backgroundAlpha();
            if (configuredColor != null || configuredAlpha != null) {
                int rgb = configuredColor == null ? 0x101010 : configuredColor & 0xFFFFFF;
                float alphaValue = configuredAlpha == null ? 0.5F : configuredAlpha;
                int lineBackground = (Math.round(alphaValue * 255.0F) << 24) | rgb;
                context.fill(window.getX() + 2, y - 1, window.getX() + window.getWidth() - 2, y + lineHeight - 1, lineBackground);
            }
            context.drawTextWithShadow(client.textRenderer, text, window.getX() + 4, y, 0xFFFFFFFF);
            y += lineHeight;
        }
    }

    public void clamp(ChatWindow window) {
        window.clamp(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        window.snap(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 6);
    }

    public void toggleVisible() {
        configManager.global().setFloatingWindowsVisible(!configManager.global().isFloatingWindowsVisible());
        configManager.requestSave();
    }
}

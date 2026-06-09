package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.window.ChatWindow;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class WindowEditorScreen extends Screen {
    private final Screen parent;
    private ChatWindow selected;
    private boolean dragging;
    private boolean resizing;

    public WindowEditorScreen(Screen parent) {
        super(Text.translatable("screen.advanced_chat_tabs.windows"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.new_window"), button -> {
            ChatTab tab = AdvancedChatTabsClient.get().tabs().activeTab();
            selected = AdvancedChatTabsClient.get().windows().createFor(tab);
        }).dimensions(8, 8, 110, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Minimize"), button -> {
            if (selected != null) selected.setMinimized(!selected.isMinimized());
        }).dimensions(122, 8, 90, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Pin"), button -> {
            if (selected != null) selected.setPinned(!selected.isPinned());
        }).dimensions(216, 8, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.delete"), button -> {
            if (selected != null) {
                AdvancedChatTabsClient.get().windows().remove(selected.getId());
                selected = null;
            }
        }).dimensions(290, 8, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.back"), button -> close())
                .dimensions(width - 88, 8, 80, 20).build());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        for (ChatWindow window : AdvancedChatTabsClient.get().windows().windows()) {
            int visibleHeight = window.isMinimized() ? 14 : window.getHeight();
            if (inside(click.x(), click.y(), window.getX(), window.getY(), window.getWidth(), visibleHeight)) {
                selected = window;
                resizing = !window.isMinimized() && inside(click.x(), click.y(), window.getX() + window.getWidth() - 10, window.getY() + window.getHeight() - 10, 10, 10);
                dragging = !resizing && click.y() < window.getY() + 14;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        if (selected != null && dragging) {
            selected.setX(selected.getX() + (int) Math.round(offsetX));
            selected.setY(selected.getY() + (int) Math.round(offsetY));
            AdvancedChatTabsClient.get().windows().clamp(selected);
            return true;
        }
        if (selected != null && resizing) {
            selected.setWidth(selected.getWidth() + (int) Math.round(offsetX));
            selected.setHeight(selected.getHeight() + (int) Math.round(offsetY));
            AdvancedChatTabsClient.get().windows().clamp(selected);
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging || resizing) {
            dragging = false;
            resizing = false;
            if (selected != null) AdvancedChatTabsClient.get().windows().clamp(selected);
            AdvancedChatTabsClient.get().config().requestSave();
            return true;
        }
        return super.mouseReleased(click);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void close() {
        AdvancedChatTabsClient.get().config().requestSave();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        for (ChatWindow window : AdvancedChatTabsClient.get().windows().windows()) {
            int h = window.isMinimized() ? 14 : window.getHeight();
            int border = window == selected ? 0xFF55FF55 : 0xFF888888;
            context.fill(window.getX() - 1, window.getY() - 1, window.getX() + window.getWidth() + 1, window.getY(), border);
            context.fill(window.getX() - 1, window.getY() + h, window.getX() + window.getWidth() + 1, window.getY() + h + 1, border);
            context.fill(window.getX() - 1, window.getY(), window.getX(), window.getY() + h, border);
            context.fill(window.getX() + window.getWidth(), window.getY(), window.getX() + window.getWidth() + 1, window.getY() + h, border);
            context.fill(window.getX(), window.getY(), window.getX() + window.getWidth(), window.getY() + h, 0x80151515);
            context.drawTextWithShadow(textRenderer, Text.literal(window.getName()), window.getX() + 4, window.getY() + 3, 0xFFFFFFFF);
            if (!window.isMinimized()) {
                context.fill(window.getX() + window.getWidth() - 8, window.getY() + window.getHeight() - 8, window.getX() + window.getWidth(), window.getY() + window.getHeight(), 0xFFAAAAAA);
            }
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

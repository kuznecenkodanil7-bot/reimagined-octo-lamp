package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class AdvancedChatTabsScreen extends Screen {
    private final Screen parent;

    public AdvancedChatTabsScreen(Screen parent) {
        super(Text.translatable("screen.advanced_chat_tabs.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        int y = height / 2 - 70;
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.advanced_chat_tabs.global_settings"), button -> client.setScreen(new ChatTabSettingsScreen(this, AdvancedChatTabsClient.get().config().global().getDefaultTabSettings())))
                .dimensions(center - 100, y, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.advanced_chat_tabs.tabs"), button -> client.setScreen(new TabListScreen(this)))
                .dimensions(center - 100, y + 24, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.advanced_chat_tabs.filters"), button -> client.setScreen(new FilterListScreen(this, AdvancedChatTabsClient.get().tabs().activeTab())))
                .dimensions(center - 100, y + 48, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("screen.advanced_chat_tabs.windows"), button -> client.setScreen(new WindowEditorScreen(this)))
                .dimensions(center - 100, y + 72, 200, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.back"), button -> close())
                .dimensions(center - 100, y + 106, 200, 20).build());
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xB0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 24, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

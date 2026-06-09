package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.tab.ChatTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class TabContextMenuScreen extends Screen {
    private final Screen parent;
    private final ChatTab tab;

    public TabContextMenuScreen(Screen parent, ChatTab tab) {
        super(Text.literal(tab.getName()));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        int x = width / 2 - 90;
        int y = Math.max(28, height / 2 - 92);
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.new_tab"), button -> client.setScreen(new RenameTabScreen(this, AdvancedChatTabsClient.get().tabs().createTab("New tab"))))
                .dimensions(x, y, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.new_window"), button -> { AdvancedChatTabsClient.get().windows().createFor(tab); client.setScreen(new WindowEditorScreen(parent)); })
                .dimensions(x, y + 22, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.rename"), button -> client.setScreen(new RenameTabScreen(this, tab)))
                .dimensions(x, y + 44, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.settings"), button -> client.setScreen(new ChatTabSettingsScreen(this, tab)))
                .dimensions(x, y + 66, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.filters"), button -> client.setScreen(new FilterListScreen(this, tab)))
                .dimensions(x, y + 88, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable(tab.isPinned() ? "button.advanced_chat_tabs.unpin" : "button.advanced_chat_tabs.pin"), button -> {
                    tab.setPinned(!tab.isPinned());
                    AdvancedChatTabsClient.get().config().requestSave();
                    init();
                }).dimensions(x, y + 110, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.duplicate"), button -> { AdvancedChatTabsClient.get().tabs().duplicate(tab); close(); })
                .dimensions(x, y + 132, 180, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.clear"), button -> { AdvancedChatTabsClient.get().tabs().clearTab(tab); close(); })
                .dimensions(x, y + 154, 180, 20).build());
        if (!tab.isSystemTab()) {
            addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.delete"), button -> { AdvancedChatTabsClient.get().tabs().delete(tab.getId()); close(); })
                    .dimensions(x, y + 176, 180, 20).build());
        }
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0x90000000);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 12, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

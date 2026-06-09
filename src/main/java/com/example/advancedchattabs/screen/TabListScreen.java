package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.tab.ChatTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public final class TabListScreen extends Screen {
    private final Screen parent;
    private int page;

    public TabListScreen(Screen parent) {
        super(Text.translatable("screen.advanced_chat_tabs.tabs"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearChildren();
        List<ChatTab> tabs = AdvancedChatTabsClient.get().tabs().tabs();
        int pageSize = 7;
        int start = Math.min(page * pageSize, Math.max(0, tabs.size() - 1));
        int end = Math.min(tabs.size(), start + pageSize);
        for (int index = start; index < end; index++) {
            ChatTab tab = tabs.get(index);
            int y = 46 + (index - start) * 24;
            String suffix = tab.getUnreadCount() > 0 ? " [" + tab.getUnreadCount() + "]" : "";
            addDrawableChild(ButtonWidget.builder(Text.literal(tab.getName() + suffix), button -> {
                AdvancedChatTabsClient.get().tabs().switchTo(tab.getId());
                client.setScreen(new TabContextMenuScreen(this, tab));
            }).dimensions(width / 2 - 150, y, 230, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("↑"), button -> { AdvancedChatTabsClient.get().tabs().move(tab, -1); init(); })
                    .dimensions(width / 2 + 84, y, 30, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("↓"), button -> { AdvancedChatTabsClient.get().tabs().move(tab, 1); init(); })
                    .dimensions(width / 2 + 118, y, 30, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.new_tab"), button -> {
            ChatTab tab = AdvancedChatTabsClient.get().tabs().createTab("Tab " + (tabs.size() + 1));
            client.setScreen(new RenameTabScreen(this, tab));
        }).dimensions(width / 2 - 150, height - 52, 146, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.back"), button -> close())
                .dimensions(width / 2 + 4, height - 52, 146, 20).build());
        if (start > 0) {
            addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> { page--; init(); }).dimensions(width / 2 - 190, height / 2 - 10, 30, 20).build());
        }
        if (end < tabs.size()) {
            addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> { page++; init(); }).dimensions(width / 2 + 160, height / 2 - 10, 30, 20).build());
        }
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xB0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

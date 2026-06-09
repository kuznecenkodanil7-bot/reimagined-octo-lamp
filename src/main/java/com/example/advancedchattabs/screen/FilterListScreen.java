package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.filter.ChatFilter;
import com.example.advancedchattabs.tab.ChatTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FilterListScreen extends Screen {
    private final Screen parent;
    private final ChatTab tab;
    private TextFieldWidget search;

    public FilterListScreen(Screen parent, ChatTab tab) {
        super(Text.translatable("screen.advanced_chat_tabs.filters"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        clearChildren();
        search = new TextFieldWidget(textRenderer, width / 2 - 150, 32, 300, 20, Text.translatable("label.advanced_chat_tabs.search"));
        search.setChangedListener(value -> rebuildList());
        addDrawableChild(search);
        rebuildList();
    }

    private void rebuildList() {
        String value = search == null ? "" : search.getText();
        clearChildren();
        addDrawableChild(search);
        List<ChatFilter> filters = tab.getFilters().stream()
                .filter(filter -> filter.getName().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT)))
                .limit(8)
                .toList();
        for (int index = 0; index < filters.size(); index++) {
            ChatFilter filter = filters.get(index);
            int y = 60 + index * 24;
            String condition = filter.getIncludeConditions().isEmpty() ? "*" : String.join(" | ", filter.getIncludeConditions());
            String label = (filter.isEnabled() ? "[+] " : "[-] ") + filter.getName() + " : " + condition;
            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> client.setScreen(new FilterEditorScreen(this, tab, filter)))
                    .dimensions(width / 2 - 150, y, 174, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.translatable(filter.isEnabled() ? "button.advanced_chat_tabs.enabled" : "button.advanced_chat_tabs.disabled"), button -> {
                        filter.setEnabled(!filter.isEnabled());
                        AdvancedChatTabsClient.get().config().requestSave();
                        rebuildList();
                    }).dimensions(width / 2 + 28, y, 54, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("↑"), button -> {
                        move(filter, -1);
                        rebuildList();
                    }).dimensions(width / 2 + 86, y, 30, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("↓"), button -> {
                        move(filter, 1);
                        rebuildList();
                    }).dimensions(width / 2 + 120, y, 30, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.new_filter"), button -> {
            ChatFilter filter = new ChatFilter(Text.translatable("default.advanced_chat_tabs.filter_name", tab.getFilters().size() + 1).getString());
            tab.getFilters().add(filter);
            client.setScreen(new FilterEditorScreen(this, tab, filter));
        }).dimensions(width / 2 - 150, height - 52, 146, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.back"), button -> close())
                .dimensions(width / 2 + 4, height - 52, 146, 20).build());
    }

    private void move(ChatFilter filter, int offset) {
        List<ChatFilter> values = tab.getFilters();
        int from = values.indexOf(filter);
        int to = Math.max(0, Math.min(values.size() - 1, from + offset));
        if (from < 0 || from == to) return;
        values.remove(from);
        values.add(to, filter);
        for (int index = 0; index < values.size(); index++) {
            values.get(index).setPriority(values.size() - index);
        }
        AdvancedChatTabsClient.get().config().requestSave();
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

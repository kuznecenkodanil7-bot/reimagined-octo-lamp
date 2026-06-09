package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.filter.FilterAction;
import com.example.advancedchattabs.filter.FilterActionType;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.window.ChatWindow;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class FilterActionEditorScreen extends Screen {
    private final Screen parent;
    private final ChatTab owner;
    private final com.example.advancedchattabs.filter.ChatFilter filter;
    private int selectedIndex;
    private TextFieldWidget color;
    private TextFieldWidget alpha;
    private TextFieldWidget soundId;
    private TextFieldWidget volume;
    private TextFieldWidget pitch;
    private TextFieldWidget cooldown;
    private TextFieldWidget icon;

    public FilterActionEditorScreen(Screen parent, ChatTab owner, com.example.advancedchattabs.filter.ChatFilter filter) {
        super(Text.translatable("screen.advanced_chat_tabs.action_editor"));
        this.parent = parent;
        this.owner = owner;
        this.filter = filter;
    }

    @Override
    protected void init() {
        clearChildren();
        if (filter.getActions().isEmpty()) filter.getActions().add(FilterAction.addCurrent());
        selectedIndex = Math.max(0, Math.min(selectedIndex, filter.getActions().size() - 1));
        FilterAction action = current();
        int x = width / 2 - 155;

        addDrawableChild(ButtonWidget.builder(Text.literal("<"), button -> { saveFields(); selectedIndex = Math.floorMod(selectedIndex - 1, filter.getActions().size()); init(); })
                .dimensions(x, 34, 30, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("label.advanced_chat_tabs.action_index", selectedIndex + 1, filter.getActions().size()), button -> {})
                .dimensions(x + 34, 34, 174, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), button -> { saveFields(); selectedIndex = (selectedIndex + 1) % filter.getActions().size(); init(); })
                .dimensions(x + 212, 34, 30, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("+"), button -> { saveFields(); filter.getActions().add(FilterAction.addCurrent()); selectedIndex = filter.getActions().size() - 1; init(); })
                .dimensions(x + 246, 34, 30, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("−"), button -> { filter.getActions().remove(selectedIndex); selectedIndex = Math.max(0, selectedIndex - 1); init(); })
                .dimensions(x + 280, 34, 30, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("filter.advanced_chat_tabs.action").append(Text.literal(": " + action.getType().name())), button -> {
                    saveFields();
                    action.setType(next(FilterActionType.values(), action.getType()));
                    init();
                }).dimensions(x, 62, 310, 20).build());

        int y = 90;
        switch (action.getType()) {
            case ADD_TO_TAB -> addDrawableChild(ButtonWidget.builder(Text.translatable("filter.advanced_chat_tabs.target_tab").append(Text.literal(": " + targetTabName(action))), button -> {
                        cycleTargetTab(action);
                        init();
                    }).dimensions(x, y, 310, 20).build());
            case COPY_TO_WINDOW -> addDrawableChild(ButtonWidget.builder(Text.translatable("filter.advanced_chat_tabs.target_window").append(Text.literal(": " + targetWindowName(action))), button -> {
                        cycleTargetWindow(action);
                        init();
                    }).dimensions(x, y, 310, 20).build());
            case HIDE_MESSAGE -> addDrawableChild(ButtonWidget.builder(Text.translatable("filter.advanced_chat_tabs.global_hide").append(Text.literal(": ")).append(Text.translatable(
                            action.isGlobalHide() ? "button.advanced_chat_tabs.enabled" : "button.advanced_chat_tabs.disabled")), button -> {
                        action.setGlobalHide(!action.isGlobalHide());
                        init();
                    }).dimensions(x, y, 310, 20).build());
            case SET_TEXT_COLOR, SET_BACKGROUND_COLOR -> color = field(x, y, String.format("#%08X", action.getColor()), "filter.advanced_chat_tabs.color");
            case SET_BACKGROUND_ALPHA -> alpha = field(x, y, Float.toString(action.getAlpha()), "filter.advanced_chat_tabs.alpha");
            case PLAY_SOUND -> {
                soundId = field(x, y, action.getSoundId(), "filter.advanced_chat_tabs.sound_id");
                volume = field(x, y + 26, Float.toString(action.getVolume()), "filter.advanced_chat_tabs.volume");
                pitch = field(x, y + 52, Float.toString(action.getPitch()), "filter.advanced_chat_tabs.pitch");
                cooldown = field(x, y + 78, Long.toString(action.getCooldownMillis()), "filter.advanced_chat_tabs.cooldown");
                addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.preview_sound"), button -> {
                            saveFields();
                            AdvancedChatTabsClient.get().tabs().playSound(action);
                        }).dimensions(x, y + 104, 310, 20).build());
            }
            case ADD_ICON -> icon = field(x, y, action.getIcon(), "filter.advanced_chat_tabs.icon");
            default -> contextHint(x, y);
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.save"), button -> {
                    saveFields();
                    AdvancedChatTabsClient.get().config().requestSave();
                    close();
                }).dimensions(x, height - 44, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.back"), button -> {
                    saveFields();
                    close();
                }).dimensions(x + 160, height - 44, 150, 20).build());
    }

    private void contextHint(int x, int y) {
        addDrawableChild(ButtonWidget.builder(Text.translatable("filter.advanced_chat_tabs.no_extra_parameters"), button -> {})
                .dimensions(x, y, 310, 20).build()).active = false;
    }

    private TextFieldWidget field(int x, int y, String value, String key) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, 310, 20, Text.translatable(key));
        field.setText(value == null ? "" : value);
        field.setMaxLength(256);
        addDrawableChild(field);
        return field;
    }

    private void saveFields() {
        if (filter.getActions().isEmpty()) return;
        FilterAction action = current();
        if (color != null) action.setColor(parseColor(color.getText(), action.getColor()));
        if (alpha != null) action.setAlpha(parseFloat(alpha.getText(), action.getAlpha()));
        if (soundId != null) action.setSoundId(soundId.getText());
        if (volume != null) action.setVolume(parseFloat(volume.getText(), action.getVolume()));
        if (pitch != null) action.setPitch(parseFloat(pitch.getText(), action.getPitch()));
        if (cooldown != null) action.setCooldownMillis(parseLong(cooldown.getText(), action.getCooldownMillis()));
        if (icon != null) action.setIcon(icon.getText());
    }

    private FilterAction current() {
        return filter.getActions().get(selectedIndex);
    }

    private String targetTabName(FilterAction action) {
        return AdvancedChatTabsClient.get().tabs().find(action.getTargetTabId()).map(ChatTab::getName)
                .orElse(Text.translatable("value.advanced_chat_tabs.none").getString());
    }

    private void cycleTargetTab(FilterAction action) {
        List<ChatTab> tabs = AdvancedChatTabsClient.get().tabs().tabs();
        if (tabs.isEmpty()) { action.setTargetTabId(null); return; }
        int current = -1;
        for (int index = 0; index < tabs.size(); index++) {
            if (tabs.get(index).getId().equals(action.getTargetTabId())) current = index;
        }
        action.setTargetTabId(tabs.get((current + 1) % tabs.size()).getId());
    }

    private String targetWindowName(FilterAction action) {
        return AdvancedChatTabsClient.get().windows().find(action.getTargetWindowId()).map(ChatWindow::getName)
                .orElse(Text.translatable("value.advanced_chat_tabs.none").getString());
    }

    private void cycleTargetWindow(FilterAction action) {
        List<ChatWindow> windows = AdvancedChatTabsClient.get().windows().windows();
        if (windows.isEmpty()) { action.setTargetWindowId(null); return; }
        int current = -1;
        for (int index = 0; index < windows.size(); index++) {
            if (windows.get(index).getId().equals(action.getTargetWindowId())) current = index;
        }
        action.setTargetWindowId(windows.get((current + 1) % windows.size()).getId());
    }

    private int parseColor(String value, int fallback) {
        try {
            String cleaned = value == null ? "" : value.trim().replace("#", "");
            long parsed = Long.parseUnsignedLong(cleaned, 16);
            if (cleaned.length() <= 6) parsed |= 0xFF000000L;
            return (int) parsed;
        } catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private float parseFloat(String value, float fallback) {
        try { return Float.parseFloat(value); } catch (RuntimeException ignored) { return fallback; }
    }

    private long parseLong(String value, long fallback) {
        try { return Long.parseLong(value); } catch (RuntimeException ignored) { return fallback; }
    }

    private static <T> T next(T[] values, T current) {
        int index = Arrays.asList(values).indexOf(current);
        return values[(Math.max(0, index) + 1) % values.length];
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

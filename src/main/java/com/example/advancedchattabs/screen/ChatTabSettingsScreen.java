package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.tab.ChatTab;
import com.example.advancedchattabs.tab.TabSettings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class ChatTabSettingsScreen extends Screen {
    private final Screen parent;
    private final ChatTab tab;
    private final TabSettings settings;
    private final boolean globalProfile;
    private int page;
    private TextFieldWidget nameField;
    private TextFieldWidget timestampField;

    public ChatTabSettingsScreen(Screen parent, ChatTab tab) {
        super(Text.translatable("screen.advanced_chat_tabs.settings"));
        this.parent = parent;
        this.tab = tab;
        this.settings = tab.getSettings();
        this.globalProfile = false;
    }

    public ChatTabSettingsScreen(Screen parent, TabSettings globalSettings) {
        super(Text.translatable("screen.advanced_chat_tabs.global_settings"));
        this.parent = parent;
        this.tab = null;
        this.settings = globalSettings;
        this.globalProfile = true;
    }

    @Override
    protected void init() {
        clearChildren();
        int left = width / 2 - 155;
        int startY = 36;

        if (!globalProfile) {
            nameField = new TextFieldWidget(textRenderer, left, startY, 310, 20, Text.translatable("label.advanced_chat_tabs.name"));
            nameField.setText(tab.getName());
            nameField.setMaxLength(64);
            addDrawableChild(nameField);
            addToggle(left, startY + 24, "setting.advanced_chat_tabs.use_global", settings.isUseGlobalSettings(),
                    () -> settings.setUseGlobalSettings(!settings.isUseGlobalSettings()), false);
            startY += 52;
        }

        if (page == 0) {
            addCycle(left, startY, "setting.advanced_chat_tabs.maximum_lines", Integer.toString(settings.getMaximumLines()),
                    () -> settings.setMaximumLines(settings.getMaximumLines() >= 1000 ? 50 : settings.getMaximumLines() + 50));
            addToggle(left, startY + 24, "setting.advanced_chat_tabs.merge", settings.isMergeIdenticalMessages(),
                    () -> settings.setMergeIdenticalMessages(!settings.isMergeIdenticalMessages()), true);
            addCycle(left, startY + 48, "setting.advanced_chat_tabs.merge_interval", settings.getMergeIntervalMillis() / 1000 + " s",
                    () -> settings.setMergeIntervalMillis(settings.getMergeIntervalMillis() >= 60_000 ? 5_000 : settings.getMergeIntervalMillis() + 5_000));
            addToggle(left, startY + 72, "setting.advanced_chat_tabs.protect_clear", settings.isProtectFromClear(),
                    () -> settings.setProtectFromClear(!settings.isProtectFromClear()), true);
            addToggle(left, startY + 96, "setting.advanced_chat_tabs.trust_indicators", settings.isShowTrustIndicators(),
                    () -> settings.setShowTrustIndicators(!settings.isShowTrustIndicators()), true);
            addToggle(left, startY + 120, "setting.advanced_chat_tabs.text_shadow", settings.isTextShadow(),
                    () -> settings.setTextShadow(!settings.isTextShadow()), true);
            addToggle(left, startY + 144, "setting.advanced_chat_tabs.background", settings.isMessageBackground(),
                    () -> settings.setMessageBackground(!settings.isMessageBackground()), true);
        } else if (page == 1) {
            addCycle(left, startY, "setting.advanced_chat_tabs.background_opacity", Math.round(settings.getBackgroundOpacity() * 100) + "%",
                    () -> settings.setBackgroundOpacity(settings.getBackgroundOpacity() >= 1.0F ? 0.0F : settings.getBackgroundOpacity() + 0.1F));
            addCycle(left, startY + 24, "setting.advanced_chat_tabs.text_scale", String.format("%.1f", settings.getTextScale()),
                    () -> settings.setTextScale(settings.getTextScale() >= 2.0F ? 0.5F : settings.getTextScale() + 0.1F));
            addCycle(left, startY + 48, "setting.advanced_chat_tabs.line_spacing", String.format("%.1f", settings.getLineSpacing()),
                    () -> settings.setLineSpacing(settings.getLineSpacing() >= 5.0F ? 0.0F : settings.getLineSpacing() + 0.5F));
            addCycle(left, startY + 72, "setting.advanced_chat_tabs.display_time", settings.getDisplayTimeSeconds() + " s",
                    () -> settings.setDisplayTimeSeconds(settings.getDisplayTimeSeconds() >= 60 ? 5 : settings.getDisplayTimeSeconds() + 5));
            addToggle(left, startY + 96, "setting.advanced_chat_tabs.smooth_fade", settings.isSmoothFade(),
                    () -> settings.setSmoothFade(!settings.isSmoothFade()), true);
            addToggle(left, startY + 120, "setting.advanced_chat_tabs.timestamps", settings.isShowTimestamps(),
                    () -> settings.setShowTimestamps(!settings.isShowTimestamps()), true);
            timestampField = new TextFieldWidget(textRenderer, left, startY + 144, 310, 20, Text.translatable("setting.advanced_chat_tabs.timestamp_format"));
            timestampField.setText(settings.getTimestampFormat());
            timestampField.setMaxLength(40);
            addDrawableChild(timestampField);
        } else {
            addToggle(left, startY, "setting.advanced_chat_tabs.timestamp_in_filters", settings.isTimestampParticipatesInFilters(),
                    () -> settings.setTimestampParticipatesInFilters(!settings.isTimestampParticipatesInFilters()), true);
            addToggle(left, startY + 24, "setting.advanced_chat_tabs.repeat_counter", settings.isShowRepeatCounter(),
                    () -> settings.setShowRepeatCounter(!settings.isShowRepeatCounter()), true);
            addToggle(left, startY + 48, "setting.advanced_chat_tabs.clear_on_server_change", settings.isClearOnServerChange(),
                    () -> settings.setClearOnServerChange(!settings.isClearOnServerChange()), true);
            addToggle(left, startY + 72, "setting.advanced_chat_tabs.persist_history", settings.isPersistHistory(),
                    () -> settings.setPersistHistory(!settings.isPersistHistory()), true);
            addCycle(left, startY + 96, "setting.advanced_chat_tabs.persisted_limit", Integer.toString(settings.getMaximumPersistedHistory()),
                    () -> settings.setMaximumPersistedHistory(settings.getMaximumPersistedHistory() >= 10_000 ? 0 : settings.getMaximumPersistedHistory() + 500));
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.page", page + 1, 3), button -> {
                    saveFields();
                    page = (page + 1) % 3;
                    init();
                }).dimensions(left, height - 48, 74, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.reset"), button -> {
                    settings.resetToDefaults();
                    if (globalProfile) settings.setUseGlobalSettings(false);
                    init();
                }).dimensions(left + 78, height - 48, 154, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.save"), button -> {
                    saveFields();
                    AdvancedChatTabsClient.get().config().requestSave();
                    close();
                }).dimensions(left + 236, height - 48, 74, 20).build());
        setDisabledState(!globalProfile && settings.isUseGlobalSettings(), startY);
    }

    private void setDisabledState(boolean inherited, int startY) {
        if (!inherited) return;
        for (var child : children()) {
            if (child instanceof ButtonWidget button && button.getY() >= startY && button.getY() < height - 48) {
                button.active = false;
            }
            if (child == timestampField && timestampField != null) {
                timestampField.active = false;
            }
        }
    }

    private void addToggle(int x, int y, String key, boolean value, Runnable action, boolean inheritable) {
        Text label = Text.translatable(key).append(Text.literal(": ")).append(Text.translatable(
                value ? "button.advanced_chat_tabs.enabled" : "button.advanced_chat_tabs.disabled"));
        ButtonWidget button = ButtonWidget.builder(label, ignored -> {
            action.run();
            saveFields();
            init();
        }).dimensions(x, y, 310, 20).build();
        addDrawableChild(button);
        if (inheritable && !globalProfile && settings.isUseGlobalSettings()) button.active = false;
    }

    private void addCycle(int x, int y, String key, String value, Runnable action) {
        ButtonWidget button = ButtonWidget.builder(Text.translatable(key).append(Text.literal(": " + value)), ignored -> {
            action.run();
            saveFields();
            init();
        }).dimensions(x, y, 310, 20).build();
        addDrawableChild(button);
        if (!globalProfile && settings.isUseGlobalSettings()) button.active = false;
    }

    private void saveFields() {
        if (tab != null && nameField != null) tab.setName(nameField.getText());
        if (timestampField != null) settings.setTimestampFormat(timestampField.getText());
    }

    @Override
    public void close() {
        saveFields();
        client.setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xC0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 14, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

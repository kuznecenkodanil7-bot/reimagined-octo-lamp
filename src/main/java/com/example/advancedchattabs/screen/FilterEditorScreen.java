package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.chat.ChatMessage;
import com.example.advancedchattabs.chat.ChatMessageType;
import com.example.advancedchattabs.chat.TrustStatus;
import com.example.advancedchattabs.filter.ChatFilter;
import com.example.advancedchattabs.filter.FilterScope;
import com.example.advancedchattabs.filter.FilterTestResult;
import com.example.advancedchattabs.filter.LogicalMode;
import com.example.advancedchattabs.filter.MatchMode;
import com.example.advancedchattabs.tab.ChatTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public final class FilterEditorScreen extends Screen {
    private final Screen parent;
    private final ChatTab tab;
    private final ChatFilter filter;
    private TextFieldWidget name;
    private TextFieldWidget include;
    private TextFieldWidget exclude;
    private TextFieldWidget testMessage;
    private String testResult = "";
    private int testColor = 0xFFAAAAAA;
    private int page;

    public FilterEditorScreen(Screen parent, ChatTab tab, ChatFilter filter) {
        super(Text.translatable("screen.advanced_chat_tabs.filter_editor"));
        this.parent = parent;
        this.tab = tab;
        this.filter = filter;
    }

    @Override
    protected void init() {
        String preservedTest = testMessage == null ? "[ЛС] Player123: привет" : testMessage.getText();
        clearChildren();
        int x = width / 2 - 155;
        name = field(x, 32, filter.getName(), Text.translatable("label.advanced_chat_tabs.name"));
        include = field(x, 58, String.join("; ", filter.getIncludeConditions()), Text.translatable("label.advanced_chat_tabs.include"));
        exclude = field(x, 84, String.join("; ", filter.getExcludeConditions()), Text.translatable("label.advanced_chat_tabs.exclude"));
        testMessage = field(x, 254, preservedTest, Text.translatable("label.advanced_chat_tabs.test_message"));

        if (page == 0) {
            addToggle(x, 112, "filter.advanced_chat_tabs.enabled", filter.isEnabled(), () -> filter.setEnabled(!filter.isEnabled()));
            addToggle(x, 136, "filter.advanced_chat_tabs.case_sensitive", filter.isCaseSensitive(), () -> filter.setCaseSensitive(!filter.isCaseSensitive()));
            addToggle(x, 160, "filter.advanced_chat_tabs.regex", filter.isUseRegularExpression(), () -> filter.setUseRegularExpression(!filter.isUseRegularExpression()));
            addValue(x, 184, "filter.advanced_chat_tabs.match_mode", filter.getMatchMode().name(), () -> filter.setMatchMode(next(MatchMode.values(), filter.getMatchMode())));
            addValue(x, 208, "filter.advanced_chat_tabs.logical_mode", filter.getLogicalMode().name(), () -> filter.setLogicalMode(filter.getLogicalMode() == LogicalMode.ANY ? LogicalMode.ALL : LogicalMode.ANY));
            addToggle(x, 232, "filter.advanced_chat_tabs.stop_after_match", filter.isStopAfterMatch(), () -> filter.setStopAfterMatch(!filter.isStopAfterMatch()));
        } else {
            addToggle(x, 112, "filter.advanced_chat_tabs.inspect_hover", filter.isInspectHover(), () -> filter.setInspectHover(!filter.isInspectHover()));
            addToggle(x, 136, "filter.advanced_chat_tabs.plain_only", filter.isPlainTextOnly(), () -> filter.setPlainTextOnly(!filter.isPlainTextOnly()));
            addToggle(x, 160, "filter.advanced_chat_tabs.inspect_sender", filter.isInspectSender(), () -> filter.setInspectSender(!filter.isInspectSender()));
            addValue(x, 184, "filter.advanced_chat_tabs.scope", filter.getScope().name(), () -> filter.setScope(next(FilterScope.values(), filter.getScope())));
            addValue(x, 208, "filter.advanced_chat_tabs.message_type", messageTypeLabel(), this::cycleMessageType);
            addValue(x, 232, "filter.advanced_chat_tabs.trust_status", trustLabel(), this::cycleTrustStatus);
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.test"), button -> test())
                .dimensions(x, 280, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.actions"), button -> {
                    saveFields();
                    client.setScreen(new FilterActionEditorScreen(this, tab, filter));
                }).dimensions(x + 106, 280, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.page", page + 1, 2), button -> {
                    saveFields();
                    page = 1 - page;
                    init();
                }).dimensions(x + 212, 280, 98, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.duplicate"), button -> {
                    saveFields();
                    tab.getFilters().add(filter.copy());
                    AdvancedChatTabsClient.get().config().requestSave();
                    close();
                }).dimensions(x, 304, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.preview_sound"), button -> previewFirstSound())
                .dimensions(x + 160, 304, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.save"), button -> {
                    saveFields();
                    AdvancedChatTabsClient.get().filterEngine().invalidatePatterns();
                    AdvancedChatTabsClient.get().config().requestSave();
                    close();
                }).dimensions(x, height - 44, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.delete"), button -> {
                    tab.getFilters().remove(filter);
                    AdvancedChatTabsClient.get().config().requestSave();
                    close();
                }).dimensions(x + 106, height - 44, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.cancel"), button -> close())
                .dimensions(x + 212, height - 44, 98, 20).build());
    }

    private TextFieldWidget field(int x, int y, String value, Text hint) {
        TextFieldWidget field = new TextFieldWidget(textRenderer, x, y, 310, 20, hint);
        field.setMaxLength(512);
        field.setText(value == null ? "" : value);
        addDrawableChild(field);
        return field;
    }

    private void addToggle(int x, int y, String key, boolean value, Runnable action) {
        Text label = Text.translatable(key).append(Text.literal(": ")).append(Text.translatable(
                value ? "button.advanced_chat_tabs.enabled" : "button.advanced_chat_tabs.disabled"));
        addDrawableChild(ButtonWidget.builder(label, button -> {
                    saveFields();
                    action.run();
                    init();
                }).dimensions(x, y, 310, 20).build());
    }

    private void addValue(int x, int y, String key, String value, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.translatable(key).append(Text.literal(": " + value)), button -> {
                    saveFields();
                    action.run();
                    init();
                }).dimensions(x, y, 310, 20).build());
    }

    private void saveFields() {
        if (name != null) filter.setName(name.getText());
        if (include != null) filter.setIncludeConditions(split(include.getText()));
        if (exclude != null) filter.setExcludeConditions(split(exclude.getText()));
    }

    private List<String> split(String value) {
        return Arrays.stream(value.split("[;\\n]"))
                .map(String::trim).filter(item -> !item.isEmpty()).toList();
    }

    private void test() {
        saveFields();
        FilterTestResult result = AdvancedChatTabsClient.get().filterEngine().test(filter, ChatMessage.plain(testMessage.getText()));
        if (!result.error().isBlank()) {
            testResult = Text.translatable("label.advanced_chat_tabs.filter_result.invalid_regex").getString() + ": " + result.error();
            testColor = 0xFFFF5555;
        } else if (result.matched()) {
            testResult = Text.translatable("label.advanced_chat_tabs.filter_result.match").getString()
                    + "; " + Text.translatable("label.advanced_chat_tabs.condition").getString() + ": " + result.matchedCondition()
                    + "; " + Text.translatable("label.advanced_chat_tabs.actions").getString() + ": "
                    + result.actions().stream().map(action -> String.valueOf(action.getType())).toList();
            testColor = 0xFF55FF55;
        } else {
            testResult = Text.translatable("label.advanced_chat_tabs.filter_result.no_match").getString()
                    + (result.excludedBy().isBlank() ? "" : "; " + Text.translatable("label.advanced_chat_tabs.excluded_by").getString() + ": " + result.excludedBy());
            testColor = 0xFFFFAA55;
        }
    }

    private String messageTypeLabel() {
        return filter.getMessageTypes().size() == ChatMessageType.values().length
                ? Text.translatable("value.advanced_chat_tabs.all").getString()
                : filter.getMessageTypes().stream().findFirst().map(Enum::name).orElse(Text.translatable("value.advanced_chat_tabs.none").getString());
    }

    private void cycleMessageType() {
        if (filter.getMessageTypes().size() == ChatMessageType.values().length) {
            filter.setMessageTypes(EnumSet.of(ChatMessageType.PLAYER));
            return;
        }
        ChatMessageType current = filter.getMessageTypes().stream().findFirst().orElse(ChatMessageType.PLAYER);
        int index = Arrays.asList(ChatMessageType.values()).indexOf(current);
        if (index + 1 >= ChatMessageType.values().length) {
            filter.setMessageTypes(EnumSet.allOf(ChatMessageType.class));
        } else {
            filter.setMessageTypes(EnumSet.of(ChatMessageType.values()[index + 1]));
        }
    }

    private String trustLabel() {
        return filter.getTrustStatuses().size() == TrustStatus.values().length
                ? Text.translatable("value.advanced_chat_tabs.all").getString()
                : filter.getTrustStatuses().stream().findFirst().map(Enum::name).orElse(Text.translatable("value.advanced_chat_tabs.none").getString());
    }

    private void cycleTrustStatus() {
        if (filter.getTrustStatuses().size() == TrustStatus.values().length) {
            filter.setTrustStatuses(EnumSet.of(TrustStatus.VERIFIED));
            return;
        }
        TrustStatus current = filter.getTrustStatuses().stream().findFirst().orElse(TrustStatus.VERIFIED);
        int index = Arrays.asList(TrustStatus.values()).indexOf(current);
        if (index + 1 >= TrustStatus.values().length) {
            filter.setTrustStatuses(EnumSet.allOf(TrustStatus.class));
        } else {
            filter.setTrustStatuses(EnumSet.of(TrustStatus.values()[index + 1]));
        }
    }

    private void previewFirstSound() {
        filter.getActions().stream()
                .filter(action -> action.getType() == com.example.advancedchattabs.filter.FilterActionType.PLAY_SOUND)
                .findFirst()
                .ifPresent(action -> AdvancedChatTabsClient.get().tabs().playSound(action));
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
        if (!testResult.isBlank()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(testResult), width / 2, 330, testColor);
        }
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

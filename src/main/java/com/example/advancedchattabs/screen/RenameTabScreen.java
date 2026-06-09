package com.example.advancedchattabs.screen;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import com.example.advancedchattabs.tab.ChatTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class RenameTabScreen extends Screen {
    private final Screen parent;
    private final ChatTab tab;
    private TextFieldWidget nameField;

    public RenameTabScreen(Screen parent, ChatTab tab) {
        super(Text.translatable("button.advanced_chat_tabs.rename"));
        this.parent = parent;
        this.tab = tab;
    }

    @Override
    protected void init() {
        nameField = new TextFieldWidget(textRenderer, width / 2 - 120, height / 2 - 20, 240, 20, Text.translatable("button.advanced_chat_tabs.rename"));
        nameField.setMaxLength(64);
        nameField.setText(tab.getName());
        addDrawableChild(nameField);
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.save"), button -> save())
                .dimensions(width / 2 - 120, height / 2 + 10, 116, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.translatable("button.advanced_chat_tabs.cancel"), button -> close())
                .dimensions(width / 2 + 4, height / 2 + 10, 116, 20).build());
        setInitialFocus(nameField);
    }

    private void save() {
        tab.setName(nameField.getText());
        AdvancedChatTabsClient.get().config().requestSave();
        close();
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0xB0101010);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 48, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, deltaTicks);
    }
}

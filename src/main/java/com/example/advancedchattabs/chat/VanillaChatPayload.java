package com.example.advancedchattabs.chat;

import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;

public record VanillaChatPayload(
        Text text,
        MessageSignatureData signature,
        MessageIndicator indicator,
        Integer backgroundColor,
        Float backgroundAlpha
) {
    public VanillaChatPayload(Text text, MessageSignatureData signature, MessageIndicator indicator) {
        this(text, signature, indicator, null, null);
    }
}

package com.example.advancedchattabs.mixin;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    /**
     * The three-argument overload is the final vanilla insertion point. Routing here preserves
     * the original Text tree, signature and MessageIndicator and avoids touching network packets.
     */
    @Inject(
            method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void advancedChatTabs$routeMessage(
            Text message,
            MessageSignatureData signatureData,
            MessageIndicator indicator,
            CallbackInfo ci
    ) {
        if (AdvancedChatTabsClient.get().tabs().interceptAdd((ChatHud) (Object) this, message, signatureData, indicator)) {
            ci.cancel();
        }
    }

    /**
     * Vanilla clear is replayed only for tabs without protection. Other packets and messages are
     * untouched, so this is deliberately narrower than intercepting the network handler.
     */
    @Inject(method = "clear(Z)V", at = @At("HEAD"), cancellable = true)
    private void advancedChatTabs$protectHistory(boolean clearHistory, CallbackInfo ci) {
        if (AdvancedChatTabsClient.get().tabs().interceptClear((ChatHud) (Object) this, clearHistory)) {
            ci.cancel();
        }
    }
}

package com.example.advancedchattabs.mixin;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /**
     * Action-bar messages do not pass through ChatHud. This narrow hook mirrors them into the
     * routing pipeline while leaving vanilla rendering intact unless a global hide action matches.
     */
    @Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
    private void advancedChatTabs$routeActionBar(Text message, boolean tinted, CallbackInfo ci) {
        if (AdvancedChatTabsClient.get().tabs().interceptOverlayMessage(message)) {
            ci.cancel();
        }
    }
}

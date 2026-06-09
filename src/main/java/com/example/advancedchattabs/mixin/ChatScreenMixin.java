package com.example.advancedchattabs.mixin;

import com.example.advancedchattabs.AdvancedChatTabsClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("TAIL"))
    private void advancedChatTabs$renderTabs(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        AdvancedChatTabsClient.get().overlay().renderChatScreen(context);
    }

    @Inject(method = "mouseClicked(Lnet/minecraft/client/gui/Click;Z)Z", at = @At("HEAD"), cancellable = true)
    private void advancedChatTabs$clickTab(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (AdvancedChatTabsClient.get().tabBar().handleClick(click.x(), click.y(), click.button())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
    private void advancedChatTabs$scrollTabs(double mouseX, double mouseY, double horizontal, double vertical, CallbackInfoReturnable<Boolean> cir) {
        if (AdvancedChatTabsClient.get().tabBar().handleScroll(mouseX, mouseY, vertical)) {
            cir.setReturnValue(true);
        }
    }
}

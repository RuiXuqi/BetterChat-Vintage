package com.llamalad7.betterchat.mixin.stellarcore;

import com.llamalad7.betterchat.BetterChat;
import github.kasuminova.stellarcore.client.handler.ClientEventHandler;
import github.kasuminova.stellarcore.client.hudcaching.HUDCaching;
import github.kasuminova.stellarcore.common.config.StellarCoreConfig;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientEventHandler.class, remap = false)
public class ClientEventHandlerMixin {
    @Inject(method = "handleBetterChatAnim", at = @At("HEAD"), cancellable = true)
    private static void fixCrash(CallbackInfo ci) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.gameSettings.hideGUI && BetterChat.percentComplete < 1.0F && StellarCoreConfig.PERFORMANCE.vanilla.hudCaching) {
            HUDCaching.dirty = true;
        }
        ci.cancel();
    }
}

package com.llamalad7.betterchat.mixin;

import com.llamalad7.betterchat.BetterChat;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(
            at = @At("HEAD"),
            method = "debugFeedbackTranslated"
    )
    public void debugWarnReset(CallbackInfo callbackInfo) {
        BetterChat.lastSender = null;
    }
}

package com.llamalad7.betterchat.mixin.vanilla;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.mixininterface.ChatLineAccessor;
import net.minecraft.client.gui.ChatLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@SuppressWarnings("UnusedMixin")
@Mixin(ChatLine.class)
public class ChatLineMixin implements ChatLineAccessor {
    @Unique
    @Nullable
    private String chatheads$sender;

    @Inject(
            at = @At("TAIL"),
            method = "<init>"
    )
    public void init(CallbackInfo callbackInfo) {
        this.chatheads$sender = BetterChat.lastSender;
    }

    @Override
    public void chatheads$setSender(@Nullable String sender) {
        this.chatheads$sender = sender;
    }

    @Nullable
    @Override
    public String chatheads$getSender() {
        return this.chatheads$sender;
    }
}

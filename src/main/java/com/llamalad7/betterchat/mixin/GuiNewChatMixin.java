package com.llamalad7.betterchat.mixin;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.mixininterface.GuiNewChatConfigurer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(GuiNewChat.class)
public abstract class GuiNewChatMixin implements GuiNewChatConfigurer {

    @Shadow
    private boolean isScrolled;

    @Shadow
    public abstract float getChatScale();

    @Shadow
    protected abstract void setChatLine(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly);

    @Unique
    private boolean betterChat$configuring;

    @Override
    public void betterChat$setConfiguring(boolean configuring) {
        this.betterChat$configuring = configuring;
    }

    @Unique
    private static void betterChat$updatePercentage(long diff) {
        if (BetterChat.percentComplete < 1) BetterChat.percentComplete += 0.004f * diff;
        BetterChat.percentComplete = MathHelper.clamp(BetterChat.percentComplete, 0, 1);
    }

    @Unique
    private float betterChat$percent;

    @Inject(
            method = "drawChat",
            at = @At("HEAD"),
            cancellable = true
    )
    private void headInject(int updateCounter, CallbackInfo ci) {
        if (this.betterChat$configuring) {
            ci.cancel();
            return;
        }
        if (BetterChat.prevMillis == -1) {
            BetterChat.prevMillis = Minecraft.getSystemTime();
            ci.cancel();
            return;
        }
        long current = Minecraft.getSystemTime();
        long diff = current - BetterChat.prevMillis;
        BetterChat.prevMillis = current;
        betterChat$updatePercentage(diff);
        float t = BetterChat.percentComplete;
        this.betterChat$percent = 1 - (--t) * t * t * t;
        this.betterChat$percent = MathHelper.clamp(this.betterChat$percent, 0, 1);
    }

    @ModifyArgs(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
                    ordinal = 0
            )
    )
    private void modifyTransLate(@Nonnull Args args) {
        args.set(0, (float) args.get(0) + BetterChat.getSettings().xOffset);
        args.set(1, (float) args.get(1) +
                (BetterChat.getSettings().smooth && !this.isScrolled ?
                        BetterChat.getSettings().yOffset + (9 - 9 * this.betterChat$percent) * this.getChatScale() :
                        BetterChat.getSettings().yOffset));
    }

    @Inject(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;enableBlend()V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void modifyDrawColor(int updateCounter, CallbackInfo ci, int i, int j, float f, boolean flag, float f1, int k, int l, int i1, ChatLine chatline, int j1, double d0, int l1, int i2, int j2, String s) {
        if (BetterChat.getSettings().smooth && i1 <= BetterChat.newLines) {
            //noinspection UnusedAssignment
            l1 = (int) (l1 * this.betterChat$percent);
        }
    }

    @Redirect(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V",
                    ordinal = 0
            )
    )
    private void removeBackground(int left, int top, int right, int bottom, int color) {
        if (!BetterChat.getSettings().clear) {
            GuiNewChat.drawRect(left, top, right, bottom, color);
        }
    }

    @Inject(
            method = "printChatMessageWithOptionalDeletion",
            at = @At("HEAD")
    )
    private void resetPercentComplete(ITextComponent chatComponent, int chatLineId, CallbackInfo ci) {
        BetterChat.percentComplete = 0.0F;
    }

    @Inject(
            method = "setChatLine",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;getChatOpen()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void updateNewLines(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci, int i, @Nonnull List<ITextComponent> list) {
        BetterChat.newLines = list.size() - 1;
    }

    @ModifyVariable(
            method = "getChatComponent",
            at = @At("STORE"),
            ordinal = 0,
            argsOnly = true)
    private int modifyMouseX(int x) {
        return x - BetterChat.getSettings().xOffset;
    }

    @ModifyVariable(method = "getChatComponent",
            at = @At("STORE"),
            ordinal = 1,
            argsOnly = true)
    private int modifyMouseY(int y) {
        return y + BetterChat.getSettings().yOffset;
    }

    @Override
    public void betterChat$setExampleChatLines(@Nonnull List<ITextComponent> exampleChatLines) {
        for (ITextComponent chatComponent : exampleChatLines) {
            this.setChatLine(chatComponent, 0, 0, false);
        }
    }
}

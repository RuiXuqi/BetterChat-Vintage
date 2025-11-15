package com.llamalad7.betterchat.mixin.vanilla;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.ChatSettings;
import com.llamalad7.betterchat.mixininterface.ChatLineAccessor;
import com.llamalad7.betterchat.mixininterface.GuiNewChatConfigurer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
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

    @Shadow
    @Final
    private Minecraft mc;

    @Unique
    private boolean betterChat$configuring;

    @Override
    @Unique
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
        this.betterChat$lineCount = l;
    }

    @SuppressWarnings("ParameterCanBeLocal")
    @Redirect(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;drawRect(IIIII)V",
                    ordinal = 0
            )
    )
    private void modifyBackground(int left, int top, int right, int bottom, int color) {
        ChatSettings settings = BetterChat.getSettings();
        if (!settings.clear) {
            right = MathHelper.ceil(GuiNewChat.calculateChatboxWidth(this.mc.gameSettings.chatWidth) / this.getChatScale());
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

    @WrapMethod(method = "getChatComponent")
    private ITextComponent modifyMousePos(int mouseX, int mouseY, @Nonnull Operation<ITextComponent> original) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int i = scaledresolution.getScaleFactor();
        ChatSettings settings = BetterChat.getSettings();
        return original.call(mouseX - (settings.xOffset + (settings.head ? BetterChat.HEAD_OFFSET : 0)) * i, mouseY + settings.yOffset * i);
    }

    @Unique
    private ChatLine betterChat$drawingLine;

    @WrapOperation(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/ChatLine;getChatComponent()Lnet/minecraft/util/text/ITextComponent;",
                    ordinal = 0
            )
    )
    private ITextComponent getMessage(ChatLine instance, @Nonnull Operation<ITextComponent> original) {
        this.betterChat$drawingLine = instance;
        return original.call(instance);
    }

    @WrapOperation(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I",
                    ordinal = 0
            )
    )
    private int render(FontRenderer instance, String text, float x, float y, int color, Operation<Integer> original) {
        if (!BetterChat.getSettings().head) return original.call(instance, text, x, y, color);
        NetworkPlayerInfo owner = ((ChatLineAccessor) this.betterChat$drawingLine).chatheads$getSender();
        if (owner != null) {
            GlStateManager.color(1, 1, 1, (((color >> 24) + 256) % 256) / 255f);
            this.mc.getTextureManager().bindTexture(owner.getLocationSkin());
            // draw base layer
            Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 8.0F, 8, 8, 8, 8, 8, 64, 64);
            // draw hat
            Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 40.0F, 8, 8, 8, 8, 8, 64, 64);
            GlStateManager.color(1, 1, 1, 1);
        }
        return original.call(instance, text, x + BetterChat.HEAD_OFFSET, y, color);
    }

    @Inject(method = "setChatLine", at = @At("TAIL"))
    private void resetSender(ITextComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci) {
        BetterChat.lastSender = null;
    }

    @Inject(
            method = "refreshChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;setChatLine(Lnet/minecraft/util/text/ITextComponent;IIZ)V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void catchSenderBeforeRefresh(CallbackInfo ci, int i, ChatLine chatline) {
        BetterChat.lastSender = ((ChatLineAccessor) chatline).chatheads$getSender();
    }

    /**
     * Mixin the method for original wrap methods. Do not use for drawing method directly.
     */
    @Inject(
            method = "getChatWidth",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyChatWidth(CallbackInfoReturnable<Integer> cir) {
        if (!BetterChat.getSettings().head) return;
        cir.setReturnValue(cir.getReturnValue() - BetterChat.HEAD_OFFSET);
    }

    @Override
    @Unique
    public void betterChat$setExampleChatLines(@Nonnull List<ITextComponent> exampleChatLines) {
        for (ITextComponent chatComponent : exampleChatLines) {
            this.setChatLine(chatComponent, 0, 0, false);
        }
    }

    @Unique
    private int betterChat$lineCount = 0;

    @Override
    @Unique
    public int betterChat$getCurrentChatHeight() {
        return this.betterChat$lineCount;
    }
}

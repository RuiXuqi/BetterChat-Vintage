package com.llamalad7.betterchat.mixin.vanilla;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.ChatSettings;
import com.llamalad7.betterchat.mixininterface.ChatLineAccessor;
import com.llamalad7.betterchat.mixininterface.GuiNewChatConfigurer;
import com.llamalad7.betterchat.rename.GuiNewChatRenamed;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedMixin")
@Mixin(GuiNewChat.class)
public abstract class GuiNewChatMixin implements GuiNewChatConfigurer {
    @Shadow
    @Final
    private Minecraft mc;

    /**
     * isScrolled in 1.12.2.
     */
    @Shadow
    private boolean field_146251_k;

    /**
     * getChatScale() in 1.12.2.
     * Returns the chatscale from mc.gameSettings.chatScale
     */
    @Shadow
    public abstract float func_146244_h();

    /**
     * setChatLine() in 1.12.2.
     */
    @Shadow
    protected abstract void func_146237_a(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly);

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
        BetterChat.percentComplete = MathHelper.clamp_float(BetterChat.percentComplete, 0, 1);
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
        this.betterChat$percent = MathHelper.clamp_float(this.betterChat$percent, 0, 1);
    }

    @ModifyArgs(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V",
                    ordinal = 0
            ),
            remap = false
    )
    private void modifyTransLate(@Nonnull Args args) {
        args.set(0, (float) args.get(0) + BetterChat.getSettings().xOffset);
        args.set(1, (float) args.get(1) +
                (BetterChat.getSettings().smooth && !this.field_146251_k ?
                        BetterChat.getSettings().yOffset + (9 - 9 * this.betterChat$percent) * this.func_146244_h() :
                        BetterChat.getSettings().yOffset));
    }

    @Inject(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/GL11;glEnable(I)V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            remap = false
    )
    private void modifyDrawColor(int p_146230_1_, CallbackInfo ci, int j, boolean flag, int k, int l, float f, float f1, int i1, int j1, int k1, int i2, ChatLine chatline, double d0, byte b0, int j2) {
        if (BetterChat.getSettings().smooth && j1 <= BetterChat.newLines) {
            //noinspection UnusedAssignment
            i2 = (int) (i2 * this.betterChat$percent);
        }
        this.betterChat$lineCount = k;
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
            right = MathHelper.ceiling_float_int(GuiNewChatRenamed.calculateChatboxWidth(this.mc.gameSettings.chatWidth) / this.func_146244_h());
            GuiNewChat.drawRect(left - 2, top, right, bottom, color);
        }
    }

    @Inject(
            method = "printChatMessageWithOptionalDeletion",
            at = @At("HEAD")
    )
    private void resetPercentComplete(IChatComponent chatComponent, int chatLineId, CallbackInfo ci) {
        BetterChat.percentComplete = 0.0F;
    }

    @Inject(
            method = "func_146237_a",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;getChatOpen()Z",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void updateNewLines(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_, CallbackInfo ci, int k, int l, ChatComponentText chatcomponenttext, ArrayList<IChatComponent> list) {
        BetterChat.newLines = list.size() - 1;
    }

    @WrapMethod(method = "func_146236_a")
    private IChatComponent modifyMousePos(int mouseX, int mouseY, @Nonnull Operation<IChatComponent> original) {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
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
                    target = "Lnet/minecraft/client/gui/ChatLine;func_151461_a()Lnet/minecraft/util/IChatComponent;",
                    ordinal = 0
            )
    )
    private IChatComponent getMessage(ChatLine instance, @Nonnull Operation<IChatComponent> original) {
        this.betterChat$drawingLine = instance;
        return original.call(instance);
    }

    //TODO: Fix head render
    @WrapOperation(
            method = "drawChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;III)I",
                    ordinal = 0
            )
    )
    private int render(FontRenderer instance, String text, int x, int y, int color, Operation<Integer> original) {
        if (!BetterChat.getSettings().head) return original.call(instance, text, x, y, color);
        String owner = ((ChatLineAccessor) this.betterChat$drawingLine).chatheads$getSender();
        if (owner != null) {
            GL11.glColor4f(1, 1, 1, (((color >> 24) + 256) % 256) / 255f);
            ResourceLocation skin = AbstractClientPlayer.getLocationSkin(owner);
            AbstractClientPlayer.getDownloadImageSkin(skin, owner);
            this.mc.getTextureManager().bindTexture(skin);
            if (BetterChat.getSettings().forceNewSkinCompat || BetterChat.simpleSkinBackportLoaded) {
                // draw base layer
                Gui.func_152125_a(x, y, 8.0F, 8, 8, 8, 8, 8, 64, 64);
                // draw hat
                Gui.func_152125_a(x, y, 40.0F, 8, 8, 8, 8, 8, 64, 64);
            } else {
                // draw base layer
                Gui.func_152125_a(x, y, 8.0F, 8, 8, 8, 8, 8, 64, 32);
            }
            GL11.glColor4f(1, 1, 1, 1);
        }
        return original.call(instance, text, x + BetterChat.HEAD_OFFSET, y, color);
    }

    @Inject(method = "func_146237_a", at = @At("TAIL"))
    private void resetSender(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly, CallbackInfo ci) {
        BetterChat.lastSender = null;
    }

    @Inject(
            method = "refreshChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiNewChat;func_146237_a(Lnet/minecraft/util/IChatComponent;IIZ)V"
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
            method = "func_146228_f",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyChatWidth(CallbackInfoReturnable<Integer> cir) {
        if (!BetterChat.getSettings().head) return;
        cir.setReturnValue(cir.getReturnValue() - BetterChat.HEAD_OFFSET);
    }

    @Override
    @Unique
    public void betterChat$setExampleChatLines(@Nonnull List<IChatComponent> exampleChatLines) {
        for (IChatComponent chatComponent : exampleChatLines) {
            this.func_146237_a(chatComponent, 0, 0, false);
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

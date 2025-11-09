package com.llamalad7.betterchat.gui;

import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.ChatSettings;
import com.llamalad7.betterchat.mixininterface.GuiNewChatConfigurer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends GuiScreen implements GuiSlider.ISlider {
    private static final List<ITextComponent> EXAMPLE_CHAT = new ArrayList<>();
    private final ChatSettings settings;
    private boolean dragging = false;
    private int dragStartX, dragStartY;
    private GuiNewChat dummyChatGUI;
    private GuiButton clearButton, smoothButton;
    private GuiSlider scaleSlider, widthSlider;

    static {
        EXAMPLE_CHAT.add(new TextComponentTranslation("gui.betterchat.text.example3"));
        EXAMPLE_CHAT.add(new TextComponentTranslation("gui.betterchat.text.example2"));
        EXAMPLE_CHAT.add(new TextComponentTranslation("gui.betterchat.text.example1"));
    }

    public GuiConfig() {
        this.settings = BetterChat.getSettings();
    }

    @Override
    public void initGui() {
        final GuiIngame ingame = Minecraft.getMinecraft().ingameGUI;
        if (ingame != null) {
            ((GuiNewChatConfigurer) ingame.getChatGUI()).betterChat$setConfiguring(true);
        }
        this.dummyChatGUI = new GuiNewChat(this.mc);
        ((GuiNewChatConfigurer) this.dummyChatGUI).betterChat$setExampleChatLines(EXAMPLE_CHAT);
        clearButton = addButton(new GuiButton(0, width / 2 - 120, height / 2 - 50, 240, 20, getPropName("clear") + " " + getColoredBool("clear", settings.clear)));
        smoothButton = addButton(new GuiButton(1, width / 2 - 120, height / 2 - 25, 240, 20, getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth)));
        scaleSlider = addButton(new GuiSlider(3, width / 2 - 120, height / 2, 240, 20, getPropName("scale") + " ", "%", 0, 100, this.mc.gameSettings.chatScale * 100, false, true, this));
        widthSlider = addButton(new GuiSlider(4, width / 2 - 120, height / 2 + 25, 240, 20, getPropName("width") + " ", "px", 40, 320, GuiNewChat.calculateChatboxWidth(this.mc.gameSettings.chatWidth), false, true, this));
        addButton(new GuiButton(2, width / 2 - 120, height / 2 + 50, 240, 20, getPropName("reset")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.mc.fontRenderer, I18n.format("gui.betterchat.text.title", TextFormatting.GREEN + TextFormatting.BOLD.toString() + "Better Chat" + TextFormatting.RESET, TextFormatting.AQUA + TextFormatting.BOLD.toString() + "LlamaLad7"), width / 2, height / 2 - 75, 0xFFFFFF);
        drawCenteredString(this.mc.fontRenderer, I18n.format("gui.betterchat.text.drag"), this.width / 2, this.height / 2 - 63, 0xFFFFFF);
        if (dragging) {
            settings.xOffset += mouseX - dragStartX;
            settings.yOffset += mouseY - dragStartY;
            dragStartX = mouseX;
            dragStartY = mouseY;
        }
        this.mc.gameSettings.chatScale = (float) scaleSlider.getValueInt() / 100;
        this.mc.gameSettings.chatWidth = ((float) widthSlider.getValueInt() - 40) / 280;

        GlStateManager.pushMatrix();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        GlStateManager.translate(0.0F, (float) (scaledresolution.getScaledHeight() - 48), 0.0F);
        this.dummyChatGUI.drawChat(0);
        GlStateManager.popMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int left = settings.xOffset;
            int right = left + this.dummyChatGUI.getChatWidth() + 4;
            int bottom = 8 + settings.yOffset + new ScaledResolution(this.mc).getScaledHeight() - 48;
            int top = bottom - EXAMPLE_CHAT.size() * 9;
            if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
                dragging = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
            }
        }

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        dragging = false;
    }

    @Override
    public void onGuiClosed() {
        settings.saveConfig();
        final GuiIngame ingame = Minecraft.getMinecraft().ingameGUI;
        if (ingame != null) {
            ((GuiNewChatConfigurer) ingame.getChatGUI()).betterChat$setConfiguring(false);
        }
        this.mc.gameSettings.saveOptions();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                settings.clear = !settings.clear;
                button.displayString = getPropName("clear") + " " + getColoredBool("clear", settings.clear);
                break;
            case 1:
                settings.smooth = !settings.smooth;
                button.displayString = getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth);
                break;
            case 2:
                settings.resetConfig();
                clearButton.displayString = getPropName("clear") + " " + getColoredBool("clear", settings.clear);
                smoothButton.displayString = getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth);
                scaleSlider.setValue(this.mc.gameSettings.chatScale * 100);
                scaleSlider.updateSlider();
                widthSlider.setValue(GuiNewChat.calculateChatboxWidth(this.mc.gameSettings.chatWidth));
                widthSlider.updateSlider();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.dummyChatGUI.scroll(Mouse.getEventDWheel());
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        this.dummyChatGUI.refreshChat();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private String getColoredBool(String prop, boolean bool) {
        if (bool) {
            return TextFormatting.GREEN + I18n.format("gui.betterchat.text." + prop + ".enabled");
        }

        return TextFormatting.RED + I18n.format("gui.betterchat.text." + prop + ".disabled");
    }

    private String getPropName(String prop) {
        return I18n.format("gui.betterchat.text." + prop + ".name");
    }

    @Override
    public void drawWorldBackground(int tint) {
        if (this.mc.world == null) {
            this.drawBackground(tint);
        }
    }
}

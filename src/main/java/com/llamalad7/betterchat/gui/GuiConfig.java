package com.llamalad7.betterchat.gui;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.llamalad7.betterchat.BetterChat;
import com.llamalad7.betterchat.ChatSettings;
import com.llamalad7.betterchat.mixininterface.GuiNewChatConfigurer;
import com.llamalad7.betterchat.rename.GuiNewChatRenamed;
import cpw.mods.fml.client.config.GuiSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiConfig extends GuiScreen implements GuiSlider.ISlider {
    private static final List<IChatComponent> EXAMPLE_CHAT = new ArrayList<>();
    private final ChatSettings settings;
    private boolean dragging = false;
    private int dragStartX, dragStartY;
    private GuiNewChat dummyChatGUI;
    private GuiButton clearButton, smoothButton, headButton;
    private GuiSlider scaleSlider, widthSlider;

    static {
        IChatComponent text = new ChatComponentTranslation("gui.betterchat.text.example3");
        text.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text));
        EXAMPLE_CHAT.add(text);
        EXAMPLE_CHAT.add(new ChatComponentTranslation("gui.betterchat.text.example2"));
        EXAMPLE_CHAT.add(new ChatComponentTranslation("gui.betterchat.text.example1"));
    }

    @SuppressWarnings("unused")
    public GuiConfig(GuiScreen parent) {
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
        clearButton = addButton(new GuiButton(0, width / 2 - 120, height / 2 - 25, 240, 20, getPropName("clear") + " " + getColoredBool("clear", settings.clear)));
        smoothButton = addButton(new GuiButton(1, width / 2 - 120, height / 2, 240, 20, getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth)));
        headButton = addButton(new GuiButton(5, width / 2 - 120, height / 2 - 50, 240, 20, getPropName("head") + " " + getColoredBool("head", settings.head)));
        scaleSlider = addButton(new GuiSlider(3, width / 2 - 120, height / 2 + 25, 240, 20, getPropName("scale") + " ", "%", 0, 100, this.mc.gameSettings.chatScale * 100, false, true, this));
        widthSlider = addButton(new GuiSlider(4, width / 2 - 120, height / 2 + 50, 240, 20, getPropName("width") + " ", "px", 40, 320, GuiNewChatRenamed.calculateChatboxWidth(this.mc.gameSettings.chatWidth), false, true, this));
        addButton(new GuiButton(2, width / 2 - 120, height / 2 + 75, 240, 20, getPropName("reset")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.mc.fontRenderer, I18n.format("gui.betterchat.text.title", EnumChatFormatting.GREEN + EnumChatFormatting.BOLD.toString() + "Better Chat" + EnumChatFormatting.RESET, EnumChatFormatting.AQUA + EnumChatFormatting.BOLD.toString() + "LlamaLad7"), width / 2, height / 2 - 75, 0xFFFFFF);
        drawCenteredString(this.mc.fontRenderer, I18n.format("gui.betterchat.text.drag"), this.width / 2, this.height / 2 - 63, 0xFFFFFF);
        if (dragging) {
            settings.xOffset += mouseX - dragStartX;
            settings.yOffset += mouseY - dragStartY;
            dragStartX = mouseX;
            dragStartY = mouseY;
        }
        GL11.glPushMatrix();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        GL11.glTranslatef(0.0F, (float) (scaledresolution.getScaledHeight() - 48), 0.0F);
        this.dummyChatGUI.drawChat(0);
        GL11.glPopMatrix();

        IChatComponent IChatComponent = this.dummyChatGUI.func_146236_a(Mouse.getX(), Mouse.getY());
        if (IChatComponent != null) {
            this.handleComponentHover(IChatComponent, mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int left = settings.xOffset;
            int right = left + GuiNewChatRenamed.calculateChatboxWidth(this.mc.gameSettings.chatWidth) + 4;
            int bottom = 8 + settings.yOffset + new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight).getScaledHeight() - 48;
            int top = bottom - ((GuiNewChatConfigurer) this.dummyChatGUI).betterChat$getCurrentChatHeight() * 9;
            if (mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom) {
                dragging = true;
                dragStartX = mouseX;
                dragStartY = mouseY;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        dragging = false;
    }

    @Override
    public void onGuiClosed() {
        settings.saveConfig();
        final GuiIngame ingame = Minecraft.getMinecraft().ingameGUI;
        if (ingame != null) {
            ingame.getChatGUI().refreshChat();
            ((GuiNewChatConfigurer) ingame.getChatGUI()).betterChat$setConfiguring(false);
        }
        this.mc.gameSettings.saveOptions();
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) {
        switch (button.id) {
            case 0:
                settings.clear = !settings.clear;
                button.displayString = getPropName("clear") + " " + getColoredBool("clear", settings.clear);
                break;
            case 1:
                settings.smooth = !settings.smooth;
                button.displayString = getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth);
                break;
            case 5:
                settings.head = !settings.head;
                button.displayString = getPropName("head") + " " + getColoredBool("head", settings.head);
                this.dummyChatGUI.refreshChat();
                break;
            case 2:
                settings.resetConfig();
                clearButton.displayString = getPropName("clear") + " " + getColoredBool("clear", settings.clear);
                smoothButton.displayString = getPropName("smooth") + " " + getColoredBool("smooth", settings.smooth);
                headButton.displayString = getPropName("head") + " " + getColoredBool("head", settings.head);
                scaleSlider.setValue(this.mc.gameSettings.chatScale * 100);
                scaleSlider.updateSlider();
                widthSlider.setValue(GuiNewChatRenamed.calculateChatboxWidth(this.mc.gameSettings.chatWidth));
                widthSlider.updateSlider();
                break;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        this.dummyChatGUI.scroll(Mouse.getEventDWheel());
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        this.dummyChatGUI.refreshChat();
        this.mc.gameSettings.chatScale = (float) scaleSlider.getValueInt() / 100;
        this.mc.gameSettings.chatWidth = ((float) widthSlider.getValueInt() - 40) / 280;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawWorldBackground(int tint) {
        if (this.mc.theWorld == null) {
            this.drawBackground(tint);
        }
    }

    private static @Nonnull String getColoredBool(String prop, boolean bool) {
        return bool ? EnumChatFormatting.GREEN + I18n.format("gui.betterchat.text." + prop + ".enabled") :
            EnumChatFormatting.RED + I18n.format("gui.betterchat.text." + prop + ".disabled");
    }

    private static @Nonnull String getPropName(String prop) {
        return I18n.format("gui.betterchat.text." + prop + ".name");
    }

    protected <T extends GuiButton> T addButton(T buttonIn) {
        this.buttonList.add(buttonIn);
        return buttonIn;
    }

    /**
     * Copied from {@link GuiChat#drawScreen(int, int, float)}.
     */
    protected void handleComponentHover(IChatComponent component, int x, int y) {
        if (component != null && component.getChatStyle().getChatHoverEvent() != null) {
            HoverEvent hoverevent = component.getChatStyle().getChatHoverEvent();

            if (hoverevent.getAction() == HoverEvent.Action.SHOW_ITEM) {
                ItemStack itemstack = null;

                try {
                    NBTBase nbtbase = JsonToNBT.func_150315_a(hoverevent.getValue().getUnformattedText());
                    if (nbtbase instanceof NBTTagCompound) {
                        itemstack = ItemStack.loadItemStackFromNBT((NBTTagCompound) nbtbase);
                    }
                } catch (NBTException ignored) {
                }

                if (itemstack != null) {
                    this.renderToolTip(itemstack, x, y);
                } else {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid Item!", x, y);
                }
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_TEXT) {
                this.func_146283_a(Splitter.on("\n").splitToList(hoverevent.getValue().getFormattedText()), x, y);
            } else if (hoverevent.getAction() == HoverEvent.Action.SHOW_ACHIEVEMENT) {
                StatBase statbase = StatList.func_151177_a(hoverevent.getValue().getUnformattedText());

                if (statbase != null) {
                    IChatComponent ichatcomponent1 = statbase.func_150951_e();
                    ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("stats.tooltip.type." + (statbase.isAchievement() ? "achievement" : "statistic"));
                    chatcomponenttranslation.getChatStyle().setItalic(Boolean.TRUE);
                    String s = statbase instanceof Achievement ? ((Achievement) statbase).getDescription() : null;
                    ArrayList<String> arraylist = Lists.newArrayList(ichatcomponent1.getFormattedText(), chatcomponenttranslation.getFormattedText());

                    if (s != null) {
                        arraylist.addAll(this.fontRendererObj.listFormattedStringToWidth(s, 150));
                    }

                    this.func_146283_a(arraylist, x, y);
                } else {
                    this.drawCreativeTabHoveringText(EnumChatFormatting.RED + "Invalid statistic/achievement!", x, y);
                }
            }

            GL11.glDisable(GL11.GL_LIGHTING);
        }
    }
}

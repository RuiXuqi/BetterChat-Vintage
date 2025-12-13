package com.llamalad7.betterchat.rename;

import net.minecraft.client.gui.GuiNewChat;

public class GuiNewChatRenamed {
    public static int calculateChatboxWidth(float scale) {
        return GuiNewChat.func_146233_a(scale);
    }

    public static int calculateChatboxHeight(float scale) {
        return GuiNewChat.func_146243_b(scale);
    }
}

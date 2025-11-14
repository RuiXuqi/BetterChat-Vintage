package com.llamalad7.betterchat.mixininterface;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public interface GuiNewChatConfigurer {
    void betterChat$setConfiguring(boolean configuring);

    void betterChat$setExampleChatLines(@Nonnull List<ITextComponent> exampleChatLines);

    int betterChat$getCurrentChatHeight();
}

package com.llamalad7.betterchat;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nonnull;

public class CommonProxy {
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        BetterChat.LOG.warn("Better Chat is a client side only mod. Stop loading...");
    }

    public void init(@Nonnull FMLInitializationEvent event) {
    }
}

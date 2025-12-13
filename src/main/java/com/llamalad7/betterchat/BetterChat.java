package com.llamalad7.betterchat;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(
    modid = BetterChat.MOD_ID,
    name = BetterChat.MOD_NAME,
    version = Tags.VERSION,
    acceptableSaveVersions = "*",
    dependencies = "required-after:unimixins",
    guiFactory = "com.llamalad7.betterchat.BetterChatConfigGuiFactory"
)
@SideOnly(Side.CLIENT)
public class BetterChat {
    public static final String MOD_ID = "betterchat";
    public static final String MOD_NAME = "Better Chat";
    public static final Logger LOG = LogManager.getLogger(MOD_NAME);
    public static String lastSender;
    public static final int HEAD_OFFSET = 10;

    private static ChatSettings settings;
    public static float percentComplete = 0.0F;
    public static int newLines;
    public static long prevMillis = -1;
    public static boolean simpleSkinBackportLoaded = false;

    @SidedProxy(clientSide = "com.llamalad7.betterchat.ClientProxy", serverSide = "com.llamalad7.betterchat.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        proxy.init(event);
    }

    public static void setSettings(ChatSettings settings) {
        BetterChat.settings = settings;
    }

    public static ChatSettings getSettings() {
        return settings;
    }
}

package com.llamalad7.betterchat;

import com.llamalad7.betterchat.command.CommandConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, clientSideOnly = true, acceptableSaveVersions = "*",
        dependencies = "required-after:mixinbooter@[8.0,)", guiFactory = "com.llamalad7.betterchat.BetterChatConfigGuiFactory"
)
@SideOnly(Side.CLIENT)
public class BetterChat {
    private static ChatSettings settings;
    public static float percentComplete = 0.0F;
    public static int newLines;
    public static long prevMillis = -1;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        settings = new ChatSettings(new Configuration(event.getSuggestedConfigurationFile()));
        settings.loadConfig();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandConfig());
    }

    public static ChatSettings getSettings() {
        return settings;
    }
}

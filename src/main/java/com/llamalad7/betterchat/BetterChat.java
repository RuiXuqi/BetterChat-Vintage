package com.llamalad7.betterchat;

import com.llamalad7.betterchat.command.CommandConfig;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, clientSideOnly = true, acceptableSaveVersions = "*",
        dependencies = "required-after:mixinbooter@[8.0,)", guiFactory = "com.llamalad7.betterchat.BetterChatConfigGuiFactory"
)
@SideOnly(Side.CLIENT)
public class BetterChat {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME);
    public static NetworkPlayerInfo lastSender;
    public static final int HEAD_OFFSET = 10;

    private static ChatSettings settings;
    public static float percentComplete = 0.0F;
    public static int newLines;
    public static long prevMillis = -1;

    @EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        settings = new ChatSettings(new Configuration(event.getSuggestedConfigurationFile()));
        settings.loadConfig();
    }

    @EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandConfig());
    }

    public static ChatSettings getSettings() {
        return settings;
    }
}

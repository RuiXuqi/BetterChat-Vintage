package com.llamalad7.betterchat;

import com.llamalad7.betterchat.command.CommandConfig;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        BetterChat.setSettings(new ChatSettings(new Configuration(event.getSuggestedConfigurationFile())));
        BetterChat.getSettings().loadConfig();
    }

    @Override
    public void init(@Nonnull FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new CommandConfig());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        BetterChat.simpleSkinBackportLoaded = Loader.isModLoaded("simpleskinbackport");
    }
}

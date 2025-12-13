package com.llamalad7.betterchat.command;

import com.llamalad7.betterchat.gui.GuiConfig;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

//TODO: Make it work
public class CommandConfig extends CommandBase {

    @Nonnull
    @Override
    public String getCommandName() {
        return "betterchat";
    }

    @Nonnull
    @Override
    public String getCommandUsage(@Nonnull ICommandSender sender) {
        return "/betterchat";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(@Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        MinecraftForge.EVENT_BUS.unregister(this);
        Minecraft.getMinecraft().displayGuiScreen(new GuiConfig(null));
    }
}

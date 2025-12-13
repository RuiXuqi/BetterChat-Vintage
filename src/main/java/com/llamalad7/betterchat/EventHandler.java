package com.llamalad7.betterchat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class EventHandler {
    @SubscribeEvent
    public void onClientChat(@Nonnull ClientChatReceivedEvent event) {
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getNetHandler();
        if (connection == null) return;
        String message = event.message.getUnformattedText();
        List<GuiPlayerInfo> playerInfos = connection.playerInfoList;

        // For chat - try to split first
        for (String part : message.split("(§.)|[^\\w]")) {
            if (part.isEmpty()) continue;
            playerInfos.stream().filter(info -> Objects.equals(info.name, part)).findFirst()
                .ifPresent(info -> BetterChat.lastSender = info.name);
        }

        // For other messages - then try to match any
        playerInfos.stream().filter(info -> message.contains(info.name)).findFirst()
            .ifPresent(info -> BetterChat.lastSender = info.name);
    }
}

package com.llamalad7.betterchat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ChatType;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID)
public class EventHandler {
    @SubscribeEvent
    public static void onClientChat(@Nonnull ClientChatReceivedEvent event) {
        NetHandlerPlayClient connection = Minecraft.getMinecraft().getConnection();
        if (connection == null) return;
        String message = event.getMessage().getUnformattedText();

        // For chat
        if (event.getType() == ChatType.CHAT) {
            for (String part : message.split("(§.)|[^\\w]")) {
                if (part.isEmpty()) continue;
                NetworkPlayerInfo p = connection.getPlayerInfo(part);
                if (p != null) {
                    BetterChat.lastSender = p;
                    return;
                }
            }
        }

        // For other messages
        for (NetworkPlayerInfo p : connection.getPlayerInfoMap()) {
            String displayName = p.getGameProfile().getName();
            if (message.contains(displayName)) {
                BetterChat.lastSender = p;
                return;
            }
        }
    }
}

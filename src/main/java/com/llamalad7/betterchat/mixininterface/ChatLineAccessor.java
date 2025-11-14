package com.llamalad7.betterchat.mixininterface;

import net.minecraft.client.network.NetworkPlayerInfo;

public interface ChatLineAccessor {
    NetworkPlayerInfo chatheads$getSender();
}

package com.llamalad7.betterchat.mixininterface;

import net.minecraft.client.network.NetworkPlayerInfo;

import javax.annotation.Nullable;

public interface ChatLineAccessor {
    void chatheads$setSender(@Nullable NetworkPlayerInfo sender);

    @Nullable
    NetworkPlayerInfo chatheads$getSender();
}

package com.llamalad7.betterchat.mixininterface;

import javax.annotation.Nullable;

public interface ChatLineAccessor {
    void chatheads$setSender(@Nullable String sender);

    @Nullable
    String chatheads$getSender();
}

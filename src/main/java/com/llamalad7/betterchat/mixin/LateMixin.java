package com.llamalad7.betterchat.mixin;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class LateMixin implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Loader.isModLoaded("stellarcore") ? Collections.singletonList("mixins.betterchat.stellarcore.json") : Collections.emptyList();
    }
}

package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.fog.FogEnvironmentExtended;
import me.flashyreese.mods.sodiumextra.client.gui.FogTypeConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FogEnvironment.class)
public abstract class MixinFogEnvironment implements FogEnvironmentExtended {

    @Override
    public void sodium_extra$applyFogSettings(FogType fogType, FogData fogData, Entity entity, BlockPos blockPos, ClientLevel level, float viewDistance, DeltaTracker deltaTracker) {
        FogTypeConfig config = SodiumExtraClientMod.options().renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());

        if (!SodiumExtraClientMod.options().renderSettings.globalFog || !config.enable) {
            fogData.environmentalStart = Float.MAX_VALUE;
            fogData.environmentalEnd = Float.MAX_VALUE;
            fogData.renderDistanceStart = Float.MAX_VALUE;
            fogData.renderDistanceEnd = Float.MAX_VALUE;
            fogData.skyEnd = Float.MAX_VALUE;
            fogData.cloudEnd = Float.MAX_VALUE;
            return;
        }
        float environmentStartMultiplier = config.environmentStartMultiplier / 100.0F;
        float environmentEndMultiplier = config.environmentEndMultiplier / 100.0F;
        float renderDistanceStartMultiplier = config.renderDistanceStartMultiplier / 100.0F;
        float renderDistanceEndMultiplier = config.renderDistanceEndMultiplier / 100.0F;
        float skyEndMultiplier = config.skyEndMultiplier / 100.0F;
        float cloudEndMultiplier = config.cloudEndMultiplier / 100.0F;

        fogData.environmentalStart *= environmentStartMultiplier;
        fogData.environmentalEnd *= environmentEndMultiplier;
        fogData.renderDistanceStart *= renderDistanceStartMultiplier;
        fogData.renderDistanceEnd *= renderDistanceEndMultiplier;
        fogData.skyEnd *= skyEndMultiplier;
        fogData.cloudEnd *= cloudEndMultiplier;
    }
}

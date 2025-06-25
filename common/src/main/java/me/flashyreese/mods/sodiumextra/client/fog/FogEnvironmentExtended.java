package me.flashyreese.mods.sodiumextra.client.fog;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;

public interface FogEnvironmentExtended {
    void sodium_extra$applyFogSettings(FogType fogType, FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float viewDistance, DeltaTracker deltaTracker);
}

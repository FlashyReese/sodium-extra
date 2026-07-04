package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinAtmosphericFogEnvironment {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void postSetupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        SodiumExtraGameOptions.AtmosphericFogSettings settings = FogDistanceHelper.getAtmosphericSettings(level);
        int fogDistance = settings.distanceChunks;
        if (fogDistance == 0) {
            return;
        }

        if (FogDistanceHelper.isBossFogActive()) {
            return;
        }

        if (FogDistanceHelper.disablesFog(fogDistance)) {
            // Keep vanilla sky/cloud fog; the sky shader uses it to blend the horizon cleanly.
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            return;
        }

        float end = FogDistanceHelper.getEnd(fogDistance);
        fog.environmentalStart = FogDistanceHelper.getStart(settings);
        fog.environmentalEnd = end;
        if (settings.affectSkyFog) {
            fog.skyEnd = Math.min(end, renderDistance);
        }
        if (settings.affectCloudFog) {
            fog.cloudEnd = end;
        }
    }
}

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
        if (FogDistanceHelper.isBossFogActive()) {
            return;
        }

        if (fogDistance == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
            applyCloudFog(fog, settings);
            return;
        }

        if (FogDistanceHelper.disablesFog(fogDistance)) {
            // Keep vanilla sky fog; the sky shader uses it to blend the horizon cleanly.
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            applyCloudFog(fog, settings);
            return;
        }

        float end = FogDistanceHelper.getEnd(fogDistance);
        fog.environmentalStart = FogDistanceHelper.getStart(settings);
        fog.environmentalEnd = end;
        // Match the sky's horizon tint to the custom terrain fog so the two don't seam.
        fog.skyEnd = Math.min(end, renderDistance);
        applyCloudFog(fog, settings);
    }

    private static void applyCloudFog(FogData fog, SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        if (settings.cloudFogPercent != FogDistanceHelper.VANILLA_CLOUD_FOG_PERCENT) {
            // Vanilla already capped cloudEnd with the dimension's fog attribute; only pull it closer.
            fog.cloudEnd = Math.min(fog.cloudEnd, FogDistanceHelper.getCloudEnd(settings.cloudFogPercent));
        }
    }
}

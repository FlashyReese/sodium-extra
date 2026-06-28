package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = FogRenderer.class, priority = 1300)
public class MixinFogRenderer {

    @Shadow
    @Final
    private static List<FogEnvironment> FOG_ENVIRONMENTS;

    @Inject(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void postFogSetup(Camera camera, int renderDistance, DeltaTracker deltaTracker, float f, ClientLevel level, CallbackInfoReturnable<Vector4f> cir, float g, Vector4f vector4f, float h, FogType fogType, Entity entity, FogData fogData) {
        if (!this.sodiumExtra$isAtmosphericFogEnvironment(fogType, entity)) {
            return;
        }

        SodiumExtraGameOptions.AtmosphericFogSettings settings = FogDistanceHelper.getAtmosphericSettings(level);
        int fogDistance = settings.distanceChunks;
        if (FogDistanceHelper.isBossFogActive()) {
            return;
        }

        if (fogDistance == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
            fogData.renderDistanceStart = FogDistanceHelper.applyStartMultiplier(fogData.renderDistanceStart, settings);
            FogDistanceHelper.applyRenderDistanceShape(fogData, settings);
            return;
        }

        if (FogDistanceHelper.disablesFog(fogDistance)) {
            fogData.renderDistanceStart = Float.MAX_VALUE;
            fogData.renderDistanceEnd = Float.MAX_VALUE;
            return;
        }

        fogData.renderDistanceStart = FogDistanceHelper.getStart(settings);
        fogData.renderDistanceEnd = FogDistanceHelper.getEnd(fogDistance);
        FogDistanceHelper.applyRenderDistanceShape(fogData, settings);
    }

    @Unique
    private boolean sodiumExtra$isAtmosphericFogEnvironment(FogType fogType, Entity entity) {
        for (FogEnvironment fogEnvironment : FOG_ENVIRONMENTS) {
            if (fogEnvironment.isApplicable(fogType, entity)) {
                return fogEnvironment instanceof AtmosphericFogEnvironment;
            }
        }

        return false;
    }
}

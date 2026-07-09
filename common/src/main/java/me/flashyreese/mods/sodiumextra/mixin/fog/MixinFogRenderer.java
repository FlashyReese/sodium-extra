package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import me.flashyreese.mods.sodiumextra.client.fog.FogOverrideState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {
    @Shadow
    @Nullable
    private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity entity, float tickDelta) {
        return null;
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void sodiumExtra$applyFog(Camera camera, FogRenderer.FogMode fogMode, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        SodiumExtraGameOptions.AtmosphericFogSettings settings = FogDistanceHelper.getAtmosphericSettings(minecraft.level);
        if (FogOverrideState.isSettingUpCloudFog()) {
            sodiumExtra$applyCloudFog(settings);
            return;
        }

        Entity entity = camera.getEntity();
        FogType fluid = camera.getFluidInCamera();
        FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, tickDelta);

        if (sodiumExtra$applyProtectedGameplayFog(fluid, mobEffectFogFunction, fogMode)) {
            return;
        }

        if (fluid != FogType.NONE || mobEffectFogFunction != null || FogDistanceHelper.isBossFogActive()) {
            return;
        }

        if (fogMode == FogRenderer.FogMode.FOG_SKY) {
            sodiumExtra$applySkyFog(settings, viewDistance);
            return;
        }

        if (fogMode == FogRenderer.FogMode.FOG_TERRAIN || thickFog) {
            sodiumExtra$applyTerrainFog(settings);
        }
    }

    private static boolean sodiumExtra$applyProtectedGameplayFog(FogType fluid, @Nullable FogRenderer.MobEffectFogFunction mobEffectFogFunction, FogRenderer.FogMode fogMode) {
        if (!FogDistanceHelper.shouldModifyProtectedGameplayFog()) {
            return false;
        }

        if (fluid == FogType.LAVA) {
            sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType.LAVA, fogMode, 0.25F, 1.0F);
            return true;
        }

        if (fluid == FogType.POWDER_SNOW) {
            sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType.POWDER_SNOW, fogMode, 0.0F, 1.0F);
            return true;
        }

        if (fluid == FogType.WATER) {
            sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType.WATER, fogMode, 0.0F, 1.0F);
            return true;
        }

        if (mobEffectFogFunction == null) {
            return false;
        }

        if (MobEffects.BLINDNESS.equals(mobEffectFogFunction.getMobEffect())) {
            sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType.BLINDNESS, fogMode, 0.25F, 0.8F);
            return true;
        }

        if (MobEffects.DARKNESS.equals(mobEffectFogFunction.getMobEffect())) {
            sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType.DARKNESS, fogMode, 0.75F, 1.0F);
            return true;
        }

        return false;
    }

    private static void sodiumExtra$applyProtectedGameplayFog(FogDistanceHelper.ProtectedFogType type, FogRenderer.FogMode fogMode, float terrainStartMultiplier, float skyEndMultiplier) {
        int distanceBlocks = FogDistanceHelper.getProtectedGameplayFogDistance(type);
        if (fogMode == FogRenderer.FogMode.FOG_SKY) {
            FogDistanceHelper.applyProtectedGameplayFog(distanceBlocks, 0.0F, skyEndMultiplier);
        } else {
            FogDistanceHelper.applyProtectedGameplayFog(distanceBlocks, terrainStartMultiplier, 1.0F);
        }
    }

    private static void sodiumExtra$applySkyFog(SodiumExtraGameOptions.AtmosphericFogSettings settings, float viewDistance) {
        int fogDistance = settings.distanceChunks;
        if (fogDistance == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
            return;
        }

        if (FogDistanceHelper.disablesFog(fogDistance)) {
            // Keep vanilla sky fog; the sky shader uses it to blend the horizon cleanly.
            return;
        }

        RenderSystem.setShaderFogStart(0.0F);
        RenderSystem.setShaderFogEnd(Math.min(FogDistanceHelper.getEnd(fogDistance), viewDistance));
    }

    private static void sodiumExtra$applyTerrainFog(SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        int fogDistance = settings.distanceChunks;
        if (fogDistance == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
            float start = FogDistanceHelper.applyStartMultiplier(RenderSystem.getShaderFogStart(), settings);
            float end = RenderSystem.getShaderFogEnd();
            RenderSystem.setShaderFogStart(start);
            RenderSystem.setShaderFogEnd(end);
            FogDistanceHelper.applyRenderDistanceShape(start, end, settings);
            return;
        }

        if (FogDistanceHelper.disablesFog(fogDistance)) {
            RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            RenderSystem.setShaderFogEnd(Float.MAX_VALUE);
            return;
        }

        float start = FogDistanceHelper.getStart(settings);
        float end = FogDistanceHelper.getEnd(fogDistance);
        RenderSystem.setShaderFogStart(start);
        RenderSystem.setShaderFogEnd(end);
        FogDistanceHelper.applyRenderDistanceShape(start, end, settings);
    }

    private static void sodiumExtra$applyCloudFog(SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        if (settings.cloudFogPercent == FogDistanceHelper.VANILLA_CLOUD_FOG_PERCENT) {
            return;
        }

        RenderSystem.setShaderFogEnd(Math.min(RenderSystem.getShaderFogEnd(), FogDistanceHelper.getCloudEnd(settings.cloudFogPercent)));
    }
}

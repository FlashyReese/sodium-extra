package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.sugar.Local;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FogRenderer.class, priority = 1300)
public class MixinFogRenderer {
    @Unique
    private boolean sodiumExtra$usingAtmosphericFog;

    @Inject(method = "setupFog", at = @At("HEAD"))
    private void resetFogEnvironment(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        this.sodiumExtra$usingAtmosphericFog = false;
    }

    @Redirect(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/FogEnvironment;setupFog(Lnet/minecraft/client/renderer/fog/FogData;Lnet/minecraft/client/Camera;Lnet/minecraft/client/multiplayer/ClientLevel;FLnet/minecraft/client/DeltaTracker;)V"))
    private void captureFogEnvironment(FogEnvironment fogEnvironment, FogData fogData, Camera camera, ClientLevel level, float viewDistance, DeltaTracker deltaTracker) {
        this.sodiumExtra$usingAtmosphericFog = fogEnvironment instanceof AtmosphericFogEnvironment;
        fogEnvironment.setupFog(fogData, camera, level, viewDistance, deltaTracker);
    }

    @Inject(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", ordinal = 0, shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD))
    private void postFogSetup(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir, @Local(name = "fog") FogData fogData) {
        if (!this.sodiumExtra$usingAtmosphericFog) {
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
}

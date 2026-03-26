package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.sugar.Local;
import me.flashyreese.mods.sodiumextra.client.fog.FogEnvironmentExtended;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Inject(method = "setupFog", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F", ordinal = 0, shift = At.Shift.AFTER, opcode = Opcodes.PUTFIELD))
    public void postFogSetup(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir, @Local(name = "fogType") FogType fogType, @Local(name = "entity") Entity entity, @Local(name = "renderDistanceInBlocks") float renderDistanceInBlocks, @Local(name = "fog") FogData fogData) {
        for (FogEnvironment fogEnvironment : FOG_ENVIRONMENTS) {
            if (fogEnvironment.isApplicable(fogType, entity) && fogEnvironment instanceof FogEnvironmentExtended fogEnvironmentExtended) {
                fogEnvironmentExtended.sodium_extra$applyFogSettings(fogType, fogData, entity, camera.blockPosition(), level, renderDistanceInBlocks);
                break;
            }
        }
    }
}

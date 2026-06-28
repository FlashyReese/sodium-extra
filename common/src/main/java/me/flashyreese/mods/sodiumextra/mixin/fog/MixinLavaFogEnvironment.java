package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.LavaFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaFogEnvironment.class)
public class MixinLavaFogEnvironment {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private void postSetupFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!FogDistanceHelper.shouldModifyProtectedGameplayFog()) {
            return;
        }

        FogDistanceHelper.applyProtectedGameplayFog(
                fog,
                FogDistanceHelper.getProtectedGameplayFogDistance(FogDistanceHelper.ProtectedFogType.LAVA),
                0.25F,
                1.0F
        );
    }
}

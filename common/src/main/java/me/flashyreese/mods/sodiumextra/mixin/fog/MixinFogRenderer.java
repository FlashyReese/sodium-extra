package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {
    @Shadow
    @Nullable
    private static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity entity, float f) {
        return null;
    }

    @Inject(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogParameters;<init>(FFLcom/mojang/blaze3d/shaders/FogShape;FFFF)V", shift = At.Shift.AFTER), cancellable = true)
    private static void applyFog(Camera camera, FogRenderer.FogMode fogMode, Vector4f vector4f, float f, boolean thickFog, float tickDelta, CallbackInfoReturnable<FogParameters> cir) {
        Entity entity = camera.getEntity();
        SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.putIfAbsent(entity.level().dimensionType().effectsLocation(), 0);
        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.get(entity.level().dimensionType().effectsLocation()) : SodiumExtraClientMod.options().renderSettings.fogDistance;
        FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, tickDelta);
        if (fogDistance == 0 || mobEffectFogFunction != null) {
            return;
        }
        if (camera.getFluidInCamera() == FogType.NONE && (thickFog || fogMode == FogRenderer.FogMode.FOG_TERRAIN)) {
            float fogStart = (float) SodiumExtraClientMod.options().renderSettings.fogStart / 100;
            if (fogDistance == 33) {
                cir.setReturnValue(FogParameters.NO_FOG);
            } else {
                FogParameters ci = cir.getReturnValue();
                FogParameters newFogParameters = new FogParameters(fogDistance * 16 * fogStart, (fogDistance + 1) * 16, ci.shape(), ci.red(), ci.green(), ci.blue(), ci.alpha());
                cir.setReturnValue(newFogParameters);
            }
        }
    }
}

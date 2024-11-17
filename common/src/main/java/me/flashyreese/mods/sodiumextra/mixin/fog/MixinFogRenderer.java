package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
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
    protected static FogRenderer.MobEffectFogFunction getPriorityFogFunction(Entity entity, float f) {
        return null;
    }

    @Inject(method = "setupFog", at = @At(value = "TAIL"))
    private static void applyFog(Camera camera, FogRenderer.FogMode fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        Entity entity = camera.getEntity();
        SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.putIfAbsent(entity.level().dimensionType().effectsLocation(), 0);
        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.get(entity.level().dimensionType().effectsLocation()) : SodiumExtraClientMod.options().renderSettings.fogDistance;
        FogRenderer.MobEffectFogFunction mobEffectFogFunction = getPriorityFogFunction(entity, tickDelta);
        if (fogDistance == 0 || mobEffectFogFunction != null) {
            return;
        }
        if (camera.getFluidInCamera() == FogType.NONE && (thickFog || fogType == FogRenderer.FogMode.FOG_TERRAIN)) {
            float fogStart = (float) SodiumExtraClientMod.options().renderSettings.fogStart / 100;
            if (fogDistance == 33) {
                RenderSystem.setShaderFogColor(1f, 1f, 1f, 0f);
                //RenderSystem.setShaderFogStart(Short.MAX_VALUE - 1 * fogStart);
                //RenderSystem.setShaderFogEnd(Short.MAX_VALUE);
            } else {
                RenderSystem.setShaderFogStart(fogDistance * 16 * fogStart);
                RenderSystem.setShaderFogEnd((fogDistance + 1) * 16);
            }
        }
    }
}

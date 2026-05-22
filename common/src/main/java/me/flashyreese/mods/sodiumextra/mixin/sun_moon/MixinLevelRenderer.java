package me.flashyreese.mods.sodiumextra.mixin.sun_moon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Mutable
    @Shadow
    @Final
    private static ResourceLocation SUN_LOCATION;

    @Mutable
    @Shadow
    @Final
    private static ResourceLocation MOON_LOCATION;

    @WrapOperation(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"
            )
    )
    public float[] redirectGetFogColorOverride(DimensionSpecialEffects instance, float skyAngle, float tickDelta, Operation<float[]> original) {
        if (SodiumExtraClientMod.options().detailSettings.sun) {
            return original.call(instance, skyAngle, tickDelta);
        } else {
            return null;
        }
    }

    @Inject(
            method = "allChanged()V",
            at = @At(value = "TAIL")
    )
    private void postWorldRendererReload(CallbackInfo ci) {
        if (SodiumExtraClientMod.options().detailSettings.sun) {
            SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
        } else {
            SUN_LOCATION = ResourceLocation.fromNamespaceAndPath("sodium-extra", "textures/transparent.png");
        }
        if (SodiumExtraClientMod.options().detailSettings.moon) {
            MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
        } else {
            MOON_LOCATION = ResourceLocation.fromNamespaceAndPath("sodium-extra", "textures/transparent.png");
        }
    }
}

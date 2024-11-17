package me.flashyreese.mods.sodiumextra.mixin.sky;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.SkyRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
    @Inject(
            method = "renderSkyDisc",
            at = @At(value = "HEAD"), cancellable = true
    )
    public void redirectRenderSkyDisc(float f, float g, float h, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }

    @Inject(method = "renderEndSky", at = @At(value = "HEAD"), cancellable = true)
    public void preRenderEndSky(PoseStack stack, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.sky) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSun", at = @At(value = "HEAD"), cancellable = true)
    private void renderSun(float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.sun) {
            ci.cancel();
        }
    }

    @Inject(method = "renderMoon", at = @At(value = "HEAD"), cancellable = true)
    private void renderMoon(int i, float f, Tesselator tesselator, PoseStack poseStack, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.moon) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStars", at = @At(value = "HEAD"), cancellable = true)
    private void renderStars(FogParameters fogParameters, float f, PoseStack poseStack, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().detailSettings.stars) {
            ci.cancel();
        }
    }
}

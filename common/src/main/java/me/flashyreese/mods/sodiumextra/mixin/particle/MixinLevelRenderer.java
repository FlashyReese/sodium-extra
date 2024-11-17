package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "tickRain", at = @At(value = "HEAD"), cancellable = true)
    public void tickRainSplashing(Camera camera, CallbackInfo callbackInfo) {
        if (!(SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash)) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void renderWeather(LightTexture manager, float f, double d, double e, double g, CallbackInfo callbackInfo) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            callbackInfo.cancel();
        }
    }
}

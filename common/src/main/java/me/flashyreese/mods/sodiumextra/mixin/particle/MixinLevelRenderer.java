package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "renderSnowAndRain", at = @At(value = "HEAD"), cancellable = true)
    private void renderWeather(LightTexture manager, float f, double d, double e, double g, CallbackInfo callbackInfo) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            callbackInfo.cancel();
        }
    }

    @WrapWithCondition(method = "tickRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private boolean addRainSplashParticle(ClientLevel level, ParticleOptions particleOptions, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        return SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash;
    }
}

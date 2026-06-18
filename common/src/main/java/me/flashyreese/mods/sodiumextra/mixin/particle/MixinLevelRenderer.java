package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    private void renderWeather(Vec3 cameraPos, WeatherRenderState weatherRenderState, CallbackInfo ci) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            ci.cancel();
        }
    }

    @Redirect(method = "tickRainParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void addRainSplashParticle(ClientLevel level, ParticleOptions particleOptions, double x, double y, double z, double xd, double yd, double zd) {
        if (SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash) {
            level.addParticle(particleOptions, x, y, z, xd, yd, zd);
        }
    }
}

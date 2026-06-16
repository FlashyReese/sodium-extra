package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.server.level.ParticleStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WeatherEffectRenderer.class)
public class MixinLevelRenderer {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true, locals = LocalCapture.NO_CAPTURE)
    private void renderWeather(CallbackInfo ci) {
        if (!(SodiumExtraClientMod.options().detailSettings.rainSnow)) {
            ci.cancel();
        }
    }
}

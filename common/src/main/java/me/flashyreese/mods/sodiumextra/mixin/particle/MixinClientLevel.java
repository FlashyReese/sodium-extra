package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
    @Inject(method = "addDestroyBlockEffect", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakParticles(BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreak) {
            ci.cancel();
        }
    }

    @Inject(method = "addBreakingBlockEffect", at = @At(value = "HEAD"), cancellable = true)
    public void addBlockBreakingParticles(BlockPos blockPos, Direction direction, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.particles || !SodiumExtraClientMod.options().particleSettings.blockBreaking) {
            ci.cancel();
        }
    }

    @Redirect(method = "tickWeatherEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    public void addRainSplashParticle(ClientLevel instance, ParticleOptions particleOptions, double x, double y, double z, double xd, double yd, double zd) {
        if (SodiumExtraClientMod.options().particleSettings.particles && SodiumExtraClientMod.options().particleSettings.rainSplash) {
            instance.addParticle(particleOptions, x, y, z, xd, yd, zd);
        }
    }
}

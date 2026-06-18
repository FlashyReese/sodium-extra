package me.flashyreese.mods.sodiumextra.mixin.particle;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleEngine.class)
public class MixinParticleEngine {
    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i, CallbackInfoReturnable<Particle> cir) {
        Identifier particleTypeId = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
        if (!SodiumExtraClientMod.options().particleSettings.isParticleEnabled(particleTypeId)) {
            cir.setReturnValue(null);
        }
    }
}

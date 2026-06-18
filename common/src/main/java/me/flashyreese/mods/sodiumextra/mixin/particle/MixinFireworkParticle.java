package me.flashyreese.mods.sodiumextra.mixin.particle;

import it.unimi.dsi.fastutil.ints.IntList;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkParticles.Starter.class)
public class MixinFireworkParticle {
    @Unique
    private final Identifier fireworkIdentifier = Identifier.fromNamespaceAndPath("minecraft", "firework");

    @Inject(method = "createParticle", at = @At(value = "HEAD"), cancellable = true)
    public void addExplosionParticle(double x, double y, double z, double velocityX, double velocityY, double velocityZ, IntList colors, IntList targetColors, boolean trail, boolean flicker, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().particleSettings.isParticleEnabled(this.fireworkIdentifier)) {
            ci.cancel();
        }
    }
}

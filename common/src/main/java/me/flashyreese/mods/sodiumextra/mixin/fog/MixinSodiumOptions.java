package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.caffeinemc.mods.sodium.client.gui.SodiumOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SodiumOptions.class)
public class MixinSodiumOptions {
    @ModifyReturnValue(method = "loadFromDisk", at = @At("RETURN"))
    private static SodiumOptions sodiumExtra$loadFromDisk(SodiumOptions options) {
        // Fog Shape controls custom distance calculations in both the terrain shader and culler.
        options.performance.useSphericalRenderFog = false;
        return options;
    }
}

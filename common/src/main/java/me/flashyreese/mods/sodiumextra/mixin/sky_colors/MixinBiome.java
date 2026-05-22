package me.flashyreese.mods.sodiumextra.mixin.sky_colors;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public class MixinBiome {
    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    private void differentSkyColor(CallbackInfoReturnable<Integer> cir) {
        if (!SodiumExtraClientMod.options().detailSettings.skyColors) {
            cir.setReturnValue(7907327);
        }
    }
}
package me.flashyreese.mods.sodiumextra.mixin.sky_colors;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import net.minecraft.util.ARGB;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AtmosphericFogEnvironment.class)
public class MixinAtmosphericFogEnvironment {
    @ModifyArg(method = "getBaseColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/environment/AtmosphericFogEnvironment;applyWeatherDarken(Lorg/joml/Vector3fc;FF)Lorg/joml/Vector3fc;"), index = 0)
    public Vector3fc modifySkyColor(Vector3fc original) {
        if (!SodiumExtraClientMod.options().detailSettings.skyColors) {
            return ARGB.vector3fFromRGB24(7907327);
        }
        return original;
    }
}

package me.flashyreese.mods.sodiumextra.mixin.fog_falloff;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {
    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogParameters;<init>(FFLcom/mojang/blaze3d/shaders/FogShape;FFFF)V"), index = 0)
    private static float setupFog(float fogStart) {
        return fogStart * ((float) SodiumExtraClientMod.options().renderSettings.fogStart / 100);
    }
}

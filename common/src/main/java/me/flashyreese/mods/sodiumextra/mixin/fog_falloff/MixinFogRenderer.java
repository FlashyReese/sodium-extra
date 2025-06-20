package me.flashyreese.mods.sodiumextra.mixin.fog_falloff;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(net.minecraft.client.renderer.fog.FogRenderer.class)
public class MixinFogRenderer {
    @ModifyArg(method = "setupFog", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/fog/FogRenderer;updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V"), index = 5)
    private float setupFog(float fogStart) {
        return fogStart * ((float) SodiumExtraClientMod.options().renderSettings.fogStart / 100);
    }
}

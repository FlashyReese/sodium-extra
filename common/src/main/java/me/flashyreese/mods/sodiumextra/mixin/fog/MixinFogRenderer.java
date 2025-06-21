package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {

    @Shadow
    @Final
    public static int FOG_UBO_SIZE;
    @Shadow
    @Final
    private GpuBuffer emptyBuffer;

    @Inject(method = "getBuffer", at = @At(value = "HEAD"), cancellable = true)
    public void getBuffer(FogRenderer.FogMode fogMode, CallbackInfoReturnable<GpuBufferSlice> cir) {
        if (Minecraft.getInstance().level == null) return;

        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl
                ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.getOrDefault(Minecraft.getInstance().level.dimensionType().effectsLocation(), 0)
                : SodiumExtraClientMod.options().renderSettings.fogDistance;
        if (fogDistance == 33) {
            cir.setReturnValue(this.emptyBuffer.slice(0, FOG_UBO_SIZE));
        }
    }

    /*@ModifyVariable(method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private float overrideEnvironmentalStart(float original) {
        System.out.println("rd start: " + original);
        if (SodiumExtraClientMod.options().renderSettings.fogDistance == 0) return original;

        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl
                ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.getOrDefault(Minecraft.getInstance().level.dimensionType().effectsLocation(), 0)
                : SodiumExtraClientMod.options().renderSettings.fogDistance;
        float fogStart = SodiumExtraClientMod.options().renderSettings.fogStart / 100.0F;

        return fogDistance * 0.16F * fogStart;
    }

    @ModifyVariable(method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
            at = @At("HEAD"), ordinal = 3, argsOnly = true)
    private float overrideEnvironmentalEnd(float original) {
        System.out.println("rd end: " + original);
        if (SodiumExtraClientMod.options().renderSettings.fogDistance == 0) return original;

        int fogDistance = SodiumExtraClientMod.options().renderSettings.multiDimensionFogControl
                ? SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.getOrDefault(Minecraft.getInstance().level.dimensionType().effectsLocation(), 0)
                : SodiumExtraClientMod.options().renderSettings.fogDistance;
        return (fogDistance + 1) * 0.16F;
    }*/
}

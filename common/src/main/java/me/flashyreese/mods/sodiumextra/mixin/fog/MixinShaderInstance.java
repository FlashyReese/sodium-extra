package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShaderInstance.class)
public class MixinShaderInstance {
    @ModifyExpressionValue(method = "setDefaultUniforms", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShaderFogStart()F"))
    private float sodiumExtra$decodeFogStart(float start) {
        return FogDistanceHelper.decodeRenderDistanceStart(start, RenderSystem.getShaderFogEnd());
    }

    @ModifyExpressionValue(method = "setDefaultUniforms", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;getShaderFogEnd()F"))
    private float sodiumExtra$decodeFogEnd(float end) {
        return FogDistanceHelper.decodeRenderDistanceEnd(RenderSystem.getShaderFogStart(), end);
    }
}

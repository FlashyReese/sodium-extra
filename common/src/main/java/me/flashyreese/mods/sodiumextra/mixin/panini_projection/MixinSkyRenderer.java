package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkyRenderer.class)
public class MixinSkyRenderer {
    // Construction can happen during a supersampled frame. Fetch the current
    // target rather than keeping a cached target after a quality change/resize.
    // Every sky draw method reads the cached field, so wrap all of them.
    @WrapOperation(method = "*", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/SkyRenderer;renderTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldTarget(SkyRenderer instance, Operation<RenderTarget> original) {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }
}

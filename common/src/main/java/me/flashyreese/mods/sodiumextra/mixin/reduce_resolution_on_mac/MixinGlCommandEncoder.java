package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.textures.GpuTextureView;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class MixinGlCommandEncoder {
    @ModifyArgs(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"))
    private void scalePresentedTexture(Args args, GpuTextureView textureView, int swapchainWidth, int swapchainHeight) {
        int sourceWidth = args.get(4);
        int sourceHeight = args.get(5);

        if (!MacReducedResolution.shouldScalePresentation(sourceWidth, sourceHeight, swapchainWidth, swapchainHeight)) {
            return;
        }

        args.set(6, 0);
        args.set(7, 0);
        args.set(8, swapchainWidth);
        args.set(9, swapchainHeight);
    }
}

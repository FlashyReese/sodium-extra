package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.textures.GpuTextureView;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public class MixinGlCommandEncoder {
    @ModifyArgs(method = "presentTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"))
    private void scalePresentedTexture(Args args, GpuTextureView textureView) {
        int sourceWidth = args.get(4);
        int sourceHeight = args.get(5);
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];

        GLFW.glfwGetFramebufferSize(Minecraft.getInstance().getWindow().handle(), framebufferWidth, framebufferHeight);

        if (!MacReducedResolution.shouldScalePresentation(sourceWidth, sourceHeight, framebufferWidth[0], framebufferHeight[0])) {
            return;
        }

        args.set(6, 0);
        args.set(7, 0);
        args.set(8, framebufferWidth[0]);
        args.set(9, framebufferHeight[0]);
    }
}

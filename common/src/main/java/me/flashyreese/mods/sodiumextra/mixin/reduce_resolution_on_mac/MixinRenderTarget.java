package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(RenderTarget.class)
public class MixinRenderTarget {
    @Shadow
    public int viewWidth;

    @Shadow
    public int viewHeight;

    @ModifyArgs(method = "_blitToScreen", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_viewport(IIII)V"))
    private void scalePresentedTexture(Args args) {
        int[] framebufferWidth = new int[1];
        int[] framebufferHeight = new int[1];

        GLFW.glfwGetFramebufferSize(Minecraft.getInstance().getWindow().getWindow(), framebufferWidth, framebufferHeight);

        if (!MacReducedResolution.shouldScalePresentation(this.viewWidth, this.viewHeight, framebufferWidth[0], framebufferHeight[0])) {
            return;
        }

        args.set(2, framebufferWidth[0]);
        args.set(3, framebufferHeight[0]);
    }
}

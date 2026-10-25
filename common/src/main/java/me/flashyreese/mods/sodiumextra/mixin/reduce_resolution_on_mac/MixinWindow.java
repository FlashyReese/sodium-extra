package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Approach is based on that used by RetiNo, by Julian Dunskus
 * https://github.com/juliand665/retiNO
 * Original is licensed under MIT
 * <p>
 * Code directly pulled from Canvas by grondag
 * https://github.com/grondag/canvas/blob/7e01cf333388bbeb7f31de55266e83c2d3252cae/src/main/java/grondag/canvas/mixin/MixinWindow.java
 * Licensed under Apache-2.0
 */
@Mixin(Window.class)
public class MixinWindow {
    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Inject(at = @At(value = "RETURN"), method = "refreshFramebufferSize")
    private void afterUpdateFrameBufferSize(CallbackInfo ci) {
        this.scaleFramebufferSize();
    }

    @Inject(method = "onFramebufferResize", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void afterFramebufferResize(long handle, int newWidth, int newHeight, CallbackInfo ci) {
        this.scaleFramebufferSize();
    }

    @Unique
    private void scaleFramebufferSize() {
        if (!MacReducedResolution.isEnabled()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            var windowWidth = stack.mallocInt(1);
            var windowHeight = stack.mallocInt(1);
            GLFW.glfwGetWindowSize(((Window) (Object) this).handle(), windowWidth, windowHeight);

            // Cocoa can deliver the framebuffer callback before Minecraft's
            // window-size callback. Use the live size for both backends and
            // Vulkan's later swapchain configuration.
            int width = windowWidth.get(0);
            int height = windowHeight.get(0);
            MacReducedResolution.rememberWindowSize(width, height);
            if (width <= 0 || height <= 0) {
                return;
            }

            if (MacReducedResolution.shouldUseWindowSizeForFramebuffer()) {
                // NeoForge can reuse a Retina early-loading window. Limit every
                // update without halving an already reduced OpenGL drawable.
                this.framebufferWidth = MacReducedResolution.limitToWindowSize(this.framebufferWidth, width);
                this.framebufferHeight = MacReducedResolution.limitToWindowSize(this.framebufferHeight, height);
            } else if (MacReducedResolution.shouldReduceFramebuffer(this.framebufferWidth, this.framebufferHeight, width, height)) {
                this.framebufferWidth = MacReducedResolution.reduce(this.framebufferWidth);
                this.framebufferHeight = MacReducedResolution.reduce(this.framebufferHeight);
            }
        }
    }
}

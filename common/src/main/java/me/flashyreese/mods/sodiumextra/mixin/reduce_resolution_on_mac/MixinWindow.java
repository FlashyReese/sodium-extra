package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
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
    private int width;

    @Shadow
    private int height;

    @Shadow
    private int framebufferWidth;

    @Shadow
    private int framebufferHeight;

    @Inject(at = @At(value = "RETURN"), method = "refreshFramebufferSize")
    private void afterUpdateFrameBufferSize(CallbackInfo ci) {
        this.scaleInitialFramebufferSize();
    }

    @Inject(method = "onFramebufferResize", at = @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;framebufferHeight:I", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void afterFramebufferResize(long handle, int newWidth, int newHeight, CallbackInfo ci) {
        this.scaleFramebufferSize();
    }

    @Unique
    private void scaleInitialFramebufferSize() {
        MacReducedResolution.rememberWindowSize(this.width, this.height);

        if (MacReducedResolution.shouldUseWindowSizeForInitialFramebuffer()) {
            this.framebufferWidth = Math.max(1, this.width);
            this.framebufferHeight = Math.max(1, this.height);
            return;
        }

        this.scaleFramebufferSize();
    }

    @Unique
    private void scaleFramebufferSize() {
        MacReducedResolution.rememberWindowSize(this.width, this.height);

        if (!MacReducedResolution.shouldReduceFramebuffer(this.framebufferWidth, this.framebufferHeight, this.width, this.height)) {
            return;
        }

        this.framebufferWidth = MacReducedResolution.reduce(this.framebufferWidth);
        this.framebufferHeight = MacReducedResolution.reduce(this.framebufferHeight);
    }
}

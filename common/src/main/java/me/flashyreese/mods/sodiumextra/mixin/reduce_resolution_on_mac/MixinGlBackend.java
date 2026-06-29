package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.opengl.GlBackend;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlBackend.class)
public class MixinGlBackend {
    @Inject(method = "setWindowHints", at = @At(value = "HEAD"))
    private void preSetWindowHints(CallbackInfo ci) {
        MacReducedResolution.useOpenGlBackend();

        if (MacReducedResolution.isEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        }
    }
}

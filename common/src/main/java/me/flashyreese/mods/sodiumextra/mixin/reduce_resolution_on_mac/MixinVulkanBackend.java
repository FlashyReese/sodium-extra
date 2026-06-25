package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.mojang.blaze3d.vulkan.VulkanBackend;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanBackend.class)
public class MixinVulkanBackend {
    @Unique
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();

    @Inject(method = "setWindowHints", at = @At(value = "HEAD"))
    private void preSetWindowHints(CallbackInfo ci) {
        if (OS_NAME.contains("mac") && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        }
    }
}

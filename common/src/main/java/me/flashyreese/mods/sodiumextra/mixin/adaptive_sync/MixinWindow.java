package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public class MixinWindow {
    @Redirect(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void setSwapInterval(int interval) {
        if (SodiumExtraClientMod.options().extraSettings.useAdaptiveSync) {
            if (GLFW.glfwExtensionSupported("GLX_EXT_swap_control_tear") || GLFW.glfwExtensionSupported("WGL_EXT_swap_control_tear")) {
                GLFW.glfwSwapInterval(-1);
            } else {
                SodiumExtraClientMod.logger().warn("Adaptive vsync not supported, falling back to vanilla vsync state!");
                SodiumExtraClientMod.options().extraSettings.useAdaptiveSync = false;
                SodiumExtraClientMod.options().writeChanges();
                GLFW.glfwSwapInterval(interval);
            }
        } else {
            GLFW.glfwSwapInterval(interval);
        }
    }
}

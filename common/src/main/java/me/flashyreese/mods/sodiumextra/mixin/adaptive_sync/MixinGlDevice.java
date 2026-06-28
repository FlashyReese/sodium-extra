package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public class MixinGlDevice {
    @Unique
    private static boolean sodiumExtra$usesAdaptiveSync() {
        return SodiumExtraClientMod.options().extraSettings.useAdaptiveSync
                && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported();
    }

    @Redirect(method = "setVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void setSwapInterval(int interval, boolean vsync) {
        if (vsync && sodiumExtra$usesAdaptiveSync()) {
            GLFW.glfwSwapInterval(-1);
            return;
        }

        GLFW.glfwSwapInterval(interval);
    }
}

package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlDevice")
public class MixinGlDevice {
    @Unique
    private static boolean sodiumExtra$usesAdaptiveSync() {
        return SodiumExtraClientMod.options().extraSettings.useAdaptiveSync
                && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported();
    }

    @WrapOperation(method = "setVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void setSwapInterval(int interval, Operation<Void> original, boolean vsync) {
        if (vsync && sodiumExtra$usesAdaptiveSync()) {
            original.call(-1);
            return;
        }

        original.call(interval);
    }
}

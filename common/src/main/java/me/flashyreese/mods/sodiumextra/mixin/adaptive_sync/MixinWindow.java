package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import com.mojang.blaze3d.platform.Window;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

@Mixin(Window.class)
public class MixinWindow {
    @Unique
    private static boolean sodiumExtra$usesAdaptiveSync() {
        return SodiumExtraClientMod.options().extraSettings.useAdaptiveSync
                && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported();
    }

    @WrapOperation(method = "updateVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void setSwapInterval(int interval, Operation<Void> original) {
        if (interval > 0 && sodiumExtra$usesAdaptiveSync()) {
            original.call(-1);
            return;
        }

        original.call(interval);
    }
}

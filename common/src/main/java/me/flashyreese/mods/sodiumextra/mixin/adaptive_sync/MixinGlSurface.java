package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import com.mojang.blaze3d.opengl.GlSurface;
import com.mojang.blaze3d.systems.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.EnumSet;

@Mixin(GlSurface.class)
public class MixinGlSurface {
    @Unique
    private static boolean sodiumExtra$usesAdaptiveSync() {
        return SodiumExtraClientMod.options().extraSettings.useAdaptiveSync
                && SodiumExtraGameOptions.VerticalSyncOption.isAdaptiveSyncSupported();
    }

    @Inject(method = "supportedPresentModes", at = @At("RETURN"), cancellable = true)
    private void addFifoRelaxedPresentMode(CallbackInfoReturnable<Collection<GpuSurface.PresentMode>> cir) {
        if (!sodiumExtra$usesAdaptiveSync()) {
            return;
        }

        EnumSet<GpuSurface.PresentMode> modes = EnumSet.copyOf(cir.getReturnValue());
        modes.add(GpuSurface.PresentMode.FIFO_RELAXED);
        cir.setReturnValue(modes);
    }

    @Redirect(method = "configure", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V", remap = false))
    private void setSwapInterval(int interval, GpuSurface.Configuration config) {
        if (config.presentMode() == GpuSurface.PresentMode.FIFO_RELAXED && sodiumExtra$usesAdaptiveSync()) {
            GLFW.glfwSwapInterval(-1);
            return;
        }

        GLFW.glfwSwapInterval(interval);
    }
}

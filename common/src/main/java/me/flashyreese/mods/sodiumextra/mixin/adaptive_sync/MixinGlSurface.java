package me.flashyreese.mods.sodiumextra.mixin.adaptive_sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.opengl.GlSurface;
import com.mojang.renderpearl.api.device.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @WrapOperation(method = "configure", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLVideo;SDL_GL_SetSwapInterval(I)Z", remap = false))
    private boolean setSwapInterval(int interval, Operation<Boolean> original, GpuSurface.Configuration config) {
        if (config.presentMode() == GpuSurface.PresentMode.FIFO_RELAXED && sodiumExtra$usesAdaptiveSync()) {
            return original.call(-1) || original.call(1);
        }

        return original.call(interval);
    }
}

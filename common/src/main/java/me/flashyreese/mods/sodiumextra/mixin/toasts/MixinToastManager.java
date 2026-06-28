package me.flashyreese.mods.sodiumextra.mixin.toasts;

import me.flashyreese.mods.sodiumextra.client.util.ToastFilter;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToastManager.class)
public class MixinToastManager {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    public void goodByeToasts(Toast toast, CallbackInfo ci) {
        if (!ToastFilter.isEnabled(toast)) {
            ci.cancel();
        }
    }

    @Inject(method = "lambda$update$1", at = @At("HEAD"), cancellable = true)
    private void removeDisabledQueuedToasts(Toast toast, CallbackInfoReturnable<Boolean> cir) {
        if (!ToastFilter.isEnabled(toast)) {
            cir.setReturnValue(true);
        }
    }
}

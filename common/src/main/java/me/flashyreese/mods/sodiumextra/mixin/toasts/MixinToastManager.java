package me.flashyreese.mods.sodiumextra.mixin.toasts;

import me.flashyreese.mods.sodiumextra.client.util.ToastFilter;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

@Mixin(ToastManager.class)
public class MixinToastManager {
    @Shadow
    @Final
    private Deque<Toast> queued;

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    public void goodByeToasts(Toast toast, CallbackInfo ci) {
        if (!ToastFilter.isEnabled(toast)) {
            ci.cancel();
        }
    }

    @Inject(method = "update", at = @At("HEAD"))
    private void removeDisabledQueuedToasts(CallbackInfo ci) {
        this.queued.removeIf(toast -> !ToastFilter.isEnabled(toast));
    }
}

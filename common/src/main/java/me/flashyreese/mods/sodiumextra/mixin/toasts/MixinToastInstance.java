package me.flashyreese.mods.sodiumextra.mixin.toasts;

import me.flashyreese.mods.sodiumextra.client.util.ToastFilter;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public class MixinToastInstance {
    @Shadow
    @Final
    private Toast toast;

    @Shadow
    protected boolean hasFinishedRendering;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void skipDisabledToast(CallbackInfo ci) {
        if (!ToastFilter.isEnabled(this.toast)) {
            this.hasFinishedRendering = true;
            ci.cancel();
        }
    }
}

package me.flashyreese.mods.sodiumextra.mixin.toasts;

import me.flashyreese.mods.sodiumextra.client.util.ToastFilter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastComponent$ToastInstance")
public class MixinToastInstance {
    @Shadow
    @Final
    private Toast toast;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void skipDisabledToast(int width, GuiGraphics guiGraphics, CallbackInfoReturnable<Boolean> cir) {
        if (!ToastFilter.isEnabled(this.toast)) {
            cir.setReturnValue(true);
        }
    }
}

package me.flashyreese.mods.sodiumextra.mixin.fps;

import me.flashyreese.mods.sodiumextra.client.FrameCounter;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        FrameCounter.getInstance().onFrame();
    }
}
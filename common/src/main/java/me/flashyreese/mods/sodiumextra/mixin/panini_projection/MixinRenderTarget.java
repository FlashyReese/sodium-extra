package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RenderTarget.class)
public class MixinRenderTarget {
    // While the world renders into a larger Panini target, vanilla still binds
    // screen-sized targets (entity outlines, post chains) with bindWrite(false),
    // keeping whichever viewport was set last. Make every bind set its own
    // viewport so each draw covers the target it is bound to.
    @ModifyVariable(method = "bindWrite", at = @At("HEAD"), argsOnly = true)
    private boolean sodiumExtra$forcePaniniViewport(boolean setViewport) {
        if (setViewport) {
            return true;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.gameRenderer != null
                && ((PaniniProjectionAccess) minecraft.gameRenderer).sodiumExtra$getPaniniProjection().forcesViewport();
    }
}

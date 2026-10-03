package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import me.flashyreese.mods.sodiumextra.client.render.OutlineRenderState;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements OutlineRenderState {
    @Unique
    private boolean sodiumExtra$hasEntityOutline;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void sodiumExtra$resetEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = false;
    }

    // The first post chain in the frame graph is the entity outline effect.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V", ordinal = 0, shift = At.Shift.AFTER))
    private void sodiumExtra$markEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = true;
    }

    @Override
    public boolean sodiumExtra$hasEntityOutline() {
        return this.sodiumExtra$hasEntityOutline;
    }
}

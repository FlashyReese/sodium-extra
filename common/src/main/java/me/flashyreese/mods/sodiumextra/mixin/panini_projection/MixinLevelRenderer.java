package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.render.EntityOutlineTarget;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements EntityOutlineTarget {
    @Shadow
    private RenderTarget entityOutlineTarget;

    @Shadow
    @Final
    private LevelRenderState levelRenderState;

    @Unique
    private boolean sodiumExtra$currentFrameRendersEntityOutline;

    // The render state is reset before GameRenderer applies Panini projection.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/state/LevelRenderState;reset()V"))
    private void sodiumExtra$captureEntityOutlineState(CallbackInfo ci) {
        this.sodiumExtra$currentFrameRendersEntityOutline = this.entityOutlineTarget != null && this.levelRenderState.haveGlowingEntities;
    }

    @Override
    public RenderTarget sodiumExtra$getEntityOutlineTarget() {
        return this.entityOutlineTarget;
    }

    @Override
    public boolean sodiumExtra$hasEntityOutline() {
        return this.sodiumExtra$currentFrameRendersEntityOutline;
    }
}

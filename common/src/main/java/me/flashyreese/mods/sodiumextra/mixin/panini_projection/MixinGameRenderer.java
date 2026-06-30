package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import me.flashyreese.mods.sodiumextra.client.render.PaniniProjection;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private GameRenderState gameRenderState;

    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Shadow
    @Final
    private RenderTarget mainRenderTarget;

    // Apply the post effect just before the held item/hand is drawn: the level
    // (terrain, entities, particles, clouds, weather) is fully rendered into the
    // main target by this point, so it gets re-projected, while the hand is drawn
    // afterwards and stays in the normal projection.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand(Lnet/minecraft/client/renderer/state/level/CameraRenderState;FLorg/joml/Matrix4fc;)V"))
    private void sodiumExtra$applyPaniniProjection(DeltaTracker deltaTracker, CallbackInfo ci) {
        PaniniProjection.process(this.minecraft, this.mainRenderTarget, this.resourcePool, this.gameRenderState.levelRenderState.cameraRenderState, this.gameRenderState.windowRenderState);
    }
}

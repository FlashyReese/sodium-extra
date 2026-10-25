package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import me.flashyreese.mods.sodiumextra.client.render.PaniniProjection;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements PaniniProjectionAccess {
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

    @Unique
    private final PaniniProjection sodiumExtra$paniniProjection = new PaniniProjection();

    @Override
    public PaniniProjection sodiumExtra$getPaniniProjection() {
        return this.sodiumExtra$paniniProjection;
    }

    @WrapMethod(method = "renderLevel")
    private void sodiumExtra$renderPaniniWorld(Operation<Void> original) {
        try {
            this.sodiumExtra$paniniProjection.begin(this.minecraft, this.mainRenderTarget, this.resourcePool,
                    this.gameRenderState.levelRenderState.cameraRenderState, this.gameRenderState.windowRenderState);
            original.call();
        } finally {
            this.sodiumExtra$paniniProjection.abort();
        }
    }

    @Inject(method = "mainRenderTarget", at = @At("HEAD"), cancellable = true)
    private void sodiumExtra$getWorldTarget(CallbackInfoReturnable<RenderTarget> cir) {
        if (this.sodiumExtra$paniniProjection.isRenderingWorld()) {
            cir.setReturnValue(this.sodiumExtra$paniniProjection.worldTarget(this.mainRenderTarget));
        }
    }

    // Resolve directly from the world target into the native output before the
    // held item, hand, screen effects and GUI use the normal projection.
    @Inject(method = "render3dHud", at = @At("HEAD"))
    private void sodiumExtra$applyPaniniProjection(CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.finish(this.minecraft, this.mainRenderTarget, this.resourcePool,
                this.gameRenderState.levelRenderState.cameraRenderState);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void sodiumExtra$closePaniniTargets(CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.close();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void sodiumExtra$resizePaniniTargets(int width, int height, CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.resize();
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void sodiumExtra$resetPaniniTargets(ClientLevel level, CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.resize();
    }
}

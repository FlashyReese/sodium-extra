package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import me.flashyreese.mods.sodiumextra.client.render.PaniniProjection;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer implements PaniniProjectionAccess {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private CrossFrameResourcePool resourcePool;

    @Shadow
    @Final
    private Camera mainCamera;

    @Unique
    private final PaniniProjection sodiumExtra$paniniProjection = new PaniniProjection();

    @Override
    public PaniniProjection sodiumExtra$getPaniniProjection() {
        return this.sodiumExtra$paniniProjection;
    }

    // GameRenderer#render updates the camera before calling renderLevel, so the
    // camera already matches this frame.
    @WrapMethod(method = "renderLevel")
    private void sodiumExtra$renderPaniniWorld(DeltaTracker deltaTracker, Operation<Void> original) {
        try {
            this.sodiumExtra$paniniProjection.begin(this.minecraft, this.resourcePool, this.mainCamera, deltaTracker);
            original.call(deltaTracker);
        } finally {
            this.sodiumExtra$paniniProjection.abort();
        }
    }

    // Resolve directly from the world target into the native output as soon as
    // the level is drawn. Vanilla then clears the main target's depth and draws
    // the hand and screen effects, which must all use the native target.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"))
    private void sodiumExtra$applyPaniniProjection(DeltaTracker deltaTracker, CallbackInfo ci, @Local(ordinal = 1) float fieldOfView) {
        this.sodiumExtra$paniniProjection.finish(this.minecraft, this.resourcePool, fieldOfView);
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

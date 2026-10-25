package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import me.flashyreese.mods.sodiumextra.client.render.PaniniProjection;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
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
    Minecraft minecraft;

    @Unique
    private final PaniniProjection sodiumExtra$paniniProjection = new PaniniProjection();

    @Override
    public PaniniProjection sodiumExtra$getPaniniProjection() {
        return this.sodiumExtra$paniniProjection;
    }

    @WrapMethod(method = "renderLevel")
    private void sodiumExtra$renderPaniniWorld(DeltaTracker deltaTracker, Operation<Void> original) {
        try {
            this.sodiumExtra$paniniProjection.begin(this.minecraft);
            original.call(deltaTracker);
        } finally {
            this.sodiumExtra$paniniProjection.abort();
        }
    }

    // Project the world as soon as the level is drawn. Vanilla then clears the
    // bound target's depth and draws the hand and screen effects, which must
    // all use the native target in the normal projection.
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"))
    private void sodiumExtra$applyPaniniProjection(DeltaTracker deltaTracker, CallbackInfo ci, @Local double fieldOfView) {
        this.sodiumExtra$paniniProjection.finish(this.minecraft, fieldOfView, deltaTracker.getGameTimeDeltaTicks());
    }

    @Inject(method = {"close", "reloadShaders"}, at = @At("HEAD"))
    private void sodiumExtra$closePanini(CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.close();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void sodiumExtra$resizePanini(int width, int height, CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.resize();
    }

    // Called when the client switches levels.
    @Inject(method = "resetData", at = @At("HEAD"))
    private void sodiumExtra$resetPanini(CallbackInfo ci) {
        this.sodiumExtra$paniniProjection.resize();
    }
}

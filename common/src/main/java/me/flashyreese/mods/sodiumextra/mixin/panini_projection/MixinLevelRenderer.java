package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.render.OutlineRenderState;
import me.flashyreese.mods.sodiumextra.client.render.PaniniProjection;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements OutlineRenderState {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean sodiumExtra$hasEntityOutline;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void sodiumExtra$resetEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = false;
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;entityOutlineTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldOutlineTarget(LevelRenderer instance, Operation<RenderTarget> original) {
        RenderTarget nativeOutline = original.call(instance);
        return nativeOutline != null ? this.sodiumExtra$paniniProjection().outlineTarget(nativeOutline) : null;
    }

    // The first post chain in the frame graph is the entity outline effect; the
    // second is the Fabulous transparency chain, which must keep running.
    // While Panini is active, the outline effect is deferred so the re-projected
    // mask is processed at native resolution and edge/blur widths remain
    // consistent across world quality settings.
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V"))
    private void sodiumExtra$deferOutlineEffect(PostChain chain, FrameGraphBuilder frame, int width, int height,
                                               PostChain.TargetBundle targets, Operation<Void> original) {
        this.sodiumExtra$hasEntityOutline = true;
        if (!this.sodiumExtra$paniniProjection().isRenderingWorld()) {
            original.call(chain, frame, width, height, targets);
        }
    }

    // Scale both the normal outline and the fixed-width high-contrast border
    // so their on-screen thickness stays the same above native resolution.
    @ModifyArg(method = "renderBlockOutline", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderHitOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;DDDLnet/minecraft/client/renderer/state/level/BlockOutlineRenderState;IF)V"), index = 7)
    private float sodiumExtra$scaleWorldOutlineWidth(float width) {
        return width * this.sodiumExtra$paniniProjection().worldScale();
    }

    @Unique
    private PaniniProjection sodiumExtra$paniniProjection() {
        return ((PaniniProjectionAccess) this.minecraft.gameRenderer).sodiumExtra$getPaniniProjection();
    }

    @Override
    public boolean sodiumExtra$hasEntityOutline() {
        return this.sodiumExtra$hasEntityOutline;
    }
}

package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.render.OutlineRenderState;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements OutlineRenderState {
    @Shadow
    @Final
    private GameRenderer gameRenderer;

    @Unique
    private boolean sodiumExtra$hasEntityOutline;

    @Inject(method = "render", at = @At("HEAD"))
    private void sodiumExtra$resetEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = false;
    }

    @WrapOperation(method = "render", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;entityOutlineTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldOutlineTarget(LevelRenderer instance, Operation<RenderTarget> original) {
        return ((PaniniProjectionAccess) this.gameRenderer).sodiumExtra$getPaniniProjection().outlineTarget(original.call(instance));
    }

    // The first post chain in the frame graph is the entity outline effect; the
    // second is the Fabulous transparency chain, which must keep running.
    // While Panini is active, the outline effect is deferred so the re-projected
    // mask is processed at native resolution and edge/blur widths remain
    // consistent across world quality settings.
    @WrapOperation(method = "render", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V"))
    private void sodiumExtra$deferOutlineEffect(PostChain chain, FrameGraphBuilder frame, int width, int height,
                                               PostChain.TargetBundle targets, Operation<Void> original) {
        this.sodiumExtra$hasEntityOutline = true;
        if (!((PaniniProjectionAccess) this.gameRenderer).sodiumExtra$getPaniniProjection().isRenderingWorld()) {
            original.call(chain, frame, width, height, targets);
        }
    }

    @Override
    public boolean sodiumExtra$hasEntityOutline() {
        return this.sodiumExtra$hasEntityOutline;
    }
}

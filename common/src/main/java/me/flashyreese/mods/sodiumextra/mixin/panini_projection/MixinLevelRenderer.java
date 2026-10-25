package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.util.PaniniProjectionAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.PostChain;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Shadow
    @Final
    private GameRenderer gameRenderer;

    @WrapOperation(method = {"render", "executeOutline"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;entityOutlineTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldOutlineTarget(LevelRenderer instance, Operation<RenderTarget> original) {
        return ((PaniniProjectionAccess) this.gameRenderer).sodiumExtra$getPaniniProjection().outlineTarget(original.call(instance));
    }

    // Process the re-projected mask at native resolution so edge/blur widths
    // remain consistent across world quality settings.
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/PostChain;addToFrame(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;IILnet/minecraft/client/renderer/PostChain$TargetBundle;)V"))
    private void sodiumExtra$deferOutlineEffect(PostChain chain, FrameGraphBuilder frame, int width, int height,
                                               PostChain.TargetBundle targets, Operation<Void> original) {
        if (!((PaniniProjectionAccess) this.gameRenderer).sodiumExtra$getPaniniProjection().isRenderingWorld()) {
            original.call(chain, frame, width, height, targets);
        }
    }
}

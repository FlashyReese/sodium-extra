package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.render.EntityOutlineState;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer implements EntityOutlineState {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean sodiumExtra$hasEntityOutline;

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void sodiumExtra$resetEntityOutline(CallbackInfo ci) {
        this.sodiumExtra$hasEntityOutline = false;
    }

    // The world pass clears the raw mask directly; outline rendering binds it
    // through entityTarget(). Keep both paths on the same scaled target.
    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;entityTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldOutlineTarget(RenderTarget nativeOutline) {
        return this.sodiumExtra$paniniProjection().outlineTarget(nativeOutline);
    }

    @ModifyReturnValue(method = "entityTarget", at = @At("RETURN"))
    private RenderTarget sodiumExtra$getWorldOutlineTargetForRendering(RenderTarget nativeOutline) {
        return this.sodiumExtra$paniniProjection().outlineTarget(nativeOutline);
    }

    // Project the raw mask before vanilla Sobel/blur, keeping glow thickness at
    // native resolution. The later Fabulous transparency chain still runs here.
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 0))
    private void sodiumExtra$deferEntityOutline(PostChain chain, float partialTick, Operation<Void> original) {
        this.sodiumExtra$hasEntityOutline = true;
        if (!this.sodiumExtra$paniniProjection().isApplying()) {
            original.call(chain, partialTick);
        }
    }

    // Fabulous reads targets directly to clear/copy depth, and RenderStateShard
    // uses their getters to bind them. Route both paths through the same chain.
    @ModifyExpressionValue(method = {"renderLevel", "getTranslucentTarget"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;translucentTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldTranslucentTarget(RenderTarget nativeTarget) {
        return this.sodiumExtra$paniniProjection().transparencyTarget("translucent", nativeTarget);
    }

    @ModifyExpressionValue(method = {"renderLevel", "getItemEntityTarget"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;itemEntityTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldItemEntityTarget(RenderTarget nativeTarget) {
        return this.sodiumExtra$paniniProjection().transparencyTarget("itemEntity", nativeTarget);
    }

    @ModifyExpressionValue(method = {"renderLevel", "getParticlesTarget"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;particlesTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldParticlesTarget(RenderTarget nativeTarget) {
        return this.sodiumExtra$paniniProjection().transparencyTarget("particles", nativeTarget);
    }

    @ModifyExpressionValue(method = {"renderLevel", "getWeatherTarget"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;weatherTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldWeatherTarget(RenderTarget nativeTarget) {
        return this.sodiumExtra$paniniProjection().transparencyTarget("weather", nativeTarget);
    }

    @ModifyExpressionValue(method = {"renderLevel", "getCloudsTarget"}, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;cloudsTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getWorldCloudsTarget(RenderTarget nativeTarget) {
        return this.sodiumExtra$paniniProjection().transparencyTarget("clouds", nativeTarget);
    }

    @ModifyExpressionValue(method = "renderLevel", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/client/renderer/LevelRenderer;transparencyChain:Lnet/minecraft/client/renderer/PostChain;"))
    private PostChain sodiumExtra$getWorldTransparencyChain(PostChain nativeChain) {
        return this.sodiumExtra$paniniProjection().transparencyChain(nativeChain);
    }

    // A render-distance change can rebuild vanilla chains from setupRender().
    // They must retain the native main target even while Panini redirects draws.
    @WrapOperation(method = {"initOutline", "initTransparency"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    private RenderTarget sodiumExtra$getNativeTargetForPostChain(Minecraft minecraft, Operation<RenderTarget> original) {
        return this.sodiumExtra$paniniProjection().nativeTarget(original.call(minecraft));
    }

    @Inject(method = {"initOutline", "initTransparency"}, at = @At("RETURN"))
    private void sodiumExtra$restoreWorldTargetAfterPostChainCreation(CallbackInfo ci) {
        if (this.sodiumExtra$paniniProjection().isRenderingWorld()) {
            this.minecraft.getMainRenderTarget().bindWrite(true);
        }
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

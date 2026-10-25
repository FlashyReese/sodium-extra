package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.render.PaniniRenderTargets;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Mixin(PostChain.class)
public abstract class MixinPostChain {
    @Shadow
    @Final
    private RenderTarget screenTarget;

    @Shadow
    @Final
    private Map<String, RenderTarget> customRenderTargets;

    @Shadow
    @Final
    private List<RenderTarget> fullSizedTargets;

    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Shadow
    public abstract void close();

    // Vanilla allocates targets while the constructor loads the chain. Keep
    // completed allocations reachable for cleanup if a later target/pass fails.
    @WrapMethod(method = "load")
    private void sodiumExtra$closeFailedPaniniChain(TextureManager textureManager, ResourceLocation resourceLocation,
                                                   Operation<Void> original) throws IOException {
        if (!PaniniRenderTargets.isCreatingChain()) {
            original.call(textureManager, resourceLocation);
            return;
        }
        try {
            original.call(textureManager, resourceLocation);
        } catch (Exception exception) {
            this.close();
            throw exception;
        }
    }

    @Inject(method = "addTempTarget", at = @At("HEAD"), cancellable = true)
    private void sodiumExtra$allocatePaniniTarget(String name, int width, int height, CallbackInfo ci) {
        if (!PaniniRenderTargets.isCreatingChain()) {
            return;
        }
        RenderTarget target = PaniniRenderTargets.create(width, height, this.screenTarget);
        target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.customRenderTargets.put(name, target);
        if (width == this.screenWidth && height == this.screenHeight) {
            this.fullSizedTargets.add(target);
        }
        ci.cancel();
    }
}

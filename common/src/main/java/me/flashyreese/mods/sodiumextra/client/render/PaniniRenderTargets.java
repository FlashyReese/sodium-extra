package me.flashyreese.mods.sodiumextra.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.flashyreese.mods.sodiumextra.client.services.RenderTargetFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

/** Allocation helpers that retain ownership until all GPU resources are ready. */
public final class PaniniRenderTargets {
    private static final ThreadLocal<Boolean> CREATING_CHAIN = ThreadLocal.withInitial(() -> false);

    private PaniniRenderTargets() {
    }

    public static RenderTarget create(int width, int height, boolean useDepth) {
        return allocate(new RenderTarget(useDepth) {}, width, height);
    }

    public static RenderTarget create(int width, int height, RenderTarget template) {
        return allocate(RenderTargetFactory.INSTANCE.create(template), width, height);
    }

    static RenderTarget allocate(RenderTarget target, int width, int height) {
        try {
            target.resize(width, height, Minecraft.ON_OSX);
            return target;
        } catch (RuntimeException exception) {
            // TextureTarget allocates in its constructor, losing the object on
            // failure. Keep it reachable here so partially created buffers die too.
            target.destroyBuffers();
            throw exception;
        }
    }

    public static boolean isCreatingChain() {
        return CREATING_CHAIN.get();
    }

    public static PostChain createChain(Minecraft minecraft, RenderTarget target, ResourceLocation id) throws IOException {
        boolean previous = CREATING_CHAIN.get();
        CREATING_CHAIN.set(true);
        try {
            // MixinPostChain also cleans up targets/passes if parsing fails partway.
            return new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), target, id);
        } finally {
            if (previous) {
                CREATING_CHAIN.set(true);
            } else {
                CREATING_CHAIN.remove();
            }
        }
    }
}

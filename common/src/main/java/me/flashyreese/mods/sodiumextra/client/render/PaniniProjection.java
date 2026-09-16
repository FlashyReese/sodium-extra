package me.flashyreese.mods.sodiumextra.client.render;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorLevelRenderer;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostChain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaniniProjection {
    private static final ResourceLocation POST_CHAIN_ID = ResourceLocation.fromNamespaceAndPath("sodium-extra", "shaders/post/panini.json");
    private static final String CONFIG_UNIFORM = "Params";
    private static final AtomicBoolean WARNED_MISSING_CHAIN = new AtomicBoolean(false);
    private static final AtomicBoolean WARNED_MISSING_UNIFORM = new AtomicBoolean(false);
    private static PostChain postChain;
    private static boolean postChainUnavailable;
    private static int postChainWidth = -1;
    private static int postChainHeight = -1;

    public static void process(Minecraft minecraft, RenderTarget mainTarget, float tickDelta, double fieldOfView) {
        Window window = minecraft.getWindow();
        if (!shouldApply(minecraft) || !hasValidWindow(window)) {
            close();
            return;
        }

        PostChain postChain = getOrCreatePostChain(minecraft, mainTarget, window);
        if (postChain == null) {
            return;
        }

        if (updateUniforms(postChain, window, fieldOfView)) {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            postChain.process(tickDelta);
            mainTarget.bindWrite(true);
        }
    }

    private static boolean shouldApply(Minecraft minecraft) {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        return settings.paniniProjection
                && settings.paniniProjectionStrength > 0
                && !settings.preventShaders
                && minecraft.player != null
                && !minecraft.player.isScoping()
                && !minecraft.gameRenderer.isPanoramicMode()
                && !isFrustumCaptured(minecraft)
                && !IrisCompat.isShaderPackInUse();
    }

    private static boolean isFrustumCaptured(Minecraft minecraft) {
        return minecraft.levelRenderer != null && ((AccessorLevelRenderer) minecraft.levelRenderer).sodiumExtra$getCapturedFrustum() != null;
    }

    private static boolean hasValidWindow(Window window) {
        return window != null && window.getWidth() > 0 && window.getHeight() > 0;
    }

    private static PostChain getOrCreatePostChain(Minecraft minecraft, RenderTarget mainTarget, Window window) {
        if (postChainUnavailable) {
            return null;
        }

        int width = window.getWidth();
        int height = window.getHeight();
        if (postChain == null) {
            try {
                postChain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), mainTarget, POST_CHAIN_ID);
            } catch (IOException | JsonSyntaxException exception) {
                postChainUnavailable = true;
                if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                    SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the post effect '{}' is unavailable", POST_CHAIN_ID, exception);
                }
                return null;
            }
        }

        if (postChainWidth != width || postChainHeight != height) {
            postChain.resize(width, height);
            postChainWidth = width;
            postChainHeight = height;
        }

        return postChain;
    }

    private static boolean updateUniforms(PostChain postChain, Window window, double fieldOfView) {
        List<PostPass> passes = ((AccessorPostChain) postChain).sodiumExtra$getPasses();
        for (PostPass pass : passes) {
            var configUniform = pass.getEffect().getUniform(CONFIG_UNIFORM);
            if (configUniform != null) {
                SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
                float strength = settings.paniniProjectionStrength / 100.0F;
                float verticalExtent = (float) Math.tan(Math.toRadians(fieldOfView) * 0.5);
                float horizontalExtent = verticalExtent * window.getWidth() / (float) window.getHeight();

                if (!Float.isFinite(horizontalExtent) || !Float.isFinite(verticalExtent)) {
                    return false;
                }

                configUniform.set(strength, horizontalExtent, verticalExtent, 0.0F);
                return true;
            }
        }

        if (WARNED_MISSING_UNIFORM.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the '{}' post uniform is unavailable", CONFIG_UNIFORM);
        }

        return false;
    }

    private static void close() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
            postChainWidth = -1;
            postChainHeight = -1;
        }
    }
}

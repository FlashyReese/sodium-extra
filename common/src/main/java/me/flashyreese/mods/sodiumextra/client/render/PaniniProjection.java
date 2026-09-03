package me.flashyreese.mods.sodiumextra.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostChain;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostPass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaniniProjection {
    private static final Identifier POST_CHAIN_ID = Identifier.fromNamespaceAndPath("sodium-extra", "panini");
    private static final String CONFIG_UNIFORM = "PaniniConfig";
    private static final AtomicBoolean WARNED_MISSING_CHAIN = new AtomicBoolean(false);
    private static final AtomicBoolean WARNED_MISSING_UNIFORM = new AtomicBoolean(false);

    public static void process(Minecraft minecraft, RenderTarget mainTarget, GraphicsResourceAllocator resourceAllocator, float fieldOfView) {
        Window window = minecraft.getWindow();
        if (!shouldApply(minecraft) || !hasValidWindow(window)) {
            return;
        }

        PostChain postChain = minecraft.getShaderManager().getPostChain(POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain == null) {
            if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the post effect '{}' is unavailable", POST_CHAIN_ID);
            }
            return;
        }

        if (updateUniforms(postChain, window, fieldOfView)) {
            postChain.process(mainTarget, resourceAllocator);
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
                && !IrisCompat.isShaderPackInUse();
    }

    private static boolean hasValidWindow(Window window) {
        return window != null && window.getWidth() > 0 && window.getHeight() > 0 && !window.isMinimized();
    }

    private static boolean updateUniforms(PostChain postChain, Window window, float fieldOfView) {
        List<PostPass> passes = ((AccessorPostChain) postChain).sodiumExtra$getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> customUniforms = ((AccessorPostPass) pass).sodiumExtra$getCustomUniforms();
            GpuBuffer configUniform = customUniforms.get(CONFIG_UNIFORM);
            if (configUniform != null) {
                configUniform = prepareConfigUniform(customUniforms, configUniform);
                return writeConfigUniform(configUniform, window, fieldOfView);
            }
        }

        if (WARNED_MISSING_UNIFORM.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the '{}' post uniform is unavailable", CONFIG_UNIFORM);
        }

        return false;
    }

    private static GpuBuffer prepareConfigUniform(Map<String, GpuBuffer> customUniforms, GpuBuffer configUniform) {
        if ((configUniform.usage() & GpuBuffer.USAGE_COPY_DST) != 0 && !configUniform.isClosed()) {
            return configUniform;
        }

        GpuBuffer replacement;
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            replacement = RenderSystem.getDevice().createBuffer(
                    () -> "Sodium Extra Panini projection config",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    createConfigBuffer(memoryStack, 0.0F, 1.0F, 1.0F)
            );
        }

        customUniforms.put(CONFIG_UNIFORM, replacement);

        if (!configUniform.isClosed()) {
            configUniform.close();
        }

        return replacement;
    }

    private static boolean writeConfigUniform(GpuBuffer configUniform, Window window, float fieldOfView) {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        float strength = settings.paniniProjectionStrength / 100.0F;

        float verticalExtent = (float) Math.tan(Math.toRadians(fieldOfView) * 0.5);
        float horizontalExtent = verticalExtent * window.getWidth() / (float) window.getHeight();

        if (!Float.isFinite(horizontalExtent) || !Float.isFinite(verticalExtent)) {
            return false;
        }

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(
                    configUniform.slice(),
                    createConfigBuffer(memoryStack, strength, horizontalExtent, verticalExtent)
            );
        }

        return true;
    }

    private static ByteBuffer createConfigBuffer(MemoryStack memoryStack, float strength, float horizontalExtent, float verticalExtent) {
        return Std140Builder.onStack(memoryStack, 16)
                .putVec4(strength, horizontalExtent, verticalExtent, 0.0F)
                .get();
    }
}

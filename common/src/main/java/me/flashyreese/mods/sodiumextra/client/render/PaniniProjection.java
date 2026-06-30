package me.flashyreese.mods.sodiumextra.client.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
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
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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

    public static void process(Minecraft minecraft, RenderTarget mainTarget, GraphicsResourceAllocator resourceAllocator, CameraRenderState cameraRenderState, WindowRenderState windowRenderState) {
        if (!shouldApply(cameraRenderState) || !hasValidWindow(windowRenderState)) {
            return;
        }

        PostChain postChain = minecraft.getShaderManager().getPostChain(POST_CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (postChain == null) {
            if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the post effect '{}' is unavailable", POST_CHAIN_ID);
            }
            return;
        }

        if (updateUniforms(postChain, windowRenderState)) {
            postChain.process(mainTarget, resourceAllocator);
        }
    }

    private static boolean shouldApply(CameraRenderState cameraRenderState) {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        Minecraft minecraft = Minecraft.getInstance();
        return settings.paniniProjection
                && settings.paniniProjectionStrength > 0
                && !settings.preventShaders
                && minecraft.player != null
                && !minecraft.player.isScoping()
                && cameraRenderState != null
                && !cameraRenderState.isPanoramicMode
                && !cameraRenderState.isFrustumCaptured
                && !IrisCompat.isShaderPackInUse();
    }

    private static boolean hasValidWindow(WindowRenderState windowRenderState) {
        return windowRenderState != null && windowRenderState.width > 0 && windowRenderState.height > 0 && !windowRenderState.isMinimized;
    }

    private static boolean updateUniforms(PostChain postChain, WindowRenderState windowRenderState) {
        List<PostPass> passes = ((AccessorPostChain) postChain).sodiumExtra$getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> customUniforms = ((AccessorPostPass) pass).sodiumExtra$getCustomUniforms();
            GpuBuffer configUniform = customUniforms.get(CONFIG_UNIFORM);
            if (configUniform != null) {
                configUniform = prepareConfigUniform(customUniforms, configUniform);
                writeConfigUniform(configUniform, windowRenderState);
                return true;
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
                    createConfigBuffer(memoryStack, 0.0F, 1.0F)
            );
        }

        customUniforms.put(CONFIG_UNIFORM, replacement);

        if (!configUniform.isClosed()) {
            configUniform.close();
        }

        return replacement;
    }

    private static void writeConfigUniform(GpuBuffer configUniform, WindowRenderState windowRenderState) {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        float strength = settings.paniniProjectionStrength / 100.0F;
        float aspect = windowRenderState.width / (float) windowRenderState.height;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(configUniform.slice(), createConfigBuffer(memoryStack, strength, aspect));
        }
    }

    private static ByteBuffer createConfigBuffer(MemoryStack memoryStack, float strength, float aspect) {
        return Std140Builder.onStack(memoryStack, 16)
                .putVec4(strength, aspect, 0.0F, 0.0F)
                .get();
    }
}

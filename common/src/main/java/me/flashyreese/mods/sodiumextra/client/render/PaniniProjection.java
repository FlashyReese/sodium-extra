package me.flashyreese.mods.sodiumextra.client.render;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.services.RenderTargetFactory;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorLevelRenderer;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostChain;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostPass;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.WindowRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class PaniniProjection {
    private static final Identifier POST_CHAIN_ID = Identifier.fromNamespaceAndPath("sodium-extra", "panini");
    private static final Identifier SOURCE_TARGET_ID = Identifier.fromNamespaceAndPath("sodium-extra", "panini_source");
    private static final Identifier OUTLINE_POST_CHAIN_ID = Identifier.withDefaultNamespace("entity_outline");
    private static final Set<Identifier> EXTERNAL_TARGETS = Set.of(PostChain.MAIN_TARGET_ID, SOURCE_TARGET_ID);
    private static final String CONFIG_UNIFORM = "PaniniConfig";
    private static final AtomicBoolean WARNED_MISSING_CHAIN = new AtomicBoolean(false);
    private static final AtomicBoolean WARNED_MISSING_UNIFORM = new AtomicBoolean(false);

    private GlobalSettingsUniform worldSettingsUniform;
    private GpuBuffer nativeSettingsUniform;
    private RenderTarget worldTarget;
    private RenderTarget worldOutlineTarget;
    private GpuBuffer outlineConfigUniform;
    private PostChain framePostChain;
    private Map<String, GpuBuffer> frameCustomUniforms;
    private GpuBuffer frameConfigUniform;
    private boolean renderingWorld;
    private float frameScale = 1.0F;
    // Quality that last failed to allocate, or 0. Retried once quality or window size changes.
    private int failedQuality;
    private int limitedWidth;
    private int limitedHeight;

    // Minecraft#getMainRenderTarget is redirected while the world renders, so
    // the native target is only fetched once that redirect is inactive.
    public void begin(Minecraft minecraft, GraphicsResourceAllocator resourceAllocator,
                      GameRenderState gameRenderState, DeltaTracker deltaTracker) {
        this.abort();
        RenderTarget nativeMain = minecraft.getMainRenderTarget();
        int quality = SodiumExtraClientMod.options().extraSettings.paniniProjectionQuality;
        if (this.failedQuality == quality) {
            return;
        }
        if (!isEnabled()) {
            this.releaseTargets(resourceAllocator);
            return;
        }
        CameraRenderState cameraRenderState = gameRenderState.levelRenderState.cameraRenderState;
        // Keep targets during temporary bypasses, such as using a spyglass.
        if (!shouldApply(minecraft, cameraRenderState) || !hasValidWindow(gameRenderState.windowRenderState)) {
            return;
        }

        PostChain postChain = minecraft.getShaderManager().getPostChain(POST_CHAIN_ID, EXTERNAL_TARGETS);
        if (postChain == null) {
            if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the post effect '{}' is unavailable", POST_CHAIN_ID);
            }
            this.releaseTargets(resourceAllocator);
            return;
        }
        Map<String, GpuBuffer> customUniforms = updateUniforms(postChain, cameraRenderState);
        if (customUniforms == null) {
            this.releaseTargets(resourceAllocator);
            return;
        }

        float requestedScale = quality / 100.0F;
        int maxTextureSize = RenderSystem.getDevice().getMaxTextureSize();
        float scale = Math.min(requestedScale, (float) maxTextureSize / Math.max(nativeMain.width, nativeMain.height));
        int width = Math.min(maxTextureSize, (int) Math.ceil(nativeMain.width * scale));
        int height = Math.min(maxTextureSize, (int) Math.ceil(nativeMain.height * scale));
        if (scale < requestedScale && (this.limitedWidth != nativeMain.width || this.limitedHeight != nativeMain.height)) {
            SodiumExtraClientMod.logger().warn("Panini world resolution is limited to {}x{} by the GPU's maximum texture size of {}", width, height, maxTextureSize);
            this.limitedWidth = nativeMain.width;
            this.limitedHeight = nativeMain.height;
        }

        try {
            this.prepareTargets(minecraft, nativeMain, width, height, resourceAllocator);
        } catch (GpuOutOfMemoryException exception) {
            this.failedQuality = quality;
            this.releaseTargets(resourceAllocator);
            SodiumExtraClientMod.logger().warn("Unable to allocate {}x{} Panini world targets; disabling Panini until the quality setting or window size changes", width, height, exception);
            return;
        }
        this.failedQuality = 0;

        this.framePostChain = postChain;
        this.frameCustomUniforms = customUniforms;
        this.frameConfigUniform = customUniforms.get(CONFIG_UNIFORM);
        this.renderingWorld = true;
        this.frameScale = this.worldTarget != null ? (float) this.worldTarget.width / nativeMain.width : 1.0F;
        if (this.worldTarget != null) {
            if (this.worldSettingsUniform == null) {
                this.worldSettingsUniform = new GlobalSettingsUniform();
            }
            this.nativeSettingsUniform = RenderSystem.getGlobalSettingsUniform();
            updateScreenSize(minecraft, gameRenderState, deltaTracker, this.worldSettingsUniform, this.worldTarget.width, this.worldTarget.height);
        }
    }

    public RenderTarget worldTarget(RenderTarget nativeMain) {
        return this.renderingWorld && this.worldTarget != null ? this.worldTarget : nativeMain;
    }

    public RenderTarget outlineTarget(RenderTarget nativeOutline) {
        return this.renderingWorld && this.worldOutlineTarget != null ? this.worldOutlineTarget : nativeOutline;
    }

    public boolean isRenderingWorld() {
        return this.renderingWorld;
    }

    public float worldScale() {
        return this.renderingWorld ? this.frameScale : 1.0F;
    }

    public void finish(Minecraft minecraft, GraphicsResourceAllocator resourceAllocator, CameraRenderState cameraRenderState) {
        this.renderingWorld = false;
        this.restoreScreenSize();
        if (this.framePostChain == null) {
            return;
        }
        RenderTarget nativeMain = minecraft.getMainRenderTarget();

        // Without a world target, source and output are the same image. That is
        // safe because the chain warps into its own swap target before blitting.
        // Vanilla clears depth before the hand and no later pass reads world
        // depth, so only color needs to be projected.
        processTarget(this.framePostChain, this.worldTarget != null ? this.worldTarget : nativeMain, nativeMain, resourceAllocator);

        RenderTarget outline = ((AccessorLevelRenderer) minecraft.levelRenderer).sodiumExtra$getEntityOutlineTarget();
        if (((OutlineRenderState) minecraft.levelRenderer).sodiumExtra$hasEntityOutline() && outline != null) {
            PostChain outlineChain = minecraft.getShaderManager().getPostChain(OUTLINE_POST_CHAIN_ID, LevelTargetBundle.OUTLINE_TARGETS);
            GpuBuffer outlineConfig = this.outlineConfigUniform();
            if (outlineChain != null && writeConfigUniform(outlineConfig, cameraRenderState, true)) {
                // Re-project the raw entity mask first. Sobel and blur then run
                // at native resolution so glow thickness matches every quality.
                // Mask filtering restores straight RGB before Sobel applies alpha.
                // The mask flag lives in its own buffer so the world pass, which
                // may still be pending on the GPU, never observes it.
                this.frameCustomUniforms.put(CONFIG_UNIFORM, outlineConfig);
                try {
                    processTarget(this.framePostChain, this.worldOutlineTarget != null ? this.worldOutlineTarget : outline, outline, resourceAllocator);
                } finally {
                    this.frameCustomUniforms.put(CONFIG_UNIFORM, this.frameConfigUniform);
                }
                processOutline(outlineChain, nativeMain, outline, resourceAllocator);
            } else {
                RenderSystem.getDevice().createCommandEncoder().clearColorTexture(Objects.requireNonNull(outline.getColorTexture()), 0);
            }
        } else if (outline != null && this.worldOutlineTarget != null) {
            // Vanilla still composites the native outline when no entities glow.
            // Its world pass cleared our scaled mask, leaving the native output stale.
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(Objects.requireNonNull(outline.getColorTexture()), 0);
        }
        this.framePostChain = null;
        this.frameCustomUniforms = null;
        this.frameConfigUniform = null;
    }

    public void abort() {
        this.resetFrame();
    }

    private void resetFrame() {
        this.renderingWorld = false;
        this.restoreScreenSize();
        this.framePostChain = null;
        this.frameCustomUniforms = null;
        this.frameConfigUniform = null;
        this.frameScale = 1.0F;
    }

    public void resize() {
        this.resetFrame();
        this.releaseTargets(null);
        this.failedQuality = 0;
        this.limitedWidth = 0;
        this.limitedHeight = 0;
    }

    public void close() {
        this.resetFrame();
        this.releaseTargets(null);
        if (this.worldSettingsUniform != null) {
            this.worldSettingsUniform.close();
            this.worldSettingsUniform = null;
        }
        if (this.outlineConfigUniform != null) {
            this.outlineConfigUniform.close();
            this.outlineConfigUniform = null;
        }
    }

    private GpuBuffer outlineConfigUniform() {
        if (this.outlineConfigUniform == null || this.outlineConfigUniform.isClosed()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                this.outlineConfigUniform = RenderSystem.getDevice().createBuffer(() -> "Sodium Extra Panini outline config",
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, createConfigBuffer(stack, 0.0F, 1.0F, 1.0F));
            }
        }
        return this.outlineConfigUniform;
    }

    private void prepareTargets(Minecraft minecraft, RenderTarget nativeMain, int width, int height, GraphicsResourceAllocator resourceAllocator) {
        if (width == nativeMain.width && height == nativeMain.height) {
            this.releaseTargets(resourceAllocator);
            return;
        }
        // Vanilla creates the outline target lazily, so it may not exist yet.
        RenderTarget nativeOutline = ((AccessorLevelRenderer) minecraft.levelRenderer).sodiumExtra$getEntityOutlineTarget();
        if (matches(this.worldTarget, nativeMain, width, height)
                && (nativeOutline == null ? this.worldOutlineTarget == null : matches(this.worldOutlineTarget, nativeOutline, width, height))) {
            return;
        }
        this.releaseTargets(resourceAllocator);
        try {
            this.worldTarget = createTarget("Sodium Extra Panini world", nativeMain, width, height);
            if (nativeOutline != null) {
                this.worldOutlineTarget = createTarget("Sodium Extra Panini outlines", nativeOutline, width, height);
            }
        } catch (RuntimeException exception) {
            this.releaseTargets(resourceAllocator);
            throw exception;
        }
        clearResourcePool(resourceAllocator);
    }

    // Color is always RGBA8; the depth format also captures NeoForge stencil support.
    private static boolean matches(RenderTarget target, RenderTarget nativeTarget, int width, int height) {
        return target != null && target.width == width && target.height == height
                && target.useDepth == nativeTarget.useDepth
                && (!target.useDepth || Objects.requireNonNull(target.getDepthTexture()).getFormat() == Objects.requireNonNull(nativeTarget.getDepthTexture()).getFormat());
    }

    private static RenderTarget createTarget(String label, RenderTarget nativeTarget, int width, int height) {
        RenderTarget target = RenderTargetFactory.INSTANCE.create(label, nativeTarget);
        try {
            target.createBuffers(width, height);
            return target;
        } catch (RuntimeException exception) {
            target.destroyBuffers();
            throw exception;
        }
    }

    private void releaseTargets(GraphicsResourceAllocator resourceAllocator) {
        boolean hadTargets = this.worldTarget != null || this.worldOutlineTarget != null;
        if (this.worldTarget != null) {
            this.worldTarget.destroyBuffers();
            this.worldTarget = null;
        }
        if (this.worldOutlineTarget != null) {
            this.worldOutlineTarget.destroyBuffers();
            this.worldOutlineTarget = null;
        }
        if (hadTargets) {
            clearResourcePool(resourceAllocator);
        }
    }

    private static void clearResourcePool(GraphicsResourceAllocator resourceAllocator) {
        if (resourceAllocator instanceof CrossFrameResourcePool pool) {
            pool.clear();
        }
    }

    private void restoreScreenSize() {
        if (this.nativeSettingsUniform != null) {
            RenderSystem.setGlobalSettingsUniform(this.nativeSettingsUniform);
            this.nativeSettingsUniform = null;
        }
    }

    private static void updateScreenSize(Minecraft minecraft, GameRenderState state, DeltaTracker deltaTracker,
                                         GlobalSettingsUniform uniform, int width, int height) {
        uniform.update(width, height, state.optionsRenderState.glintStrength,
                minecraft.level == null ? 0L : minecraft.level.getGameTime(), deltaTracker,
                state.optionsRenderState.menuBackgroundBlurriness, state.levelRenderState.cameraRenderState.pos,
                state.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS);
    }

    private static void processTarget(PostChain postChain, RenderTarget source, RenderTarget output, GraphicsResourceAllocator resourceAllocator) {
        FrameGraphBuilder frame = new FrameGraphBuilder();
        Map<Identifier, ResourceHandle<RenderTarget>> targets = new HashMap<>();
        targets.put(SOURCE_TARGET_ID, frame.importExternal("panini_source", source));
        targets.put(PostChain.MAIN_TARGET_ID, frame.importExternal("panini_output", output));
        postChain.addToFrame(frame, output.width, output.height, new PostChain.TargetBundle() {
            @Override
            public void replace(@NonNull Identifier id, @NonNull ResourceHandle<RenderTarget> handle) {
                if (!targets.containsKey(id)) {
                    throw new IllegalArgumentException("Unknown Panini target: " + id);
                }
                targets.put(id, handle);
            }

            @Override
            public ResourceHandle<RenderTarget> get(@NonNull Identifier id) {
                return targets.get(id);
            }
        });
        frame.execute(resourceAllocator);
    }

    private static void processOutline(PostChain postChain, RenderTarget nativeMain, RenderTarget outline, GraphicsResourceAllocator resourceAllocator) {
        FrameGraphBuilder frame = new FrameGraphBuilder();
        LevelTargetBundle targets = new LevelTargetBundle();
        targets.main = frame.importExternal("panini_outline_main", nativeMain);
        targets.entityOutline = frame.importExternal("panini_outline", outline);
        postChain.addToFrame(frame, outline.width, outline.height, targets);
        frame.execute(resourceAllocator);
    }

    private static boolean isEnabled() {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        return settings.paniniProjection && settings.paniniProjectionStrength > 0 && !settings.preventShaders
                && !IrisCompat.isShaderPackInUse();
    }

    private static boolean shouldApply(Minecraft minecraft, CameraRenderState cameraRenderState) {
        return minecraft.player != null && !minecraft.player.isScoping() && cameraRenderState != null
                && !cameraRenderState.isPanoramicMode && !isFrustumCaptured(minecraft);
    }

    private static boolean isFrustumCaptured(Minecraft minecraft) {
        return minecraft.gameRenderer.getMainCamera().getCapturedFrustum() != null;
    }

    private static boolean hasValidWindow(WindowRenderState windowRenderState) {
        return windowRenderState != null && windowRenderState.width > 0 && windowRenderState.height > 0 && !windowRenderState.isMinimized;
    }

    private static Map<String, GpuBuffer> updateUniforms(PostChain postChain, CameraRenderState cameraRenderState) {
        List<PostPass> passes = ((AccessorPostChain) postChain).sodiumExtra$getPasses();
        for (PostPass pass : passes) {
            Map<String, GpuBuffer> customUniforms = ((AccessorPostPass) pass).sodiumExtra$getCustomUniforms();
            GpuBuffer configUniform = customUniforms.get(CONFIG_UNIFORM);
            if (configUniform != null) {
                configUniform = prepareConfigUniform(customUniforms, configUniform);
                return writeConfigUniform(configUniform, cameraRenderState) ? customUniforms : null;
            }
        }
        if (WARNED_MISSING_UNIFORM.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the '{}' post uniform is unavailable", CONFIG_UNIFORM);
        }
        return null;
    }

    private static GpuBuffer prepareConfigUniform(Map<String, GpuBuffer> customUniforms, GpuBuffer configUniform) {
        if ((configUniform.usage() & GpuBuffer.USAGE_COPY_DST) != 0 && !configUniform.isClosed()) {
            return configUniform;
        }
        GpuBuffer replacement;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            replacement = RenderSystem.getDevice().createBuffer(() -> "Sodium Extra Panini projection config",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, createConfigBuffer(stack, 0.0F, 1.0F, 1.0F));
        }
        customUniforms.put(CONFIG_UNIFORM, replacement);
        if (!configUniform.isClosed()) {
            configUniform.close();
        }
        return replacement;
    }

    private static boolean writeConfigUniform(GpuBuffer configUniform, CameraRenderState cameraRenderState) {
        return writeConfigUniform(configUniform, cameraRenderState, false);
    }

    private static boolean writeConfigUniform(GpuBuffer configUniform, CameraRenderState cameraRenderState, boolean outlineMask) {
        float strength = SodiumExtraClientMod.options().extraSettings.paniniProjectionStrength / 100.0F;
        float horizontalExtent = 1.0F / Math.abs(cameraRenderState.projectionMatrix.m00());
        float verticalExtent = 1.0F / Math.abs(cameraRenderState.projectionMatrix.m11());
        if (!Float.isFinite(horizontalExtent) || !Float.isFinite(verticalExtent)) {
            return false;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(configUniform.slice(), createConfigBuffer(stack, strength, horizontalExtent, verticalExtent, outlineMask));
        }
        return true;
    }

    private static ByteBuffer createConfigBuffer(MemoryStack stack, float strength, float horizontalExtent, float verticalExtent) {
        return createConfigBuffer(stack, strength, horizontalExtent, verticalExtent, false);
    }

    private static ByteBuffer createConfigBuffer(MemoryStack stack, float strength, float horizontalExtent, float verticalExtent, boolean outlineMask) {
        return Std140Builder.onStack(stack, 16).putVec4(strength, horizontalExtent, verticalExtent, outlineMask ? 1.0F : 0.0F).get();
    }
}

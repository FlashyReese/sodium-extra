package me.flashyreese.mods.sodiumextra.client.render;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.client.services.RenderTargetFactory;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorLevelRenderer;
import me.flashyreese.mods.sodiumextra.mixin.panini_projection.AccessorPostChain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Applies Panini projection to the world image.
 * <p>
 * Above 100% quality, the world is rendered into a larger offscreen target by
 * redirecting {@link Minecraft#getMainRenderTarget()}, then projected straight
 * into the native target. In 1.21.1 a PostChain is bound to fixed targets at
 * construction, so that projection is drawn directly with the shader program.
 * Fabulous uses a separate transparency chain whose fixed targets have the
 * world resolution. Outlines are projected before vanilla's native Sobel/blur.
 */
public class PaniniProjection {
    private static final ResourceLocation POST_CHAIN_ID = ResourceLocation.fromNamespaceAndPath("sodium-extra", "shaders/post/panini.json");
    private static final ResourceLocation TRANSPARENCY_CHAIN_ID = ResourceLocation.withDefaultNamespace("shaders/post/transparency.json");
    private static final String PROGRAM_NAME = "sodium_extra_panini";
    private static final String CONFIG_UNIFORM = "Params";
    private static final int GL_LINEAR = 9729;
    private static final int GL_LEQUAL = 515;
    private static final int GL_ALWAYS = 519;
    private static final AtomicBoolean WARNED_MISSING_CHAIN = new AtomicBoolean(false);
    private static final AtomicBoolean WARNED_MISSING_UNIFORM = new AtomicBoolean(false);
    private static final AtomicBoolean WARNED_MISSING_PROGRAM = new AtomicBoolean(false);

    private PostChain postChain;
    private int postChainWidth = -1;
    private int postChainHeight = -1;
    private boolean postChainUnavailable;
    private PostChain outlinePostChain;
    private RenderTarget outlineTarget;
    private int outlineWidth = -1;
    private int outlineHeight = -1;
    private boolean outlinePostChainUnavailable;
    private EffectInstance projectionEffect;
    private boolean projectionEffectUnavailable;
    private RenderTarget worldTarget;
    private RenderTarget worldOutlineTarget;
    private PostChain worldTransparencyChain;
    private PostChain nativeTransparencyChain;
    private RenderTarget frameNativeMain;
    private boolean applying;
    private boolean renderingWorld;
    // Quality that last failed to allocate, or 0. Retried once quality or window size changes.
    private int failedQuality;
    private int limitedWidth;
    private int limitedHeight;

    // Minecraft#getMainRenderTarget is redirected while the world renders, so
    // the native target is only fetched once that redirect is inactive.
    public void begin(Minecraft minecraft) {
        this.abort();
        RenderTarget nativeMain = minecraft.getMainRenderTarget();
        try {
            if (!isEnabled()) {
                this.releaseAll();
                return;
            }
            // Keep resources during temporary bypasses, such as using a spyglass.
            if (!shouldApply(minecraft) || !hasValidWindow(minecraft.getWindow())) {
                return;
            }
            if (this.getOrCreateProjectionEffect(minecraft) == null) {
                this.releaseWorldTargets();
                return;
            }

            int quality = SodiumExtraClientMod.options().extraSettings.paniniProjectionQuality;
            if (this.failedQuality != quality) {
                this.failedQuality = 0;
            }
            float requestedScale = quality / 100.0F;
            int maxTextureSize = RenderSystem.maxSupportedTextureSize();
            float scale = Math.min(requestedScale, (float) maxTextureSize / Math.max(nativeMain.width, nativeMain.height));
            int width = Math.min(maxTextureSize, (int) Math.ceil(nativeMain.width * scale));
            int height = Math.min(maxTextureSize, (int) Math.ceil(nativeMain.height * scale));
            if (scale < requestedScale && (this.limitedWidth != nativeMain.width || this.limitedHeight != nativeMain.height)) {
                SodiumExtraClientMod.logger().warn("Panini world resolution is limited to {}x{} by the GPU's maximum texture size of {}", width, height, maxTextureSize);
                this.limitedWidth = nativeMain.width;
                this.limitedHeight = nativeMain.height;
            }

            if (this.failedQuality == quality || (width <= nativeMain.width && height <= nativeMain.height)) {
                this.releaseWorldTargets();
            } else {
                try {
                    this.prepareWorldTargets(minecraft, nativeMain, width, height);
                    this.failedQuality = 0;
                } catch (IOException | RuntimeException exception) {
                    this.failedQuality = quality;
                    this.releaseWorldTargets();
                    SodiumExtraClientMod.logger().warn("Unable to allocate {}x{} Panini world targets; using native resolution until the quality setting or window size changes", width, height, exception);
                }
            }

            if (this.worldTarget == null && this.getOrCreatePostChain(minecraft, nativeMain, minecraft.getWindow()) == null) {
                return;
            }
            this.applying = true;
            this.renderingWorld = true;
            this.frameNativeMain = nativeMain;
        } finally {
            // Allocation, destruction and chain loading can all unbind the FBO.
            // Vanilla clears/draws the world before its next explicit target bind.
            this.worldTarget(nativeMain).bindWrite(true);
        }
    }

    public RenderTarget worldTarget(RenderTarget nativeMain) {
        return this.renderingWorld && this.worldTarget != null ? this.worldTarget : nativeMain;
    }

    public RenderTarget nativeTarget(RenderTarget target) {
        return this.renderingWorld ? this.frameNativeMain : target;
    }

    public RenderTarget outlineTarget(RenderTarget nativeOutline) {
        return this.renderingWorld && nativeOutline != null && this.worldOutlineTarget != null ? this.worldOutlineTarget : nativeOutline;
    }

    public RenderTarget transparencyTarget(String name, RenderTarget nativeTarget) {
        return this.renderingWorld && nativeTarget != null && this.worldTransparencyChain != null
                ? this.worldTransparencyChain.getTempTarget(name) : nativeTarget;
    }

    public PostChain transparencyChain(PostChain nativeChain) {
        return this.renderingWorld && nativeChain != null && this.worldTransparencyChain != null ? this.worldTransparencyChain : nativeChain;
    }

    public boolean isApplying() {
        return this.applying;
    }

    public boolean isRenderingWorld() {
        return this.renderingWorld;
    }

    /**
     * Vanilla rebinds targets with bindWrite(false), which keeps the previous
     * viewport. That is only correct while every target is screen-sized, so the
     * viewport has to follow each bind while the world target is larger.
     */
    public boolean forcesViewport() {
        return this.renderingWorld;
    }

    public void finish(Minecraft minecraft, double fieldOfView, float partialTick) {
        boolean offscreen = this.renderingWorld && this.worldTarget != null;
        this.renderingWorld = false;
        if (!this.applying) {
            return;
        }
        RenderTarget nativeMain = minecraft.getMainRenderTarget();
        Window window = minecraft.getWindow();
        try {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.resetTextureMatrix();
            if (offscreen) {
                // Resolve the world even if this frame's projection is unusable.
                if (!setConfigUniform(this.projectionEffect.safeGetUniform(CONFIG_UNIFORM), window, fieldOfView, false)) {
                    this.projectionEffect.safeGetUniform(CONFIG_UNIFORM).set(0.0F, 1.0F, 1.0F, 0.0F);
                }
                this.drawProjection(this.worldTarget, nativeMain);
            } else if (updateUniforms(this.postChain, window, fieldOfView, false)) {
                this.postChain.process(partialTick);
            }

            AccessorLevelRenderer levelRenderer = (AccessorLevelRenderer) minecraft.levelRenderer;
            RenderTarget outline = levelRenderer.sodiumExtra$getEntityOutlineTarget();
            if (outline != null) {
                if (((EntityOutlineState) minecraft.levelRenderer).sodiumExtra$hasEntityOutline()) {
                    boolean projected;
                    if (offscreen && this.worldOutlineTarget != null) {
                        projected = setConfigUniform(this.projectionEffect.safeGetUniform(CONFIG_UNIFORM), window, fieldOfView, true);
                        if (projected) {
                            this.drawProjection(this.worldOutlineTarget, outline);
                        }
                    } else {
                        PostChain chain = this.getOrCreateOutlinePostChain(minecraft, outline);
                        projected = chain != null && updateUniforms(chain, window, fieldOfView, true);
                        if (projected) {
                            chain.process(partialTick);
                        }
                    }
                    PostChain outlineEffect = levelRenderer.sodiumExtra$getEntityOutlinePostChain();
                    if (projected && outlineEffect != null) {
                        // Sobel/blur run at native resolution after the raw mask
                        // is projected, so glow thickness does not warp or scale.
                        outlineEffect.process(partialTick);
                    } else {
                        outline.clear(Minecraft.ON_OSX);
                    }
                } else if (offscreen && this.worldOutlineTarget != null) {
                    // Vanilla cleared the scaled mask, leaving last frame's native glow.
                    outline.clear(Minecraft.ON_OSX);
                }
            }
        } finally {
            this.applying = false;
            this.frameNativeMain = null;
            // The hand, screen effects and GUI use the native projection/viewport.
            nativeMain.bindWrite(true);
        }
    }

    public void abort() {
        if (this.renderingWorld) {
            this.renderingWorld = false;
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
        this.applying = false;
        this.frameNativeMain = null;
    }

    public void resize() {
        this.abort();
        this.releaseWorldTargets();
        this.failedQuality = 0;
        this.limitedWidth = 0;
        this.limitedHeight = 0;
    }

    public void close() {
        this.abort();
        this.releaseAll();
        this.postChainUnavailable = false;
        this.projectionEffectUnavailable = false;
        this.failedQuality = 0;
        this.limitedWidth = 0;
        this.limitedHeight = 0;
    }

    private void releaseAll() {
        this.releaseWorldTargets();
        this.closeOutline();
        if (this.postChain != null) {
            this.postChain.close();
            this.postChain = null;
            this.postChainWidth = -1;
            this.postChainHeight = -1;
        }
        if (this.projectionEffect != null) {
            this.projectionEffect.close();
            this.projectionEffect = null;
        }
    }

    private void prepareWorldTargets(Minecraft minecraft, RenderTarget nativeMain, int width, int height) throws IOException {
        AccessorLevelRenderer levelRenderer = (AccessorLevelRenderer) minecraft.levelRenderer;
        RenderTarget nativeOutline = levelRenderer.sodiumExtra$getEntityOutlineTarget();
        if (!matches(this.worldTarget, nativeMain, width, height)
                || (nativeOutline == null ? this.worldOutlineTarget != null : !matches(this.worldOutlineTarget, nativeOutline, width, height))) {
            this.releaseWorldTargets();
            this.worldTarget = PaniniRenderTargets.create(width, height, nativeMain);
            if (nativeOutline != null) {
                this.worldOutlineTarget = PaniniRenderTargets.create(width, height, nativeOutline);
                this.worldOutlineTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            }
        }

        PostChain nativeChain = levelRenderer.sodiumExtra$getTransparencyChain();
        if (this.nativeTransparencyChain != nativeChain) {
            this.closeTransparency();
            if (nativeChain != null) {
                this.worldTransparencyChain = PaniniRenderTargets.createChain(minecraft, this.worldTarget, TRANSPARENCY_CHAIN_ID);
                this.worldTransparencyChain.resize(width, height);
                for (String name : List.of("translucent", "itemEntity", "particles", "weather", "clouds")) {
                    if (this.worldTransparencyChain.getTempTarget(name) == null) {
                        throw new IOException("Missing Fabulous render target: " + name);
                    }
                }
                this.nativeTransparencyChain = nativeChain;
            }
        }
    }

    private static boolean matches(RenderTarget target, RenderTarget nativeTarget, int width, int height) {
        return target != null && target.width == width && target.height == height
                && RenderTargetFactory.INSTANCE.matches(target, nativeTarget);
    }

    private void releaseWorldTargets() {
        this.closeTransparency();
        if (this.worldTarget != null) {
            this.worldTarget.destroyBuffers();
            this.worldTarget = null;
        }
        if (this.worldOutlineTarget != null) {
            this.worldOutlineTarget.destroyBuffers();
            this.worldOutlineTarget = null;
        }
    }

    private void closeTransparency() {
        if (this.worldTransparencyChain != null) {
            this.worldTransparencyChain.close();
            this.worldTransparencyChain = null;
        }
        this.nativeTransparencyChain = null;
    }

    // Mirrors PostPass#process, but samples and writes targets of different sizes.
    private void drawProjection(RenderTarget source, RenderTarget output) {
        // Fabulous's chain resets all its input textures to nearest filtering.
        // The projection filter requires bilinear sampling on every frame.
        source.setFilterMode(GL_LINEAR);
        int width = output.width;
        int height = output.height;
        RenderSystem.viewport(0, 0, width, height);
        this.projectionEffect.setSampler("DiffuseSampler", source::getColorTextureId);
        this.projectionEffect.safeGetUniform("ProjMat").set(new Matrix4f().setOrtho(0.0F, width, 0.0F, height, 0.1F, 1000.0F));
        this.projectionEffect.safeGetUniform("InSize").set((float) source.width, (float) source.height);
        this.projectionEffect.safeGetUniform("OutSize").set((float) width, (float) height);
        this.projectionEffect.apply();
        output.bindWrite(false);
        RenderSystem.depthFunc(GL_ALWAYS);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.addVertex(0.0F, 0.0F, 500.0F);
        builder.addVertex(width, 0.0F, 500.0F);
        builder.addVertex(width, height, 500.0F);
        builder.addVertex(0.0F, height, 500.0F);
        BufferUploader.draw(builder.buildOrThrow());
        RenderSystem.depthFunc(GL_LEQUAL);
        this.projectionEffect.clear();
        output.unbindWrite();
        source.unbindRead();
    }

    private EffectInstance getOrCreateProjectionEffect(Minecraft minecraft) {
        if (this.projectionEffect == null && !this.projectionEffectUnavailable) {
            try {
                this.projectionEffect = new EffectInstance(minecraft.getResourceManager(), PROGRAM_NAME);
            } catch (IOException | JsonSyntaxException exception) {
                this.projectionEffectUnavailable = true;
                if (WARNED_MISSING_PROGRAM.compareAndSet(false, true)) {
                    SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the shader program '{}' is unavailable", PROGRAM_NAME, exception);
                }
            }
        }
        return this.projectionEffect;
    }

    private static boolean isEnabled() {
        SodiumExtraGameOptions.ExtraSettings settings = SodiumExtraClientMod.options().extraSettings;
        return settings.paniniProjection && settings.paniniProjectionStrength > 0 && !settings.preventShaders
                && !IrisCompat.isShaderPackInUse();
    }

    private static boolean shouldApply(Minecraft minecraft) {
        return minecraft.player != null && !minecraft.player.isScoping() && !minecraft.gameRenderer.isPanoramicMode()
                && !isFrustumCaptured(minecraft);
    }

    private static boolean isFrustumCaptured(Minecraft minecraft) {
        return minecraft.levelRenderer != null && ((AccessorLevelRenderer) minecraft.levelRenderer).sodiumExtra$getCapturedFrustum() != null;
    }

    private static boolean hasValidWindow(Window window) {
        return window != null && window.getWidth() > 0 && window.getHeight() > 0;
    }

    private PostChain getOrCreatePostChain(Minecraft minecraft, RenderTarget mainTarget, Window window) {
        if (this.postChainUnavailable) {
            return null;
        }

        int width = window.getWidth();
        int height = window.getHeight();
        try {
            if (this.postChain == null) {
                this.postChain = PaniniRenderTargets.createChain(minecraft, mainTarget, POST_CHAIN_ID);
            }
            if (this.postChainWidth != width || this.postChainHeight != height) {
                this.postChain.resize(width, height);
                this.postChainWidth = width;
                this.postChainHeight = height;
            }
        } catch (IOException | RuntimeException exception) {
            if (this.postChain != null) {
                this.postChain.close();
                this.postChain = null;
            }
            this.postChainWidth = -1;
            this.postChainHeight = -1;
            this.postChainUnavailable = true;
            if (WARNED_MISSING_CHAIN.compareAndSet(false, true)) {
                SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the post effect '{}' is unavailable", POST_CHAIN_ID, exception);
            }
            return null;
        }

        return this.postChain;
    }

    private PostChain getOrCreateOutlinePostChain(Minecraft minecraft, RenderTarget target) {
        if (this.outlineTarget != target) {
            this.closeOutline();
            this.outlineTarget = target;
        }
        if (target == null || this.outlinePostChainUnavailable) {
            return null;
        }

        try {
            if (this.outlinePostChain == null) {
                // In 1.21.1 a PostChain owns fixed input/output targets, so the
                // outline needs its own chain rather than reusing the main one.
                this.outlinePostChain = PaniniRenderTargets.createChain(minecraft, target, POST_CHAIN_ID);
            }
            if (this.outlineWidth != target.viewWidth || this.outlineHeight != target.viewHeight) {
                this.outlinePostChain.resize(target.viewWidth, target.viewHeight);
                this.outlineWidth = target.viewWidth;
                this.outlineHeight = target.viewHeight;
            }
        } catch (IOException | RuntimeException exception) {
            if (this.outlinePostChain != null) {
                this.outlinePostChain.close();
                this.outlinePostChain = null;
            }
            this.outlineWidth = -1;
            this.outlineHeight = -1;
            this.outlinePostChainUnavailable = true;
            SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection to glowing outlines because the post effect '{}' is unavailable", POST_CHAIN_ID, exception);
            return null;
        }

        return this.outlinePostChain;
    }

    private static boolean updateUniforms(PostChain postChain, Window window, double fieldOfView, boolean outlineMask) {
        List<PostPass> passes = ((AccessorPostChain) postChain).sodiumExtra$getPasses();
        for (PostPass pass : passes) {
            var configUniform = pass.getEffect().getUniform(CONFIG_UNIFORM);
            if (configUniform != null) {
                return setConfigUniform(configUniform, window, fieldOfView, outlineMask);
            }
        }

        if (WARNED_MISSING_UNIFORM.compareAndSet(false, true)) {
            SodiumExtraClientMod.logger().warn("Unable to apply Panini Projection because the '{}' post uniform is unavailable", CONFIG_UNIFORM);
        }

        return false;
    }

    private static boolean setConfigUniform(AbstractUniform configUniform, Window window, double fieldOfView, boolean outlineMask) {
        float strength = SodiumExtraClientMod.options().extraSettings.paniniProjectionStrength / 100.0F;
        float verticalExtent = (float) Math.tan(Math.toRadians(fieldOfView) * 0.5);
        float horizontalExtent = verticalExtent * window.getWidth() / (float) window.getHeight();

        if (!Float.isFinite(horizontalExtent) || !Float.isFinite(verticalExtent)) {
            return false;
        }

        configUniform.set(strength, horizontalExtent, verticalExtent, outlineMask ? 1.0F : 0.0F);
        return true;
    }

    private void closeOutline() {
        if (this.outlinePostChain != null) {
            this.outlinePostChain.close();
            this.outlinePostChain = null;
        }
        this.outlineTarget = null;
        this.outlineWidth = -1;
        this.outlineHeight = -1;
        this.outlinePostChainUnavailable = false;
    }
}

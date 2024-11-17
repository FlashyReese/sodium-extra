package me.flashyreese.mods.sodiumextra.mixin.optimizations.draw_helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.flashyreese.mods.sodiumextra.client.render.vertex.formats.TextureColorVertex;
import me.flashyreese.mods.sodiumextra.client.render.vertex.formats.TextureVertex;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiGraphics.class, priority = 1500)
public abstract class MixinGuiGraphics {

    @Shadow
    @Final
    private PoseStack pose;

    @Shadow
    @Final
    private MultiBufferSource.BufferSource bufferSource;

    @Shadow
    @Deprecated
    protected abstract void flushIfUnmanaged();

    /**
     * @author FlashyReese
     * @reason Impl Sodium's vertex writer
     */
    @Inject(method = "fillGradient(Lcom/mojang/blaze3d/vertex/VertexConsumer;IIIIIII)V", at = @At(value = "HEAD"), cancellable = true)
    private void fillGradient(VertexConsumer vertexConsumer, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd, CallbackInfo ci) {
        VertexBufferWriter writer = VertexBufferWriter.of(vertexConsumer);
        Matrix4f matrix4f = this.pose.last().pose();
        colorStart = ColorARGB.toABGR(colorStart);
        colorEnd = ColorARGB.toABGR(colorEnd);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final long buffer = stack.nmalloc(4 * ColorVertex.STRIDE);
            long ptr = buffer;

            ColorVertex.put(ptr, matrix4f, startX, startY, z, colorStart);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, startX, endY, z, colorEnd);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, endX, endY, z, colorEnd);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, endX, startY, z, colorStart);
            ptr += ColorVertex.STRIDE;

            writer.push(stack, buffer, 4, ColorVertex.FORMAT);
        }
        ci.cancel();
    }

    /**
     * @author FlashyReese
     * @reason Impl Sodium's vertex writer
     */
    @Inject(method = "fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V", at = @At(value = "HEAD"), cancellable = true)
    public void fill(RenderType type, int x1, int y1, int x2, int y2, int z, int color, CallbackInfo ci) {
        Matrix4f matrix4f = this.pose.last().pose();
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int i = y1;
            y1 = y2;
            y2 = i;
        }
        VertexConsumer vertexConsumer = this.bufferSource.getBuffer(type);
        VertexBufferWriter writer = VertexBufferWriter.of(vertexConsumer);
        color = ColorARGB.toABGR(color);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final long buffer = stack.nmalloc(4 * ColorVertex.STRIDE);
            long ptr = buffer;

            ColorVertex.put(ptr, matrix4f, x1, y1, z, color);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, x1, y2, z, color);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, x2, y2, z, color);
            ptr += ColorVertex.STRIDE;

            ColorVertex.put(ptr, matrix4f, x2, y1, z, color);
            ptr += ColorVertex.STRIDE;

            writer.push(stack, buffer, 4, ColorVertex.FORMAT);
        }
        this.flushIfUnmanaged();
        ci.cancel();
    }


    /**
     * @author FlashyReese
     * @reason Impl Sodium's vertex writer
     */
    @Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V", at = @At(value = "HEAD"), cancellable = true)
    public void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, CallbackInfo ci) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = this.pose.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        VertexBufferWriter writer = VertexBufferWriter.of(bufferBuilder);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final long buffer = stack.nmalloc(4 * TextureVertex.STRIDE);
            long ptr = buffer;

            TextureVertex.write(ptr, matrix4f, x1, y1, z, u1, v1);
            ptr += TextureVertex.STRIDE;

            TextureVertex.write(ptr, matrix4f, x1, y2, z, u1, v2);
            ptr += TextureVertex.STRIDE;

            TextureVertex.write(ptr, matrix4f, x2, y2, z, u2, v2);
            ptr += TextureVertex.STRIDE;

            TextureVertex.write(ptr, matrix4f, x2, y1, z, u2, v1);
            ptr += TextureVertex.STRIDE;

            writer.push(stack, buffer, 4, TextureVertex.FORMAT);
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        ci.cancel();
    }

    /**
     * @author FlashyReese
     * @reason Impl Sodium's vertex writer
     */
    @Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V", at = @At(value = "HEAD"), cancellable = true)
    public void drawTexturedQuad(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha, CallbackInfo ci) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        Matrix4f matrix4f = this.pose.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        VertexBufferWriter writer = VertexBufferWriter.of(bufferBuilder);
        int color = ColorABGR.pack(red, green, blue, alpha);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            final long buffer = stack.nmalloc(4 * TextureColorVertex.STRIDE);
            long ptr = buffer;

            TextureColorVertex.write(ptr, matrix4f, x1, y1, z, color, u1, v1);
            ptr += TextureColorVertex.STRIDE;

            TextureColorVertex.write(ptr, matrix4f, x1, y2, z, color, u1, v2);
            ptr += TextureColorVertex.STRIDE;

            TextureColorVertex.write(ptr, matrix4f, x2, y2, z, color, u2, v2);
            ptr += TextureColorVertex.STRIDE;

            TextureColorVertex.write(ptr, matrix4f, x2, y1, z, color, u2, v1);
            ptr += TextureColorVertex.STRIDE;

            writer.push(stack, buffer, 4, TextureColorVertex.FORMAT);
        }
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        RenderSystem.disableBlend();
        ci.cancel();
    }
}

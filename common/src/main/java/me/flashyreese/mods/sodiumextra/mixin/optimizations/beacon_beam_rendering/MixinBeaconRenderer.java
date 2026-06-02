package me.flashyreese.mods.sodiumextra.mixin.optimizations.beacon_beam_rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.flashyreese.mods.sodiumextra.compat.IrisCompat;
import net.caffeinemc.mods.sodium.api.math.MatrixHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.EntityVertex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BeaconRenderer.class, priority = 1500)
public abstract class MixinBeaconRenderer {

    /**
     * @author FlashyReese
     * @reason Use optimized vertex writer, also avoids unnecessary allocations
     */
    @Inject(method = "renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/resources/ResourceLocation;FFJIIIFF)V", at = @At(value = "HEAD"), cancellable = true)
    private static void optimizeRenderBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, ResourceLocation resourceLocation, float tickDelta, float heightScale, long worldTime, int yOffset, int maxY, int color, float innerRadius, float outerRadius, CallbackInfo ci) {
        ci.cancel();
        if (IrisCompat.isIrisPresent()) {
            if (IrisCompat.isRenderingShadowPass()) {
                return;
            }
        }

        int height = yOffset + maxY;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float time = (float) Math.floorMod(worldTime, 40) + tickDelta;
        float negativeTime = maxY < 0 ? time : -time;
        float fractionalPart = Mth.frac(negativeTime * 0.2F - (float) Mth.floor(negativeTime * 0.1F));
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(time * 2.25F - 45.0F));
        float innerX1;
        float innerZ2;
        float innerX3 = -innerRadius;
        float innerZ4 = -innerRadius;
        float innerV2 = -1.0F + fractionalPart;
        float innerV1 = (float) maxY * heightScale * (0.5F / innerRadius) + innerV2;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            long buffer = stack.nmalloc(2 * 16 * EntityVertex.STRIDE);
            long ptr = buffer;
            // Note: ModelVertex color takes in ABGR
            ptr = writeBeamLayerVertices(ptr, poseStack, ColorARGB.toABGR(color), yOffset, height, 0.0F, innerRadius, innerRadius, 0.0F, innerX3, 0.0F, 0.0F, innerZ4, innerV1, innerV2);
            VertexBufferWriter.of(multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, false))).push(stack, buffer, 16, EntityVertex.FORMAT);

            poseStack.popPose();
            innerX1 = -outerRadius;
            float outerZ1 = -outerRadius;
            innerZ2 = -outerRadius;
            innerX3 = -outerRadius;
            innerV2 = -1.0F + fractionalPart;
            innerV1 = (float) maxY * heightScale + innerV2;

            buffer = ptr;
            ptr = writeBeamLayerVertices(ptr, poseStack, ColorARGB.toABGR(color, 32), yOffset, height, innerX1, outerZ1, outerRadius, innerZ2, innerX3, outerRadius, outerRadius, outerRadius, innerV1, innerV2);
            VertexBufferWriter.of(multiBufferSource.getBuffer(RenderType.beaconBeam(resourceLocation, true))).push(stack, buffer, 16, EntityVertex.FORMAT);
        }
        poseStack.popPose();
    }

    @Unique
    private static long writeBeamLayerVertices(long ptr, PoseStack poseStack, int color, int yOffset, int height, float x1, float z1, float x2, float z2, float x3, float z3, float x4, float z4, float v1, float v2) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f positionMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();

        var normal = MatrixHelper.transformNormal(normalMatrix, false, (float) 0.0, (float) 1.0, (float) 0.0);

        ptr = transformAndWriteVertex(ptr, positionMatrix, x1, height, z1, color, 1.0f, v1, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x1, yOffset, z1, color, 1.0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x2, yOffset, z2, color, 0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x2, height, z2, color, 0f, v1, normal);

        ptr = transformAndWriteVertex(ptr, positionMatrix, x4, height, z4, color, 1.0f, v1, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x4, yOffset, z4, color, 1.0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x3, yOffset, z3, color, 0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x3, height, z3, color, 0f, v1, normal);

        ptr = transformAndWriteVertex(ptr, positionMatrix, x2, height, z2, color, 1.0f, v1, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x2, yOffset, z2, color, 1.0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x4, yOffset, z4, color, 0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x4, height, z4, color, 0f, v1, normal);

        ptr = transformAndWriteVertex(ptr, positionMatrix, x3, height, z3, color, 1.0f, v1, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x3, yOffset, z3, color, 1.0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x1, yOffset, z1, color, 0f, v2, normal);
        ptr = transformAndWriteVertex(ptr, positionMatrix, x1, height, z1, color, 0f, v1, normal);
        return ptr;
    }

    @Unique
    private static long transformAndWriteVertex(long ptr, Matrix4f positionMatrix, float x, float y, float z, int color, float u, float v, int normal) {
        float transformedX = MatrixHelper.transformPositionX(positionMatrix, x, y, z);
        float transformedY = MatrixHelper.transformPositionY(positionMatrix, x, y, z);
        float transformedZ = MatrixHelper.transformPositionZ(positionMatrix, x, y, z);

        EntityVertex.write(ptr, transformedX, transformedY, transformedZ, color, u, v, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, normal);
        ptr += EntityVertex.STRIDE;
        return ptr;
    }

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "HEAD"), cancellable = true)
    public void render(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        Frustum frustum = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getCullingFrustum();
        AABB box = new AABB(
                beaconBlockEntity.getBlockPos().getX() - 1.0,
                beaconBlockEntity.getBlockPos().getY() - 1.0,
                beaconBlockEntity.getBlockPos().getZ() - 1.0,
                beaconBlockEntity.getBlockPos().getX() + 1.0,
                beaconBlockEntity.getBlockPos().getY() + (beaconBlockEntity.getBeamSections().isEmpty() ? 1.0 : 1024.0), // todo: probably want to limit this to max height vanilla overshoots as well
                beaconBlockEntity.getBlockPos().getZ() + 1.0);

        if (!frustum.isVisible(box)) {
            ci.cancel();
        }
    }
}

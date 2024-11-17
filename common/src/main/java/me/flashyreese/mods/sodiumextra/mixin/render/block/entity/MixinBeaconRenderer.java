package me.flashyreese.mods.sodiumextra.mixin.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = BeaconRenderer.class, priority = 999)
public abstract class MixinBeaconRenderer {

    @Shadow
    protected static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float f, long l, int i, int j, int k) {
    }

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "HEAD"), cancellable = true)
    public void render(BeaconBlockEntity beaconBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.beaconBeam)
            ci.cancel();
    }

    @Coerce
    @Redirect(method = "render(Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BeaconRenderer;renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FJIII)V"))
    private void modifyMaxY(PoseStack matrices, MultiBufferSource vertexConsumers, float tickDelta, long worldTime, int yOffset, int maxY, int color, BeaconBlockEntity beaconBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
        if (maxY == 1024 && SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight) {
            int lastSegment = beaconBlockEntity.getBlockPos().getY() + yOffset;
            maxY = Objects.requireNonNull(beaconBlockEntity.getLevel()).getMaxBuildHeight() - lastSegment;
        }
        renderBeaconBeam(matrices, vertexConsumers, tickDelta, worldTime, yOffset, maxY, color);
    }
}

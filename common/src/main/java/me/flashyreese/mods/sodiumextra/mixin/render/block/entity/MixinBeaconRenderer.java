package me.flashyreese.mods.sodiumextra.mixin.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = BeaconRenderer.class, priority = 999)
public abstract class MixinBeaconRenderer<T extends BlockEntity & BeaconBeamOwner> {

    @Shadow
    private static void renderBeaconBeam(PoseStack poseStack, MultiBufferSource multiBufferSource, float tickDelta, float g, long worldTime, int yOffset, int maxY, int color) {
    }

    // Todo: Fix neo
    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MutliBufferSource;IILnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void render(T blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, Vec3 vec3, CallbackInfo ci) {
        if (!SodiumExtraClientMod.options().renderSettings.beaconBeam)
            ci.cancel();
    }

    // Todo: Fix neo
    @Coerce
    @Redirect(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MutliBufferSource;IILnet/minecraft/world/phys/Vec3;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BeaconRenderer;renderBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FFJIII)V"), require = 0)
    private void modifyMaxY(PoseStack poseStack, MultiBufferSource multiBufferSource, float tickDelta, float g, long worldTime, int yOffset, int maxY, int color, BlockEntity beaconBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
        if (maxY == 2048 && SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight) {
            int lastSegment = beaconBlockEntity.getBlockPos().getY() + yOffset;
            maxY = Objects.requireNonNull(beaconBlockEntity.getLevel()).getMaxY() - lastSegment;
        }
        renderBeaconBeam(poseStack, multiBufferSource, tickDelta, g, worldTime, yOffset, maxY, color);
    }
}

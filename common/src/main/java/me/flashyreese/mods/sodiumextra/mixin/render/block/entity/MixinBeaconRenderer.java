package me.flashyreese.mods.sodiumextra.mixin.render.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(value = BeaconRenderer.class, priority = 999)
public abstract class MixinBeaconRenderer<T extends BlockEntity & BeaconBeamOwner> {

    @Unique
    private BeaconRenderState sodium_extra$beaconRenderState;

    @Shadow
    private static void submitBeaconBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float f, float g, int i, int j, int k) {
    }

    @Inject(method = {"submit(Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V"}, at = @At(value = "HEAD"), cancellable = true, require = 1)
    public void render(BeaconRenderState beaconRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        this.sodium_extra$beaconRenderState = beaconRenderState;
        if (!SodiumExtraClientMod.options().renderSettings.beaconBeam)
            ci.cancel();
    }

    @Coerce
    @Redirect(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BeaconRenderer;submitBeaconBeam(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;FFIII)V"))
    private void modifyMaxY(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float f, float g, int yOffset, int maxY, int color) {
        if (maxY == 2048 && SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight) {
            int lastSegment = this.sodium_extra$beaconRenderState.blockPos.getY() + yOffset;
            maxY = Objects.requireNonNull(Minecraft.getInstance().level).getMaxY() - lastSegment;
        }
        submitBeaconBeam(poseStack, submitNodeCollector, f, g, yOffset, maxY, color);
    }
}

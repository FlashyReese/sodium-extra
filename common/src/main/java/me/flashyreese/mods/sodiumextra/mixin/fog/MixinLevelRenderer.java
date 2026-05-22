package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.mojang.blaze3d.vertex.PoseStack;
import me.flashyreese.mods.sodiumextra.client.fog.FogOverrideState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderClouds", at = @At("HEAD"))
    private void setupCloudFog(PoseStack poseStack, Matrix4f projectionMatrix, Matrix4f modelViewMatrix, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (this.minecraft.level == null) {
            return;
        }

        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        float viewDistance = this.minecraft.gameRenderer.getRenderDistance();
        boolean thickFog = this.minecraft.level.effects().isFoggyAt(Mth.floor(cameraX), Mth.floor(cameraY)) || this.minecraft.gui.getBossOverlay().shouldCreateWorldFog();

        FogOverrideState.whileSettingUpCloudFog(() -> FogRenderer.setupFog(camera, FogRenderer.FogMode.FOG_TERRAIN, Math.max(viewDistance, 32.0F), thickFog, tickDelta));
    }
}

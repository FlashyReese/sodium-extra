package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuSurface;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vulkan.VulkanGpuSurface;
import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VkOffset3D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuSurface.class)
public class MixinVulkanGpuSurface {
    @Shadow
    private int swapchainWidth;

    @Shadow
    private int swapchainHeight;

    /*
     * Keep the Vulkan swapchain at the same reduced size as Minecraft's render
     * target. Without this, MoltenVK has to scale the final vkCmdBlitImage into
     * the native Retina-sized swapchain every frame, which tested visually correct
     * but caused severe low-percentile frame drops on macOS.
     */
    @ModifyVariable(method = "configure", at = @At("HEAD"), argsOnly = true)
    private GpuSurface.Configuration reduceSwapchainConfiguration(GpuSurface.Configuration config) {
        return MacReducedResolution.reduceSurfaceConfiguration(config);
    }

    @Inject(method = "blitFromTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkCmdBlitImage(Lorg/lwjgl/vulkan/VkCommandBuffer;JIJILorg/lwjgl/vulkan/VkImageBlit$Buffer;I)V", remap = false))
    private void scalePresentedTexture(CommandEncoderBackend commandEncoder, GpuTextureView textureView, CallbackInfo ci, @Local(ordinal = 0) VkImageBlit.Buffer blitRegion) {
        if (!MacReducedResolution.shouldScalePresentation(textureView.getWidth(0), textureView.getHeight(0), this.swapchainWidth, this.swapchainHeight)) {
            return;
        }

        VkOffset3D.Buffer dstOffsets = blitRegion.dstOffsets();
        int position = dstOffsets.position();
        dstOffsets.position(0);
        dstOffsets.x(0).y(this.swapchainHeight).z(0);
        dstOffsets.position(1);
        dstOffsets.x(this.swapchainWidth).y(0).z(1);
        dstOffsets.position(position);
    }
}

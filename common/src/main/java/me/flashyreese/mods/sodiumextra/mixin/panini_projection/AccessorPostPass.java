package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PostPass.class)
public interface AccessorPostPass {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> sodiumExtra$getCustomUniforms();
}

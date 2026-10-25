package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface AccessorLevelRenderer {
    @Accessor("entityOutlineTarget")
    RenderTarget sodiumExtra$getEntityOutlineTarget();
}

package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface AccessorLevelRenderer {
    @Accessor("capturedFrustum")
    Frustum sodiumExtra$getCapturedFrustum();
}

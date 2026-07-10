package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PostChain.class)
public interface AccessorPostChain {
    @Accessor("passes")
    List<PostPass> sodiumExtra$getPasses();
}

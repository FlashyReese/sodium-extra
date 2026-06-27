package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.flashyreese.mods.sodiumextra.client.fog.FogShaderTransformer;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Applies the fog-shape transform only to Sodium's terrain shader after vanilla resolves the source.
 */
@Mixin(ShaderManager.class)
public class MixinShaderManager {
    private static final Identifier SODIUM_TERRAIN_SHADER = Identifier.fromNamespaceAndPath("sodium", "blocks/block_layer_opaque");

    @ModifyReturnValue(method = "getShader", at = @At("RETURN"))
    private String sodiumExtra$injectRenderDistanceShape(String source, Identifier location) {
        if (location.equals(SODIUM_TERRAIN_SHADER)) {
            return FogShaderTransformer.injectRenderDistanceShape(source);
        }

        return source;
    }
}

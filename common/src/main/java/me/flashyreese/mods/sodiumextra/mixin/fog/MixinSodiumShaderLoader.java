package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.flashyreese.mods.sodiumextra.client.fog.FogShaderTransformer;
import net.caffeinemc.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ShaderLoader.class, remap = false)
public class MixinSodiumShaderLoader {
    @ModifyReturnValue(method = "getShaderSource", at = @At("RETURN"))
    private static String sodiumExtra$injectSodiumShaderSource(String source, Identifier location) {
        return FogShaderTransformer.injectSodiumShaderSource(source, location);
    }
}

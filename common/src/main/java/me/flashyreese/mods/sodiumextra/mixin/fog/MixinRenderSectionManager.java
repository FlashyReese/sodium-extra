package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.systems.RenderSystem;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
    @Shadow
    private float getRenderDistance() {
        throw new AssertionError();
    }

    @ModifyReturnValue(method = "getEffectiveRenderDistance", at = @At("RETURN"))
    private float sodiumExtra$expandCylindricalFogCullDistance(float distance) {
        return FogDistanceHelper.expandCylindricalCullDistance(distance, RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd(), this.getRenderDistance());
    }

    @ModifyReturnValue(method = "getSearchDistance", at = @At("RETURN"))
    private float sodiumExtra$expandCylindricalFogSearchDistance(float distance) {
        return FogDistanceHelper.expandCylindricalCullDistance(distance, RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd(), this.getRenderDistance());
    }
}

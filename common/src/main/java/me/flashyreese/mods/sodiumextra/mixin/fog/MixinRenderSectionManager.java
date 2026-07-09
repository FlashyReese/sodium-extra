package me.flashyreese.mods.sodiumextra.mixin.fog;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.CullType;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
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
    private float sodiumExtra$expandCylindricalFogCullDistance(float distance, FogParameters fogParameters) {
        // Sodium's culler only knows one distance, so use the cylindrical shader's tallest visible axis.
        return FogDistanceHelper.expandCylindricalCullDistance(distance, fogParameters.renderStart(), fogParameters.renderEnd(), this.getRenderDistance());
    }

    @ModifyReturnValue(method = "getSearchDistanceForCullType", at = @At("RETURN"))
    private float sodiumExtra$expandCylindricalFogCullTypeDistance(float distance, CullType cullType, FogParameters fogParameters) {
        // REGULAR/WIDE are not fog-culled in Sodium, but they still gate LOCAL traversal first.
        return FogDistanceHelper.expandCylindricalCullDistance(distance, fogParameters.renderStart(), fogParameters.renderEnd(), this.getRenderDistance());
    }

    @ModifyReturnValue(method = "getSearchDistance", at = @At("RETURN"))
    private float sodiumExtra$expandCylindricalFogSearchDistance(float distance, FogParameters fogParameters) {
        // renderOutOfGraph and final SectionTree traversal use this directly.
        return FogDistanceHelper.expandCylindricalCullDistance(distance, fogParameters.renderStart(), fogParameters.renderEnd(), this.getRenderDistance());
    }
}

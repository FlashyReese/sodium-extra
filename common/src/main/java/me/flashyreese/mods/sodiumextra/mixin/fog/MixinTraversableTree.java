package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.TraversableTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TraversableTree.class)
public class MixinTraversableTree {
    @Inject(method = "cylindricalDistanceTest", at = @At("HEAD"), cancellable = true)
    private static void sodiumExtra$testExpandedCylindricalDistance(float dx, float dy, float dz, float distanceLimit, CallbackInfoReturnable<Boolean> cir) {
        if (FogDistanceHelper.isExpandedCylindricalCullDistance(distanceLimit)) {
            cir.setReturnValue(FogDistanceHelper.testExpandedCylindricalCullDistance(dx * dx + dz * dz, dy, distanceLimit));
        }
    }
}

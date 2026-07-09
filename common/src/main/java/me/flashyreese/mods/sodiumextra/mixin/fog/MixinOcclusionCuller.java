package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(OcclusionCuller.class)
public class MixinOcclusionCuller {
    @Unique
    private static final int SODIUM_EXTRA$DOWN_DIRECTION = 1 << 0;

    @Unique
    private static final int SODIUM_EXTRA$UP_DIRECTION = 1 << 1;

    @Shadow
    private float searchDistanceRegular;

    @Shadow
    private float searchDistanceLocal;

    @ModifyArgs(
            method = "processQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;visitNeighbors(Lnet/caffeinemc/mods/sodium/client/util/collections/WriteQueue;Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSection;IIILnet/minecraft/core/SectionPos;)V"
            )
    )
    private void sodiumExtra$allowExpandedCylindricalVerticalTraversal(Args args) {
        if (!FogDistanceHelper.isExpandedCylindricalCullDistance(this.searchDistanceRegular)
                && !FogDistanceHelper.isExpandedCylindricalCullDistance(this.searchDistanceLocal)) {
            return;
        }

        int verticalDirections = sodiumExtra$getOutwardVerticalDirections(args.get(5), args.get(1));
        args.set(2, (Integer)args.get(2) | verticalDirections);
        args.set(3, (Integer)args.get(3) | verticalDirections);
        args.set(4, (Integer)args.get(4) | verticalDirections);
    }

    @Unique
    private static int sodiumExtra$getOutwardVerticalDirections(SectionPos origin, RenderSection section) {
        if (origin == null || section == null) {
            return 0;
        }

        int directions = 0;
        int sectionY = section.getChunkY();
        int originY = origin.getY();

        if (sectionY <= originY) {
            directions |= SODIUM_EXTRA$DOWN_DIRECTION;
        }

        if (sectionY >= originY) {
            directions |= SODIUM_EXTRA$UP_DIRECTION;
        }

        return directions;
    }

    @Inject(method = "testDistance", at = @At("HEAD"), cancellable = true)
    private static void sodiumExtra$testExpandedCylindricalDistance(float horizontalDistanceSquared, float verticalDistance, float distanceLimit, CallbackInfoReturnable<Boolean> cir) {
        if (FogDistanceHelper.isExpandedCylindricalCullDistance(distanceLimit)) {
            cir.setReturnValue(FogDistanceHelper.testExpandedCylindricalCullDistance(horizontalDistanceSquared, verticalDistance, distanceLimit));
        }
    }
}

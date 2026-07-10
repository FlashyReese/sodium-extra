package me.flashyreese.mods.sodiumextra.mixin.fog;

import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.RenderSectionVisitor;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.OcclusionCuller;
import net.caffeinemc.mods.sodium.client.render.viewport.CameraTransform;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.collections.ReadQueue;
import net.caffeinemc.mods.sodium.client.util.collections.WriteQueue;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(OcclusionCuller.class)
public class MixinOcclusionCuller {
    @Unique
    private static final int SODIUM_EXTRA$DOWN_DIRECTION = 1 << 0;

    @Unique
    private static final int SODIUM_EXTRA$UP_DIRECTION = 1 << 1;

    @Unique
    private static final ThreadLocal<SectionPos> SODIUM_EXTRA$EXPANDED_CYLINDRICAL_ORIGIN = new ThreadLocal<>();

    @Inject(method = "processQueue", at = @At("HEAD"))
    private static void sodiumExtra$captureExpandedCylindricalTraversal(RenderSectionVisitor visitor, Viewport viewport, float searchDistance, boolean useOcclusionCulling, int frame, ReadQueue<RenderSection> readQueue, WriteQueue<RenderSection> writeQueue, CallbackInfo ci) {
        if (FogDistanceHelper.isExpandedCylindricalCullDistance(searchDistance)) {
            SODIUM_EXTRA$EXPANDED_CYLINDRICAL_ORIGIN.set(viewport.getChunkCoord());
        } else {
            SODIUM_EXTRA$EXPANDED_CYLINDRICAL_ORIGIN.remove();
        }
    }

    @Inject(method = "processQueue", at = @At("RETURN"))
    private static void sodiumExtra$clearExpandedCylindricalTraversal(RenderSectionVisitor visitor, Viewport viewport, float searchDistance, boolean useOcclusionCulling, int frame, ReadQueue<RenderSection> readQueue, WriteQueue<RenderSection> writeQueue, CallbackInfo ci) {
        SODIUM_EXTRA$EXPANDED_CYLINDRICAL_ORIGIN.remove();
    }

    @ModifyArgs(
            method = "processQueue",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/occlusion/OcclusionCuller;visitNeighbors(Lnet/caffeinemc/mods/sodium/client/util/collections/WriteQueue;Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSection;II)V"
            )
    )
    private static void sodiumExtra$allowExpandedCylindricalVerticalTraversal(Args args) {
        SectionPos origin = SODIUM_EXTRA$EXPANDED_CYLINDRICAL_ORIGIN.get();
        if (origin == null) {
            return;
        }

        int verticalDirections = sodiumExtra$getOutwardVerticalDirections(origin, args.get(1));
        args.set(2, (Integer)args.get(2) | verticalDirections);
    }

    @Inject(method = "isWithinRenderDistance", at = @At("HEAD"), cancellable = true)
    private static void sodiumExtra$isWithinExpandedCylindricalRenderDistance(CameraTransform transform, RenderSection section, float distanceLimit, CallbackInfoReturnable<Boolean> cir) {
        if (!FogDistanceHelper.isExpandedCylindricalCullDistance(distanceLimit)) {
            return;
        }

        int dx = section.getOriginX() - transform.intX;
        int dy = section.getOriginY() - transform.intY;
        int dz = section.getOriginZ() - transform.intZ;
        float nearestX = sodiumExtra$nearestToZero(dx - 1, dx + 17) - transform.fracX;
        float nearestY = sodiumExtra$nearestToZero(dy - 1, dy + 17) - transform.fracY;
        float nearestZ = sodiumExtra$nearestToZero(dz - 1, dz + 17) - transform.fracZ;

        cir.setReturnValue(FogDistanceHelper.testExpandedCylindricalCullDistance(nearestX * nearestX + nearestZ * nearestZ, nearestY, distanceLimit));
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

    @Unique
    private static int sodiumExtra$nearestToZero(int min, int max) {
        if (min > 0) {
            return min;
        }

        return Math.min(max, 0);
    }
}

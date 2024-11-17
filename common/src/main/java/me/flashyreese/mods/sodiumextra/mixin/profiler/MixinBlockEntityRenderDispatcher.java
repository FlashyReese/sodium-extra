package me.flashyreese.mods.sodiumextra.mixin.profiler;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

@Mixin(BlockEntityRenderDispatcher.class)
public class MixinBlockEntityRenderDispatcher {
    @Unique
    private static final WeakHashMap<Class<?>, String> names = new WeakHashMap<>();

    @Inject(at = @At("HEAD"), method = "setupAndRender")
    private static <T extends BlockEntity> void onRender(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo info) {
        Level level = blockEntity.getLevel();
        if (level != null) {
            String name = names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                level.getProfiler().push(name);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "setupAndRender")
    private static <T extends BlockEntity> void afterRender(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, CallbackInfo info) {
        Level level = blockEntity.getLevel();
        if (level != null) {
            String name = names.computeIfAbsent(renderer.getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                level.getProfiler().pop();
            }
        }
    }
}
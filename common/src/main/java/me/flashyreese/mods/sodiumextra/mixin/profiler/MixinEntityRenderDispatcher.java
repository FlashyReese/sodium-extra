package me.flashyreese.mods.sodiumextra.mixin.profiler;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
    private static final WeakHashMap<Class<?>, String> names = new WeakHashMap<>();

    @Shadow
    public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);

    @Inject(at = @At("HEAD"), method = "render")
    private <E extends Entity> void onRender(E entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        Level level = entity.level();
        if (level != null) {
            String name = names.computeIfAbsent(this.getRenderer(entity).getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                level.getProfiler().push(name);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private <E extends Entity> void afterRender(E entity, double x, double y, double z, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        Level level = entity.level();
        if (level != null) {
            String name = names.computeIfAbsent(this.getRenderer(entity).getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                level.getProfiler().pop();
            }
        }
    }
}
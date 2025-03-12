package me.flashyreese.mods.sodiumextra.mixin.profiler;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
    @Shadow
    public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T entity);

    private static final WeakHashMap<Class<?>, String> names = new WeakHashMap<>();

    @Inject(at = @At("HEAD"), method = "render")
    private <E extends Entity> void onRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        World world = entity.getWorld();
        if (world != null) {
            String name = names.computeIfAbsent(this.getRenderer(entity).getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                world.getProfiler().push(name);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render")
    private <E extends Entity> void afterRender(E entity, double x, double y, double z, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        World world = entity.getWorld();
        if (world != null) {
            String name = names.computeIfAbsent(this.getRenderer(entity).getClass(), Class::getSimpleName);
            if (!name.isEmpty()) {
                world.getProfiler().pop();
            }
        }
    }
}
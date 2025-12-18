package me.flashyreese.mods.sodiumextra.mixin.animation;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.common.util.AnimationStateExtended;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(TextureAtlas.class)
public class MixinTextureAtlas {
    @Shadow
    @Final
    private Identifier location;
    @Unique
    private final Map<Supplier<Boolean>, List<Identifier>> animatedSprites = Map.of(
            () -> SodiumExtraClientMod.options().animationSettings.water, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/water_still"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/water_flow")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.lava, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/lava_still"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/lava_flow")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.portal, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/nether_portal")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.fire, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/fire_0"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/fire_1"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/soul_fire_0"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/soul_fire_1"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/campfire_fire"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/campfire_log_lit"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/soul_campfire_fire"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/soul_campfire_log_lit")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.blockAnimations, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/magma"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/lantern"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sea_lantern"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/soul_lantern"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/kelp"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/kelp_plant"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/seagrass"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/tall_seagrass_top"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/tall_seagrass_bottom"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/warped_stem"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/crimson_stem"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/blast_furnace_front_on"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/smoker_front_on"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/stonecutter_saw"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/prismarine"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/respawn_anchor_top"),
                    Identifier.fromNamespaceAndPath("minecraft", "entity/conduit/wind"),
                    Identifier.fromNamespaceAndPath("minecraft", "entity/conduit/wind_vertical")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.sculkSensor, List.of(
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_catalyst_top_bloom"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_catalyst_side_bloom"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_shrieker_inner_top"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_vein"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_shrieker_can_summon_inner_top"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_sensor_tendril_inactive"),
                    Identifier.fromNamespaceAndPath("minecraft", "block/sculk_sensor_tendril_active"),
                    Identifier.fromNamespaceAndPath("minecraft", "vibration")
            )
    );

    @Redirect(method = "cycleAnimationFrames", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;tick()V"))
    public void cycleAnimationFrames(SpriteContents.AnimationState instance) {
        if (instance instanceof AnimationStateExtended extended && SodiumExtraClientMod.options().animationSettings.animation && this.shouldAnimate(extended.sodium_extra$getSprite().contents().name())) {
            instance.tick();
        }
    }

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createAnimationState(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;I)Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;"))
    public SpriteContents.AnimationState upload(TextureAtlasSprite instance, GpuBufferSlice gpuBufferSlice, int i) {
        SpriteContents.AnimationState state = instance.createAnimationState(gpuBufferSlice, i);
        ((AnimationStateExtended) state).sodium_extra$setSprite(instance);
        return state;
    }

    @Unique
    private boolean shouldAnimate(Identifier identifier) {
        if (identifier != null) {
            for (Map.Entry<Supplier<Boolean>, List<Identifier>> supplierListEntry : this.animatedSprites.entrySet()) {
                if (supplierListEntry.getValue().contains(identifier)) {
                    return supplierListEntry.getKey().get();
                }
            }
        }
        return true;
    }
}

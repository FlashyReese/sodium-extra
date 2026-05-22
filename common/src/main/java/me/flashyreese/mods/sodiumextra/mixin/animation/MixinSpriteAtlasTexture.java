package me.flashyreese.mods.sodiumextra.mixin.animation;

import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(TextureAtlas.class)
public abstract class MixinSpriteAtlasTexture extends AbstractTexture {
    @Unique
    private final Map<Supplier<Boolean>, List<ResourceLocation>> animatedSprites = Map.of(
            () -> SodiumExtraClientMod.options().animationSettings.water, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_still"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/water_flow")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.lava, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_still"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/lava_flow")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.portal, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/nether_portal")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.fire, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_0"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/fire_1"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_0"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_fire_1"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_fire"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/campfire_log_lit"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_fire"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_campfire_log_lit")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.blockAnimations, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/magma"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/lantern"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sea_lantern"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/soul_lantern"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/kelp"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/kelp_plant"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/seagrass"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/tall_seagrass_top"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/tall_seagrass_bottom"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/warped_stem"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/crimson_stem"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/blast_furnace_front_on"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/smoker_front_on"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/stonecutter_saw"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/prismarine"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/respawn_anchor_top"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "entity/conduit/wind"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "entity/conduit/wind_vertical")
            ),
            () -> SodiumExtraClientMod.options().animationSettings.sculkSensor, List.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_catalyst_top_bloom"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_catalyst_side_bloom"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_shrieker_inner_top"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_vein"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_shrieker_can_summon_inner_top"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_sensor_tendril_inactive"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "block/sculk_sensor_tendril_active"),
                    ResourceLocation.fromNamespaceAndPath("minecraft", "vibration")
            )
    );

    @Redirect(method = "upload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;createTicker()Lnet/minecraft/client/renderer/texture/TextureAtlasSprite$Ticker;"))
    public TextureAtlasSprite.Ticker sodiumExtra$tickAnimatedSprites(TextureAtlasSprite instance) {
        TextureAtlasSprite.Ticker tickableAnimation = instance.createTicker();
        if (tickableAnimation != null && SodiumExtraClientMod.options().animationSettings.animation && this.shouldAnimate(instance.contents().name()))
            return tickableAnimation;
        return null;
    }

    @Unique
    private boolean shouldAnimate(ResourceLocation identifier) {
        if (identifier != null) {
            for (Map.Entry<Supplier<Boolean>, List<ResourceLocation>> supplierListEntry : this.animatedSprites.entrySet()) {
                if (supplierListEntry.getValue().contains(identifier)) {
                    return supplierListEntry.getKey().get();
                }
            }
        }
        return true;
    }
}

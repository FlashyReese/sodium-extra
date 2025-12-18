package me.flashyreese.mods.sodiumextra.mixin.animation;

import me.flashyreese.mods.sodiumextra.common.util.AnimationStateExtended;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.AnimationState.class)
public class MixinSpriteContentsAnimationState implements AnimationStateExtended {
    @Unique
    private TextureAtlasSprite sprite;

    @Override
    public TextureAtlasSprite sodium_extra$getSprite() {
        return sprite;
    }

    @Override
    public void sodium_extra$setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }
}

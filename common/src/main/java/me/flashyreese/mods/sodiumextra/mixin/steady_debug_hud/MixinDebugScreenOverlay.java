package me.flashyreese.mods.sodiumextra.mixin.steady_debug_hud;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Util;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Unique
    private final List<String> leftTextCache = new ArrayList<>();
    @Unique
    private final List<String> rightTextCache = new ArrayList<>();
    @Unique
    private long nextTime = 0L;
    @Unique
    private boolean rebuild = true;

    @Shadow
    protected abstract void extractLines(GuiGraphicsExtractor guiGraphics, List<String> list, boolean bl);

    @Inject(method = "extractRenderState", at = @At(value = "HEAD"))
    public void preRender(GuiGraphicsExtractor guiGraphics, CallbackInfo ci) {
        if (SodiumExtraClientMod.options().extraSettings.steadyDebugHud) {
            final long currentTime = Util.getMillis();
            if (currentTime > this.nextTime) {
                this.rebuild = true;
                this.nextTime = currentTime + (SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval * 50L);
            } else {
                this.rebuild = false;
            }
        } else {
            this.rebuild = true;
        }
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;Z)V", ordinal = 0))
    public void sodiumExtra$wrapDrawLeftText(DebugScreenOverlay instance, GuiGraphicsExtractor guiGraphics, List<String> text, boolean left, Operation<Void> original) {
        if (this.rebuild) {
            this.leftTextCache.clear();
            this.leftTextCache.addAll(text);
        }
        original.call(instance, guiGraphics, this.leftTextCache, left);
    }

    @WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;extractLines(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Ljava/util/List;Z)V", ordinal = 1))
    public void sodiumExtra$wrapDrawRightText(DebugScreenOverlay instance, GuiGraphicsExtractor guiGraphics, List<String> text, boolean left, Operation<Void> original) {
        if (this.rebuild) {
            this.rightTextCache.clear();
            this.rightTextCache.addAll(text);
        }
        original.call(instance, guiGraphics, this.rightTextCache, left);
    }
}

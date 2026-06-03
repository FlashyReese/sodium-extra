package me.flashyreese.mods.sodiumextra.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.debug.DebugOptionsScreen$OptionEntry")
public abstract class MixinDebugOptionsScreen extends DebugOptionsScreen.AbstractOptionEntry {
    @Shadow
    @Final
    private CycleButton<Boolean> never;

    @Shadow
    @Final
    private CycleButton<Boolean> overlay;

    @Shadow
    @Final
    private CycleButton<Boolean> always;

    @Shadow
    @Final
    private boolean isAllowed;

    @Shadow
    @Final
    private String name;

    @Inject(method = "extractContent", at = @At(value = "HEAD"), cancellable = true)
    public void redirectExtractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a, CallbackInfo ci) {
        if (!this.name.startsWith("sodium-extra:")) {
            return;
        }

        ci.cancel();

        Identifier id = Identifier.parse(this.name);
        int x = this.getContentX();
        int y = this.getContentY();
        graphics.text(Minecraft.getInstance().font, Component.translatable(id.getPath()), x, y + 5, this.isAllowed ? -1 : -8355712);
        int buttonsStartX = x + this.getContentWidth() - this.never.getWidth() - this.overlay.getWidth() - this.always.getWidth();
        if (hovered && mouseX < buttonsStartX) {
            if (!this.isAllowed) {
                graphics.setTooltipForNextFrame(Component.translatable("debug.options.notAllowed.tooltip"), mouseX, mouseY);
            } else {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font.split(Component.translatable(id.getPath() + ".tooltip"), 200), mouseX, mouseY);
            }
        }

        this.never.setX(buttonsStartX);
        this.overlay.setX(this.never.getX() + this.never.getWidth());
        this.always.setX(this.overlay.getX() + this.overlay.getWidth());
        this.always.setY(y);
        this.overlay.setY(y);
        this.never.setY(y);
        this.always.extractRenderState(graphics, mouseX, mouseY, a);
        this.overlay.extractRenderState(graphics, mouseX, mouseY, a);
        this.never.extractRenderState(graphics, mouseX, mouseY, a);
    }
}

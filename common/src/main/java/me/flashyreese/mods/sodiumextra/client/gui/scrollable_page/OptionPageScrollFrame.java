package me.flashyreese.mods.sodiumextra.client.gui.scrollable_page;

import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionPage;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionPageScrollFrame extends AbstractFrame {
    protected final OptionPage page;
    private boolean canScroll;
    private ScrollBarComponent scrollBar = null;

    public OptionPageScrollFrame(Dim2i dim, OptionPage page) {
        super(dim);
        this.page = page;
        this.setupFrame();
        this.buildFrame();
    }

    public void setupFrame() {
        this.children.clear();
        this.controlElements.clear();

        int y = 0;
        if (!this.page.getGroups().isEmpty()) {
            OptionGroup lastGroup = this.page.getGroups().get(this.page.getGroups().size() - 1);

            for (OptionGroup group : this.page.getGroups()) {
                y += group.getOptions().size() * 18;
                if (group != lastGroup) {
                    y += 4;
                }
            }
        }

        this.canScroll = this.dim.height() < y;
        if (this.canScroll) {
            this.scrollBar = new ScrollBarComponent(new Dim2i(this.dim.getLimitX() - 10, this.dim.y(), 10, this.dim.height()), y, this.dim.height(), this::buildFrame, this.dim);
        }
    }

    @Override
    public void buildFrame() {
        if (this.page == null) return;

        this.children.clear();
        this.controlElements.clear();

        int y = 0;
        for (OptionGroup group : this.page.getGroups()) {
            // Add each option's control element
            for (Option<?> option : group.getOptions()) {
                Control<?> control = option.getControl();
                ControlElement<?> element = control.createElement(new Dim2i(this.dim.x(), this.dim.y() + y - (this.canScroll ? this.scrollBar.getOffset() : 0), this.dim.width() - (this.canScroll ? 11 : 0), 18));
                this.children.add(element);

                // Move down to the next option
                y += 18;
            }

            // Add padding beneath each option group
            y += 4;
        }

        if (this.canScroll) {
            this.scrollBar.updateThumbPosition();
        }

        super.buildFrame();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        ControlElement<?> hoveredElement = this.controlElements.stream()
                .filter(ControlElement::isHovered)
                .findFirst()
                .orElse(this.controlElements.stream() // If there is no hovered element, use the focused element.
                        .filter(ControlElement::isFocused)
                        .findFirst()
                        .orElse(null));
        this.applyScissor(guiGraphics, this.dim.x(), this.dim.y(), this.dim.width(), this.dim.height(), () -> super.render(guiGraphics, mouseX, mouseY, delta));
        if (this.canScroll) {
            this.scrollBar.render(guiGraphics, mouseX, mouseY, delta);
        }
        if (this.dim.containsCursor(mouseX, mouseY) && hoveredElement != null) {
            this.renderOptionTooltip(guiGraphics, hoveredElement);
        }
    }

    private void renderOptionTooltip(GuiGraphics guiGraphics, ControlElement<?> element) {
        Dim2i dim = element.getDimensions();

        int textPadding = 3;
        int boxPadding = 3;

        int boxWidth = 240;

        int boxY = Math.max(dim.y(), this.dim.y());
        int boxX = this.dim.getLimitX() + boxPadding;

        Option<?> option = element.getOption();
        List<FormattedCharSequence> tooltip = new ArrayList<>(Minecraft.getInstance().font.split(option.getTooltip(), boxWidth - (textPadding * 2)));

        OptionImpact impact = option.getImpact();

        if (impact != null) {
            tooltip.add(Language.getInstance().getVisualOrder(Component.translatable("sodium.options.performance_impact_string", impact.getLocalizedName()).withStyle(ChatFormatting.GRAY)));
        }

        /*if (option.getFlags().contains(OptionFlag.REQUIRES_GAME_RESTART)) {
            tooltip.add(Language.getInstance().reorder(Text.translatable("sodium.option_flag.requires_game_restart.tooltip").formatted(Formatting.RED)));
        }*/

        int boxHeight = (tooltip.size() * 12) + boxPadding;
        int boxYLimit = boxY + boxHeight;
        int boxYCutoff = this.dim.getLimitY() - 25 /*- 64*/;

        // If the box is going to be cutoff on the Y-axis, move it back up the difference
        if (boxYLimit > boxYCutoff) {
            boxY -= boxYLimit - boxYCutoff;
        }

        this.drawRect(guiGraphics, boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xE0000000);

        for (int i = 0; i < tooltip.size(); i++) {
            guiGraphics.drawString(Minecraft.getInstance().font, tooltip.get(i), boxX + textPadding, boxY + textPadding + (i * 12), 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (this.dim.containsCursor(mouseButtonEvent.x(), mouseButtonEvent.y()) && super.mouseClicked(mouseButtonEvent, bl)) {
            return true;
        }
        if (this.canScroll) {
            return this.scrollBar.mouseClicked(mouseButtonEvent, bl);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        if (super.mouseDragged(mouseButtonEvent, d, e)) {
            return true;
        }
        if (this.canScroll) {
            return this.scrollBar.mouseDragged(mouseButtonEvent, d, e);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (super.mouseReleased(mouseButtonEvent)) {
            return true;
        }
        if (this.canScroll) {
            return this.scrollBar.mouseReleased(mouseButtonEvent);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        if (this.canScroll) {
            return this.scrollBar.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return false;
    }
}
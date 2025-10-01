package me.flashyreese.mods.sodiumextra.client.gui.options.control;

import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.control.Control;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlElement;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.util.Dim2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

import java.util.function.Supplier;

public class DynamicSliderControl implements Control<Integer> {
    private final Option<Integer> option;
    private final Supplier<Integer> min;
    private final Supplier<Integer> max;
    private final int interval;
    private final ControlValueFormatter mode;

    public DynamicSliderControl(Option<Integer> option, Supplier<Integer> min, Supplier<Integer> max, int interval, ControlValueFormatter mode) {
        Validate.isTrue(interval > 0, "The slider interval must be greater than zero");
        Validate.notNull(mode, "The slider mode must not be null");
        this.option = option;
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.mode = mode;
    }

    public ControlElement<Integer> createElement(Dim2i dim) {
        int min = this.min.get();
        int max = this.max.get();
        int interval = this.interval;

        Validate.isTrue(max > min, "The maximum value must be greater than the minimum value");
        Validate.isTrue(((max - min) % interval) == 0, "The maximum value must be divisable by the interval");

        return new DynamicSliderControl.Button(this.option, dim, min, max, this.interval, this.mode);
    }

    public Option<Integer> getOption() {
        return this.option;
    }

    public int getMaxWidth() {
        return 170;
    }

    private static class Button extends ControlElement<Integer> {
        private static final int THUMB_WIDTH = 2;
        private static final int TRACK_HEIGHT = 1;
        private final Rect2i sliderBounds;
        private final ControlValueFormatter formatter;
        private final int min;
        private final int max;
        private final int range;
        private final int interval;
        private int contentWidth;
        private double thumbPosition;
        private boolean sliderHeld;

        public Button(Option<Integer> option, Dim2i dim, int min, int max, int interval, ControlValueFormatter formatter) {
            super(option, dim);
            this.min = min;
            this.max = max;
            this.range = max - min;
            this.interval = interval;
            this.thumbPosition = this.getThumbPositionForValue(option.getValue());
            this.formatter = formatter;
            this.sliderBounds = new Rect2i(dim.getLimitX() - 96, dim.getCenterY() - 5, 90, 10);
            this.sliderHeld = false;
        }

        public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            int sliderX = this.sliderBounds.getX();
            int sliderY = this.sliderBounds.getY();
            int sliderWidth = this.sliderBounds.getWidth();
            int sliderHeight = this.sliderBounds.getHeight();
            MutableComponent label = this.formatter.format(this.option.getValue()).copy();
            if (!this.option.isAvailable()) {
                label.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(true));
            }

            int labelWidth = this.font.width(label);
            boolean drawSlider = this.option.isAvailable() && (this.hovered || this.isFocused());
            if (drawSlider) {
                this.contentWidth = sliderWidth + labelWidth;
            } else {
                this.contentWidth = labelWidth;
            }

            super.render(graphics, mouseX, mouseY, delta);
            if (drawSlider) {
                this.thumbPosition = this.getThumbPositionForValue(this.option.getValue());
                double thumbOffset = Mth.clamp((double) (this.getIntValue() - this.min) / (double) this.range * (double) sliderWidth, 0.0F, sliderWidth);
                int thumbX = (int) ((double) sliderX + thumbOffset - (double) 2.0F);
                int trackY = (int) ((double) ((float) sliderY + (float) sliderHeight / 2.0F) - (double) 0.5F);
                this.drawRect(graphics, thumbX, sliderY, thumbX + 4, sliderY + sliderHeight, -1);
                this.drawRect(graphics, sliderX, trackY, sliderX + sliderWidth, trackY + 1, -1);
                this.drawString(graphics, label, sliderX - labelWidth - 6, sliderY + sliderHeight / 2 - 4, -1);
            } else {
                this.drawString(graphics, label, sliderX + sliderWidth - labelWidth, sliderY + sliderHeight / 2 - 4, -1);
            }

        }

        public int getContentWidth() {
            return this.contentWidth;
        }

        public int getIntValue() {
            return this.min + this.interval * (int) Math.round(this.getSnappedThumbPosition() / (double) this.interval);
        }

        public double getSnappedThumbPosition() {
            return this.thumbPosition / ((double) 1.0F / (double) this.range);
        }

        public double getThumbPositionForValue(int value) {
            return (double) (value - this.min) * ((double) 1.0F / (double) this.range);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.sliderHeld = false;
            if (this.option.isAvailable() && button == 0 && this.dim.containsCursor(mouseX, mouseY)) {
                if (this.sliderBounds.contains((int) mouseX, (int) mouseY)) {
                    this.setValueFromMouse(mouseX);
                    this.sliderHeld = true;
                }

                return true;
            } else {
                return false;
            }
        }

        private void setValueFromMouse(double d) {
            this.setValue((d - (double) this.sliderBounds.getX()) / (double) this.sliderBounds.getWidth());
        }

        public void setValue(double d) {
            this.thumbPosition = Mth.clamp(d, 0.0F, 1.0F);
            int value = this.getIntValue();
            if (this.option.getValue() != value) {
                this.option.setValue(value);
            }

        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!this.isFocused()) {
                return false;
            } else if (keyCode == 263) {
                this.option.setValue(Mth.clamp(this.option.getValue() - this.interval, this.min, this.max));
                return true;
            } else if (keyCode == 262) {
                this.option.setValue(Mth.clamp(this.option.getValue() + this.interval, this.min, this.max));
                return true;
            } else {
                return false;
            }
        }

        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (this.option.isAvailable() && button == 0) {
                if (this.sliderHeld) {
                    this.setValueFromMouse(mouseX);
                }

                return true;
            } else {
                return false;
            }
        }
    }
}

package me.flashyreese.mods.sodiumextra.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public abstract class AbstractTextHudWidget implements HudWidget {
    protected final Minecraft client = Minecraft.getInstance();
    private final String id;
    private final Component name;
    private Component text = Component.empty();
    private int width;

    protected AbstractTextHudWidget(String id, Component name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Component name() {
        return this.name;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.client.font.lineHeight;
    }

    @Override
    public Component getText() {
        return this.text;
    }

    @Override
    public final void tick() {
        Component next = this.getTextInternal();
        this.text = next;
        this.width = this.client.font.width(next);
    }

    protected abstract Component getTextInternal();
}

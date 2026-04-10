package me.flashyreese.mods.sodiumextra.client.hud;

import net.minecraft.network.chat.Component;

public interface HudWidget {
    String id();

    Component name();

    int getWidth();

    int getHeight();

    Component getText();

    void tick();
}

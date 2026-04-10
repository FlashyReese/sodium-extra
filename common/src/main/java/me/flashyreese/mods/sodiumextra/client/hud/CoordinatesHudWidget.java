package me.flashyreese.mods.sodiumextra.client.hud;

import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class CoordinatesHudWidget extends AbstractTextHudWidget {
    public CoordinatesHudWidget() {
        super("coordinates", Component.translatable("sodium-extra.option.show_coordinates"));
    }

    @Override
    protected Component getTextInternal() {
        if (this.client.showOnlyReducedInfo()) {
            return Component.translatable("sodium-extra.overlay.coordinates_unavailable");
        }

        if (this.client.player == null) {
            return Component.empty();
        }

        Vec3 pos = this.client.player.position();
        return Component.translatable("sodium-extra.overlay.coordinates",
                String.format(Locale.ROOT, "%.2f", pos.x),
                String.format(Locale.ROOT, "%.2f", pos.y),
                String.format(Locale.ROOT, "%.2f", pos.z));
    }
}

package me.flashyreese.mods.sodiumextra.mixin.fog;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface AccessorMinecraft {
    @Accessor("singleplayerServer")
    IntegratedServer sodiumExtra$getSingleplayerServer();
}

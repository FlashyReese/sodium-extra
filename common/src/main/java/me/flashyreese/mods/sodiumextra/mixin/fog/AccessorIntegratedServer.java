package me.flashyreese.mods.sodiumextra.mixin.fog;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.client.server.IntegratedServer")
public interface AccessorIntegratedServer {
    @Invoker("commandsAllowedForOtherPlayers")
    boolean sodiumExtra$commandsAllowedForOtherPlayers();
}

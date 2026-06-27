package me.flashyreese.mods.sodiumextra.mixin.fog;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.server.MinecraftServer")
public interface AccessorMinecraftServer {
    @Invoker("getPlayerList")
    PlayerList sodiumExtra$getPlayerList();
}

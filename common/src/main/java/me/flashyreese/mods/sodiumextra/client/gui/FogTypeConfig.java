package me.flashyreese.mods.sodiumextra.client.gui;

public class FogTypeConfig {
    public boolean enable;
    public int environmentStartMultiplier;
    public int environmentEndMultiplier;
    public int renderDistanceStartMultiplier;
    public int renderDistanceEndMultiplier;
    public int skyEndMultiplier;
    public int cloudEndMultiplier;

    public FogTypeConfig() {
        this.enable = true;
        this.environmentStartMultiplier = 100;
        this.environmentEndMultiplier = 100;
        this.renderDistanceStartMultiplier = 100;
        this.renderDistanceEndMultiplier = 100;
        this.skyEndMultiplier = 100;
        this.cloudEndMultiplier = 100;
    }
}

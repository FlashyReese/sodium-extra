rootProject.name = "sodium-extra"

pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.neoforged.net/releases/") }
        gradlePluginPortal()
    }
}

include("common")
include("fabric")
include("neoforge")

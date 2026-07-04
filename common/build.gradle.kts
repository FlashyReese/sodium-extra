import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version "1.17.11"
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra

val SODIUM_VERSION: String by rootProject.extra
val GREENLIGHT_VERSION: String by rootProject.extra

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
    compileOnly("net.fabricmc:sponge-mixin:0.17.3+mixin.0.8.7")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-getter-api-v2")
    addDependentFabricModule("fabric-rendering-v1")

    implementation("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
    compileOnly("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
}

tasks.withType<AbstractRemapJarTask>().forEach {
    it.targetNamespace = "named"
}

tasks.named("compileJava") {
    mustRunAfter("genSourcesWithVineflower")
}

loom {
    accessWidenerPath = file("src/main/resources/${rootProject.name}.accesswidener")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "FlashyReeseReleases"
            url = uri("https://maven.flashyreese.me/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
        maven {
            name = "FlashyReeseSnapshots"
            url = uri("https://maven.flashyreese.me/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

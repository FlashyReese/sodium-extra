plugins {
    id("java")
    id("idea")
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String

val SODIUM_VERSION = rootProject.extra["SODIUM_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String

architectury {
    compileOnly()
    common(
        "fabric", "neoforge",
    )
    injectInjectables = false
}

// LWJGL 3.4.3 includes inactive Java 27 classes in its multi-release JAR. Architectury Transformer 5.2.91
// scans those entries while producing the NeoForge common JAR unless LWJGL is removed from its analysis classpath.
configurations.named("architecturyTransformerClasspath") {
    exclude(group = "org.lwjgl")
}

// This trick hides common tasks in the IDEA list.
tasks.configureEach {
    group = null
}

loom {
    accessWidenerPath = file("src/main/resources/${rootProject.name}.accesswidener")
}

dependencies {
    minecraft("net.minecraft:minecraft:$MINECRAFT_VERSION")

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.5")
    compileOnly("net.fabricmc:sponge-mixin:0.17.4+mixin.0.8.7")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        compileOnly(module)
    }

    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-getter-api-v2")
    addDependentFabricModule("fabric-rendering-v1")

    compileOnly("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
    compileOnly("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
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

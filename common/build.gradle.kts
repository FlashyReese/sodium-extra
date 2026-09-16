plugins {
    id("java")
    id("idea")
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val PARCHMENT_VERSION = rootProject.extra["PARCHMENT_VERSION"] as String?
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String

val SODIUM_VERSION = rootProject.extra["SODIUM_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String

architectury {
    common("fabric", "neoforge")
    injectInjectables = false
}

// This trick hides common tasks in the IDEA list.
tasks.configureEach {
    group = null
}

tasks.named<Jar>("jar") {
    destinationDirectory.set(layout.buildDirectory.dir("libs"))
    archiveClassifier.set("")
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("remapped")
}

loom {
    mixin {
        useLegacyMixinAp = false
        //defaultRefmapName = "${rootProject.name}.refmap.json"
    }

    accessWidenerPath = file("src/main/resources/${rootProject.name}.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.layered {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })

    compileOnly("io.github.llamalad7:mixinextras-common:0.5.0")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.0")
    compileOnly("net.fabricmc:sponge-mixin:0.13.2+mixin.0.8.5")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addDependentFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modCompileOnly(module)
    }

    addDependentFabricModule("fabric-api-base")
    addDependentFabricModule("fabric-block-view-api-v2")
    addDependentFabricModule("fabric-renderer-api-v1")
    addDependentFabricModule("fabric-rendering-data-attachment-v1")

    modCompileOnly("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
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

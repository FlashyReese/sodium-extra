import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("idea")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val PARCHMENT_VERSION = rootProject.extra["PARCHMENT_VERSION"] as String?
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String

val SODIUM_VERSION = rootProject.extra["SODIUM_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String

base {
    archivesName.set("${rootProject.name}-fabric")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/${rootProject.name}.accesswidener"))

    mods {
        create("sodium-extra") {
            sourceSet("main")
            sourceSet("main", ":common")
        }
    }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

val common = configurations.create("common") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val shadowBundle = configurations.create("shadowBundle") {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val embeddedGreenlightApi = configurations.create("embeddedGreenlightApi") {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = false
}

configurations.named("compileOnly") {
    extendsFrom(embeddedGreenlightApi)
}

configurations.named("runtimeOnly") {
    extendsFrom(embeddedGreenlightApi)
}

configurations.named("testCompileOnly") {
    extendsFrom(embeddedGreenlightApi)
}

configurations.named("testRuntimeOnly") {
    extendsFrom(embeddedGreenlightApi)
}

configurations.named("compileClasspath") {
    extendsFrom(common)
}

configurations.named("developmentFabric") {
    extendsFrom(common)
}

dependencies {
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.layered {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        modImplementation(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-block-view-api-v2")
    //addEmbeddedFabricModule("fabric-renderer-api-v1")
    addEmbeddedFabricModule("fabric-rendering-fluids-v1")
    addEmbeddedFabricModule("fabric-resource-loader-v0")
    modImplementation("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
    add(embeddedGreenlightApi.name, "me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    add("common", project(path = ":common", configuration = "namedElements")) {
        isTransitive = false
    }
    add("shadowBundle", project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach {
    dependsOn("generateRemapClasspath")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", MINECRAFT_VERSION)

        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "minecraft_version" to MINECRAFT_VERSION
            ))
        }
    }

    jar {
        archiveClassifier.set("dev")
        from(rootDir.resolve("LICENSE.txt"))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowBundle)
    archiveClassifier.set("dev-shadow")
    from(rootDir.resolve("LICENSE.txt"))

    // The Greenlight API is published in the named namespace. Merge it into this
    // input jar so Loom remaps its Minecraft references alongside Sodium Extra.
    from({ embeddedGreenlightApi.map { zipTree(it) } }) {
        exclude("META-INF/MANIFEST.MF")
    }
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn(tasks.named("shadowJar"))
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    archiveClassifier.set("")
}

configurations.named("runtimeElements") {
    outgoing.artifacts.clear()
}

artifacts {
    add("runtimeElements", tasks.named("remapJar"))
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

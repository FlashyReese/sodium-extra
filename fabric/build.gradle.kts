import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("idea")
    id("dev.architectury.loom-no-remap")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val FABRIC_LOADER_VERSION = rootProject.extra["FABRIC_LOADER_VERSION"] as String
val FABRIC_API_VERSION = rootProject.extra["FABRIC_API_VERSION"] as String

val SODIUM_VERSION = rootProject.extra["SODIUM_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String

base {
    archivesName.set("${rootProject.name}-fabric")
}

architectury {
    platformSetupLoomIde()
    compileOnly()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/${rootProject.name}.accesswidener"))
    nestJars(tasks.named<ShadowJar>("shadowJar"), configurations.named("include"))

    mods {
        create("sodium-extra") {
            sourceSet("main")
            sourceSet("main", ":common")
        }
    }

    runs {
        named("client") {
            client()
            displayName.set("Fabric Client")
            generateRunConfig.set(true)
            runDirectory.set(layout.projectDirectory.dir("run"))
        }
        named("server") {
            server()
            displayName.set("Fabric Server")
            generateRunConfig.set(true)
            runDirectory.set(layout.projectDirectory.dir("run"))
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

configurations.named("compileClasspath") {
    extendsFrom(common)
}

configurations.named("runtimeClasspath") {
    extendsFrom(common)
}

dependencies {
    minecraft("net.minecraft:minecraft:$MINECRAFT_VERSION")
    implementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-block-getter-api-v2")
    addEmbeddedFabricModule("fabric-rendering-v1")
    implementation("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
    implementation("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    include("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    add("common", project(":common")) {
        isTransitive = false
    }
    add("shadowBundle", project(path = ":common", configuration = "runtimeElements")) {
        isTransitive = false
    }
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", MINECRAFT_VERSION.replace("-rc-", "-rc."))
        inputs.property("fabric_loader_version", FABRIC_LOADER_VERSION)
        inputs.property("sodium_version", SODIUM_VERSION)

        filesMatching("fabric.mod.json") {
            expand(mapOf(
                "version" to project.version,
                "minecraft_version" to MINECRAFT_VERSION.replace("-rc-", "-rc."),
                "fabric_loader_version" to FABRIC_LOADER_VERSION,
                "sodium_version" to SODIUM_VERSION
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
    archiveClassifier.set("")
    from(rootDir.resolve("LICENSE.txt"))
    manifest.attributes("Fabric-Mapping-Namespace" to "official")
}

configurations.named("apiElements") {
    outgoing.artifacts.clear()
}

configurations.named("runtimeElements") {
    outgoing.artifacts.clear()
}

artifacts {
    add("apiElements", tasks.named("shadowJar"))
    add("runtimeElements", tasks.named("shadowJar"))
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

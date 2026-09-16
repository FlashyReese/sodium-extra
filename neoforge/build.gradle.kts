import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("idea")
    id("java-library")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val MINECRAFT_VERSION = rootProject.extra["MINECRAFT_VERSION"] as String
val PARCHMENT_VERSION = rootProject.extra["PARCHMENT_VERSION"] as String?
val NEOFORGE_VERSION = rootProject.extra["NEOFORGE_VERSION"] as String
val SODIUM_VERSION = rootProject.extra["SODIUM_VERSION"] as String
val GREENLIGHT_VERSION = rootProject.extra["GREENLIGHT_VERSION"] as String
val SODIUM_NEOFORGE_RUNTIME_MODS = listOf(
    "org.sinytra.forgified-fabric-api:fabric-api-base:0.4.42+d1308ded19",
    "org.sinytra.forgified-fabric-api:fabric-renderer-api-v1:3.4.1+9125b6dc19",
    "org.sinytra.forgified-fabric-api:fabric-rendering-data-attachment-v1:0.3.48+73761d2e19",
    "org.sinytra.forgified-fabric-api:fabric-block-view-api-v2:1.0.10+9afaaf8c19",
)

base {
    archivesName.set("${rootProject.name}-neoforge")
}

architectury {
    compileOnly()
    platformSetupLoomIde()
    neoForge()
}

repositories {
    maven("https://maven.neoforged.net/releases/")
}

loom {
    neoForge {
        accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")
    }

    mods {
        named("main") {
            sourceSet("main", ":common")
        }
    }

    runs {
        named("client") {
            client()
            displayName.set("NeoForge Client")
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
    minecraft("com.mojang:minecraft:$MINECRAFT_VERSION")
    mappings(loom.layered {
        officialMojangMappings()
        if (PARCHMENT_VERSION != null) {
            parchment("org.parchmentmc.data:parchment-${MINECRAFT_VERSION}:${PARCHMENT_VERSION}@zip")
        }
    })
    // Specify the version of NeoForge to use.
    add("neoForge", "net.neoforged:neoforge:$NEOFORGE_VERSION")

    // Sodium's NeoForge wrapper provides runtime services; the nested mod jar is needed as a real mod in Loom dev runs.
    modImplementation("net.caffeinemc:sodium-neoforge-mod:$SODIUM_VERSION")
    add("forgeRuntimeLibrary", "net.caffeinemc:sodium-neoforge:$SODIUM_VERSION") {
        isTransitive = false
    }
    implementation("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    include("me.flashyreese.mods:greenlight-api:$GREENLIGHT_VERSION")
    SODIUM_NEOFORGE_RUNTIME_MODS.forEach {
        modRuntimeOnly(it)
    }
    add("common", project(path = ":common", configuration = "namedElements")) {
        isTransitive = false
    }
    add("shadowBundle", project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
}

tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.test {
    failOnNoDiscoveredTests = false
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", MINECRAFT_VERSION)
        inputs.property("sodium_version", SODIUM_VERSION)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf(
                "version" to project.version,
                "minecraft_version" to MINECRAFT_VERSION,
                "sodium_version" to SODIUM_VERSION,
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
}

tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    dependsOn(tasks.named("shadowJar"))
    inputFile.set(tasks.named<ShadowJar>("shadowJar").flatMap { it.archiveFile })
    archiveClassifier.set("")
}

configurations.named("apiElements") {
    outgoing.artifacts.clear()
}

configurations.named("runtimeElements") {
    outgoing.artifacts.clear()
}

artifacts {
    add("apiElements", tasks.named("remapJar"))
    add("runtimeElements", tasks.named("remapJar"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
            setArtifacts(listOf(tasks.named("remapJar")))
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

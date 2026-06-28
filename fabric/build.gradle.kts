plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version ("1.17.11")
}

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val MOD_VERSION: String by rootProject.extra

val SODIUM_VERSION: String by rootProject.extra
val ARCHIVE_NAME: String by rootProject.extra

base {
    archivesName.set("$ARCHIVE_NAME-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")
    compileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    runtimeOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")
    testCompileOnly("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun addEmbeddedFabricModule(name: String) {
        val module = fabricApi.module(name, FABRIC_API_VERSION)
        implementation(module)
    }

    // Fabric API modules
    addEmbeddedFabricModule("fabric-api-base")
    addEmbeddedFabricModule("fabric-block-getter-api-v2")
    addEmbeddedFabricModule("fabric-rendering-v1")
    compileOnly(project(":common"))
    implementation("net.caffeinemc:sodium-fabric:$SODIUM_VERSION")
}

tasks.test {
    failOnNoDiscoveredTests = false
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/${rootProject.name}.accesswidener"))

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

val modVersion = project.version.toString()

tasks {
    withType<JavaCompile> {
        source(project(":common").sourceSets.main.get().allSource)
    }

    javadoc { source(project(":common").sourceSets.main.get().allJava) }

    processResources {
        from(project(":common").sourceSets.main.get().resources)

        inputs.property("version", modVersion)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to modVersion))
        }
    }

    jar {
        from(rootDir.resolve("LICENSE.txt"))
    }
}

tasks.named("validateAccessWidener").configure {
    dependsOn(":common:genSourcesWithVineflower")
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

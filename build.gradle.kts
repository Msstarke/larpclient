plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.shadow)
}

val modVersion: String by project
val modId: String by project

version = modVersion
group = "com.larpclient"
base.archivesName.set("LarpClient")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.nea.moe/releases")
    maven("https://repo.nea.moe/mirror")
    maven("https://notenoughupdates.org/maven/releases")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings(loom.officialMojangMappings())
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    // MoulConfig - config GUI framework
    shadowImpl(libs.moulconfig.modern) { isTransitive = false }
    shadowImpl(libs.moulconfig.common) { isTransitive = false }

    // LibAutoUpdate - auto-update from GitHub releases
    shadowImpl(libs.libautoupdate)
}

loom {
    runConfigs.all {
        ideConfigGenerated(true)
        runDir("../../run") // shared run dir across versions
    }
    mixin {
        defaultRefmapName.set("$modId-refmap.json")
    }
}

tasks {
    processResources {
        val props = mapOf(
            "version" to modVersion,
            "mod_id" to modId,
            "minecraft_version" to stonecutter.current.version,
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    shadowJar {
        configurations = listOf(shadowImpl)
        archiveClassifier.set("dev-shadow")
        relocate("io.github.notenoughupdates.moulconfig", "com.larpclient.deps.moulconfig")
        relocate("moe.nea.libautoupdate", "com.larpclient.deps.libautoupdate")
        mergeServiceFiles()
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
        archiveFileName.set("LarpClient-${modVersion}-mc${stonecutter.current.version}.jar")
    }

    jar {
        archiveClassifier.set("dev-slim")
    }

    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }

    compileJava {
        options.release.set(21)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

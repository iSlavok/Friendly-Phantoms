pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        // Anchor versions. Stonecutter builds one jar per node; a single jar covers
        // a whole range of Minecraft versions because the mixin targets stable
        // intermediary names. Add versions here as the matrix grows.
        // Minecraft 26+ requires JDK 25 to build. Include it only when the running
        // JDK supports it, so the project still builds on JDK 21-24 (CI provisions
        // JDK 25 for the unobfuscated job).
        val versions = mutableListOf("1.18.2", "1.19.4", "1.20.4", "1.20.6", "1.21.8")
        if (JavaVersion.current().majorVersion.toInt() >= 25) {
            versions.add("26.2")
        }
        versions(*versions.toTypedArray())
        vcsVersion = "1.21.8"
    }
}

rootProject.name = "friendly-phantoms"

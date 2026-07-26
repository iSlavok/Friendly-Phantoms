plugins {
    java
    // 2.x: its Modrinth modLoaders is free-form, so it can publish the plugin
    // loaders (bukkit/spigot/paper/purpur/folia) that older versions reject.
    id("me.modmuss50.mod-publish-plugin") version "2.1.1"
}

java {
    // Modern Spigot/Paper (MC 1.20.6+) require Java 21.
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigot" }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") { name = "sonatype" }
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}

dependencies {
    // Compile against Spigot-API for maximum fork coverage (Bukkit ⊂ Spigot ⊂
    // Paper ⊂ Purpur). Provided by the server at runtime — never bundled.
    compileOnly("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")

    // Tests: Mockito for the pure listener logic, MockBukkit for a load smoke test.
    // paper-api (superset of the Bukkit API) supplies the API classes for tests;
    // its version must match what MockBukkit-v1.21 targets (1.21.11) or MockBukkit
    // throws IncompatiblePaperVersionException on load.
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

group = property("maven_group") as String
version = "${property("mod_version")}+paper"
base { archivesName = "friendly-phantoms-plugin" }

tasks.test { useJUnitPlatform() }

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") { expand("version" to project.version) }
}

tasks.jar {
    from(rootDir.resolve("../LICENSE")) {
        rename { "${it}_friendly-phantoms-plugin" }
    }
}

// Publishing to Modrinth (same project as the mod jars). One jar, tagged for the
// whole Bukkit fork chain. Requires gradle property `modrinth_id` and env
// MODRINTH_TOKEN. modLoaders is free-form on mod-publish-plugin 2.x.
publishMods {
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    changelog.set("See the GitHub release notes: https://github.com/iSlavok/Friendly-Phantoms/releases")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    displayName.set("Friendly Phantoms ${property("mod_version")} (Plugin)")
    modLoaders.addAll("bukkit", "spigot", "paper", "purpur", "folia")

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        // Phantoms exist since 1.13; the stable Bukkit API makes one jar cover a
        // wide range. Tag the commonly-used versions across that range.
        minecraftVersions.addAll(
            "1.16.5", "1.17.1", "1.18.2", "1.19.4",
            "1.20.1", "1.20.4", "1.20.6",
            "1.21", "1.21.1", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8",
        )
    }
}

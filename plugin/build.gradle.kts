plugins {
    java
    // 2.x: its Modrinth modLoaders is free-form, so it can publish the plugin
    // loaders (bukkit/spigot/paper/purpur/folia) that older versions reject.
    id("me.modmuss50.mod-publish-plugin") version "2.1.1"
    // Adds `runServer`: downloads a Paper server and runs it with this plugin
    // loaded (EULA auto-accepted for the dev run). A one-click server for testing.
    id("xyz.jpenilla.run-paper") version "3.0.2"
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

tasks.runServer {
    val mc = providers.gradleProperty("run_mc").getOrElse("1.21.8")
    minecraftVersion(mc)
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(if (mc.substringBefore(".").toInt() >= 26) 25 else 21))
    })
}

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
        minecraftVersionRange {
            start = "1.13"
            end = "26.2"
        }
    }
}

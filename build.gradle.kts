plugins {
    id("dev.kikugie.stonecutter")
    // Architectury Loom is a superset fork of Fabric Loom that can also build
    // NeoForge/Forge from the same source tree. On Fabric nodes it behaves like
    // Fabric Loom; `loom.platform` (set in gradle.properties) selects the target.
    id("dev.architectury.loom") version "1.17.491"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

// Per-version matrix. The mod source is shared across every version; only these
// coordinates differ. Anchors are the newest patch of each "breakage boundary"
// (Java level / rendering API), and each jar declares the Minecraft range it covers.
data class Mc(
    val yarn: String,
    val modmenu: String,
    val java: Int,
    val depends: String,
    val gameVersions: List<String>,
    // Fabric API version for the gametest source set (null = no gametest on this anchor).
    val fapi: String? = null,
)

val mcVersion = stonecutter.current.version
val mc = when (mcVersion) {
    "1.18.2" -> Mc("1.18.2+build.4", "3.2.5", 17, ">=1.18 <1.19",
        listOf("1.18", "1.18.1", "1.18.2"), "0.77.0+1.18.2")
    "1.19.4" -> Mc("1.19.4+build.2", "6.3.1", 17, ">=1.19 <1.20",
        listOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"), "0.87.2+1.19.4")
    "1.20.4" -> Mc("1.20.4+build.3", "9.2.0", 17, ">=1.20 <1.20.5",
        listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"), "0.97.3+1.20.4")
    "1.20.6" -> Mc("1.20.6+build.3", "10.0.0", 21, ">=1.20.5 <1.21",
        listOf("1.20.5", "1.20.6"), "0.100.8+1.20.6")
    "1.21.8" -> Mc("1.21.8+build.1", "15.0.2", 21, ">=1.21 <1.22",
        listOf("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7",
            "1.21.8", "1.21.9", "1.21.10", "1.21.11"), "0.136.1+1.21.8")
    else -> error("Unconfigured Minecraft version: $mcVersion")
}

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

repositories {
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    // Transitive dependency of older ModMenu builds (eu.pb4:placeholder-api).
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${mc.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // ModMenu is an optional, client-only soft dependency (see fabric.mod.json "suggests").
    modImplementation("com.terraformersmc:modmenu:${mc.modmenu}")
}

// Server gametest (Minecraft 1.20+). Directly verifies the mixin: a spawned phantom
// must not be able to target players. Executed in CI.
mc.fapi?.let { fapiVersion ->
    fabricApi {
        configureTests {
            createSourceSet = true
            modId = "${property("archives_base_name")}-test"
            eula = true
        }
    }
    dependencies {
        "modGametestImplementation"("net.fabricmc.fabric-api:fabric-api:$fapiVersion")
    }
}

// The NeoForge entrypoint and its metadata live in the shared tree but must not
// compile/ship on Fabric (Mojmap-only code, wrong mappings here).
tasks.named<JavaCompile>("compileJava") {
    exclude("online/slavok/neoforge/**")
}

tasks.processResources {
    // NeoForge-only metadata; Fabric uses fabric.mod.json.
    exclude("META-INF/neoforge.mods.toml", "pack.mcmeta")
    val props = mapOf(
        "version" to project.version,
        "java_level" to mc.java,
        "minecraft_dep" to mc.depends,
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) {
        expand(props)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = mc.java
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(mc.java)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

// Publishing to Modrinth. GitHub releases are handled by the release workflow.
// Requires: gradle property `modrinth_id` set to the project id, and env MODRINTH_TOKEN.
publishMods {
    file.set(tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    changelog.set("See the GitHub release notes: https://github.com/iSlavok/Friendly-Phantoms/releases")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    // The Fabric jar also runs on Quilt as-is (no dedicated Quilt build needed),
    // so tag it for both loaders on Modrinth.
    modLoaders.add("fabric")
    modLoaders.add("quilt")
    displayName.set("Friendly Phantoms ${property("mod_version")} (Fabric/Quilt, MC $mcVersion)")

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(mc.gameVersions)
        optional("modmenu")
    }
}

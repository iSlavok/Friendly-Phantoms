// Build script for NeoForge nodes. Uses ModDevGradle (net.neoforged.moddev),
// NOT loom, so it coexists with the fabric-loom Fabric/unobfuscated nodes in one
// Stonecutter build (Architectury Loom cannot: it clashes with fabric-loom and
// does not support the unobfuscated 26.x nodes). NeoForge mods are Mojang-mapped
// in production, so the plain `jar` is the final loadable mod jar — no reobf.
//
// The shared mod source is reused via source-set excludes: only PhantomBehavior
// (no Minecraft references) and online.slavok.neoforge compile here; the Fabric
// mixin/entrypoint/ModMenu are excluded. Behaviour is a LivingChangeTargetEvent
// listener instead of a mixin.
plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.141"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

data class NeoF(
    val neoforge: String,
    val depends: String,
    val gameVersions: List<String>,
    val java: Int,
    // Resource pack_format for this Minecraft version (for the mod's pack.mcmeta).
    val packFormat: Int,
)

val mcVersion = stonecutter.current.version
val nf = when (mcVersion) {
    // `depends` uses the Maven version-range syntax that neoforge.mods.toml expects.
    "1.20.6" -> NeoF("20.6.139", "[1.20.5,1.21)",
        listOf("1.20.5", "1.20.6"), 21, 32)
    "1.21.8" -> NeoF("21.8.54", "[1.21,1.22)",
        listOf("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6",
            "1.21.7", "1.21.8"), 21, 64)
    else -> error("Unconfigured NeoForge version: $mcVersion")
}

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String

base {
    // Distinct from the Fabric jar of the same MC version to avoid a filename
    // collision when all jars are collected for a GitHub release.
    archivesName = "${property("archives_base_name")}-neoforge"
}

neoForge {
    // Pulls Minecraft + NeoForge (Mojang-mapped). `mods` tells ModDevGradle which
    // source set forms the mod; the modId must match neoforge.mods.toml.
    version = nf.neoforge
    mods {
        register("friendly_phantoms") {
            sourceSet(sourceSets.getByName("main"))
        }
    }
    runs {
        register("client") {
            client()
            gameDirectory = file("run")
        }
        register("server") {
            server()
            gameDirectory = file("run")
        }
        // Headless load smoke: starts a server, loads the mod, runs any game tests
        // (none yet), exits. A broken @Mod / neoforge.mods.toml fails this.
        // Task: runGameTestServer.
        register("gameTestServer") {
            type = "gameTestServer"
            gameDirectory = file("run")
        }
    }
}

// Exclude Fabric-only sources — they reference Yarn names / Fabric APIs absent here.
tasks.named<JavaCompile>("compileJava") {
    exclude("online/slavok/FriendlyPhantoms.java")
    exclude("online/slavok/mixin/**")
    exclude("online/slavok/client/**")
}

tasks.processResources {
    exclude("fabric.mod.json")
    exclude("*.mixins.json")
    val props = mapOf(
        "version" to project.version,
        "minecraft_dep" to nf.depends,
        "pack_format" to nf.packFormat,
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(props)
    }
}

// Stonecutter's comment-processing must run before ModDevGradle resolves sources.
tasks.named("createMinecraftArtifacts") {
    dependsOn(tasks.named("stonecutterGenerate"))
}

tasks.withType<JavaCompile>().configureEach {
    options.release = nf.java
}

java {
    val jv = JavaVersion.toVersion(nf.java)
    sourceCompatibility = jv
    targetCompatibility = jv
    toolchain.languageVersion = JavaLanguageVersion.of(nf.java)
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

publishMods {
    // NeoForge is Mojang-mapped in production: the plain `jar` is the final jar.
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    // Distinct from the Fabric version of the same Minecraft version so the two
    // do not collide as Modrinth version numbers on the shared project.
    version.set("${property("mod_version")}+neoforge.mc$mcVersion")
    changelog.set("See the GitHub release notes: https://github.com/iSlavok/Friendly-Phantoms/releases")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("neoforge")
    displayName.set("Friendly Phantoms ${property("mod_version")} (NeoForge, MC $mcVersion)")

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(nf.gameVersions)
    }
}

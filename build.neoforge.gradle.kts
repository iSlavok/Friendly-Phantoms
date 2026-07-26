// Build script for NeoForge nodes (Mojang-mapped). The shared mod source is
// reused via source-set excludes: only PhantomBehavior (no Minecraft references)
// and the online.slavok.neoforge package compile here. The Fabric mixin, the
// Fabric entrypoint and the ModMenu screen are excluded — NeoForge implements the
// behaviour with a LivingChangeTargetEvent listener instead of a mixin.
plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.17.491"
    id("architectury-plugin") version "3.5.169"
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

architectury {
    platformSetupLoomIde()
    neoForge()
}

repositories {
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.officialMojangMappings())
    "neoForge"("net.neoforged:neoforge:${nf.neoforge}")
}

// Compile only the shared, Minecraft-agnostic code plus the NeoForge entrypoint.
// Everything Fabric-specific is dropped so it never has to resolve against Mojmaps.
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

tasks.withType<JavaCompile>().configureEach {
    options.release = nf.java
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(nf.java)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

publishMods {
    file.set(tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile })
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

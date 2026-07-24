// Build script for unobfuscated Minecraft (26+). Unlike the Yarn-mapped versions,
// this Loom plugin does NOT remap: there are no mappings, dependencies use plain
// `implementation`, and the artifact is `jar` (not `remapJar`).
plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17.17"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

val mcVersion = stonecutter.current.version
val modmenuVersion = "20.0.1"
val javaVersion = 25
val mcDepends = ">=26.1 <27"
val gameVersions = listOf("26.1", "26.2")

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String

base {
    archivesName = property("archives_base_name") as String
}

repositories {
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("com.terraformersmc:modmenu:$modmenuVersion")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "java_level" to javaVersion,
        "minecraft_dep" to mcDepends,
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) {
        expand(props)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaVersion
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(javaVersion)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

publishMods {
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    changelog.set("See the GitHub release notes: https://github.com/iSlavok/Friendly-Phantoms/releases")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    displayName.set("Friendly Phantoms ${property("mod_version")} (MC $mcVersion)")

    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(gameVersions)
        optional("modmenu")
    }
}

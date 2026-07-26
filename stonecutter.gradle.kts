plugins {
    id("dev.kikugie.stonecutter")
}

// Active version used by the IDE and single-version tasks (build/runClient/...).
// Since Stonecutter 0.7 a plain `./gradlew build` builds every declared version.
stonecutter active "1.21.8"

// Convenience: one stable button per run type that targets the ACTIVE version
// (switch it with the Stonecutter version switcher). Delegates to the active
// node's runClient/runServer — works for both Fabric (Loom) and NeoForge
// (ModDevGradle), which share those task names.
for (type in listOf("Client", "Server")) {
    tasks.register("runActive$type") {
        group = "friendly phantoms"
        description = "Runs the active version's Minecraft $type"
        dependsOn(":${stonecutter.current!!.project}:run$type")
    }
}

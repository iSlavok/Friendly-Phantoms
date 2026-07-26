// Standalone build: the server plugin is deliberately NOT part of the mod's
// Stonecutter/Architectury version grid (different API world, nothing shared).
// Build it with the root wrapper via `./gradlew -p plugin <task>` so the heavy
// Loom/Minecraft version nodes are never configured just to build a plugin.
rootProject.name = "friendly-phantoms-plugin"

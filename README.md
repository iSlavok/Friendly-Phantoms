# Friendly Phantoms

**Phantoms will no longer annoy you, but you can still get their membranes.**

A tiny mod (and server plugin): phantoms stop targeting and attacking players.
They still spawn and can be killed, so phantom membranes remain farmable.

Ships as:

- **Fabric / Quilt** mod — Minecraft 1.18.2 – 26.2 (the Fabric jar runs on Quilt).
- **NeoForge** mod — Minecraft 1.20.6 and 1.21.8.
- **Server plugin** — one jar for Bukkit / Spigot / Paper / Purpur / Folia (MC 1.13+).

The mods build from one shared source via
[Stonecutter](https://stonecutter.kikugie.dev/) (Fabric with Fabric Loom,
NeoForge with ModDevGradle). `./gradlew build` compiles every mod version
(26+ needs JDK 25); the plugin is a standalone build (`./gradlew -p plugin build`).
ModMenu is optional (Fabric only).

## Running locally

Every artifact has an IDE-runnable Gradle task.

**Mods — client & server.** Pick the version with the Stonecutter version switcher
(IntelliJ Stonecutter panel, or `./gradlew "Set active project to <version>"`),
then run — both are under the **friendly phantoms** task group:

- `runActiveClient` — the active version's client
- `runActiveServer` — the active version's dedicated server

They delegate to the active node, Fabric *or* NeoForge. To run a specific version
without switching the active one, use its node task directly, e.g.
`:1.21.8:runClient`, `:1.20.6-neoforge:runServer`, `:26.2:runClient`.
NeoForge also has `:<node>:runGameTestServer` — a headless smoke that loads the
mod on a server and exits.

**Plugin — server.** `./gradlew -p plugin runServer` downloads a Paper server and
runs it with the plugin loaded (EULA auto-accepted for the dev run). Change the
Minecraft version with `-Prun_mc=1.20.6`; the same jar also runs on
Spigot/Purpur/Folia. Plugins are server-side only — no client.

**Seeing the effect in-game** (any client/server):

```
/gamerule doDaylightCycle false
/time set night
/summon minecraft:phantom ~ ~8 ~
```

A vanilla phantom dives and attacks; a friendly one circles but never attacks.
Hit it to confirm membranes still drop.

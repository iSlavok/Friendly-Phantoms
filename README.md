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

**Mods — client & server (top-right Run dropdown).** Fabric Loom / ModDevGradle
auto-generate IntelliJ run configurations for the **active** version — look in the
Run/Debug dropdown (top-right) for **Minecraft Client** / **Minecraft Server**
(or **NeoForge Client/Server** when a NeoForge node is active). Switch which
version is active with the Stonecutter version switcher (IntelliJ Stonecutter
panel, or `./gradlew "Set active project to <version>"`) and reload Gradle; the
configs follow the active version.

Equivalent Gradle tasks (Gradle tool window → *friendly-phantoms → Tasks*):

- `runActiveClient` / `runActiveServer` (group **friendly phantoms**) — the active
  version's client/server, Fabric *or* NeoForge.
- A specific version without switching: its node task, e.g. `:1.21.8:runClient`,
  `:1.20.6-neoforge:runServer`, `:26.2:runClient`.
- NeoForge `:<node>:runGameTestServer` — headless smoke that loads the mod and exits.

**Plugin — server (Gradle tool window).** The plugin is an included build, so it
appears in the Gradle tool window as **friendly-phantoms-plugin** → *Tasks → run
paper → runServer* (double-click to run). It downloads a Paper server and runs it
with the plugin loaded (EULA auto-accepted for the dev run). From a terminal:
`./gradlew -p plugin runServer`. Change the Minecraft version with
`-Prun_mc=1.20.6`; the same jar also runs on Spigot/Purpur/Folia. Plugins are
server-side only — no client.

**Seeing the effect in-game** (any client/server):

```
/gamerule doDaylightCycle false
/time set night
/summon minecraft:phantom ~ ~8 ~
```

A vanilla phantom dives and attacks; a friendly one circles but never attacks.
Hit it to confirm membranes still drop.

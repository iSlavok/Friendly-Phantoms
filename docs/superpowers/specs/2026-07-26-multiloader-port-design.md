# Friendly Phantoms — Multi-loader & Server-plugin Port

**Date:** 2026-07-26
**Status:** Approved (design)
**Author:** iSlavok (with Claude)

## 1. Goal

Ship the "phantoms never attack players, but still spawn and remain farmable"
behaviour beyond Fabric: add a **NeoForge** mod build, a **server plugin** that
covers the Bukkit → Folia fork chain, and mark the existing Fabric jar as
**Quilt**-compatible. Publish everything to a single **Modrinth** project.

Non-goals (explicitly dropped after research — see §9):
- **Forge (LexForge)** — superseded by NeoForge; only useful for legacy ≤1.20.1.
- **LiteLoader** — dead, newest supported Minecraft is 1.12.2.
- **Data pack** — the only vanilla mechanism (scoreboard team + `friendlyFire=false`)
  is an imperfect fit for the phantom AI goal and consumes the entity's single
  team slot; not worth shipping.
- **Hangar / CurseForge / SpigotMC** — Modrinth only for now.

## 2. Background — what is actually being ported

The entire gameplay logic is a single effect: a phantom must not target/attack
players. Today this is one SpongePowered mixin:
- MC `<1.22` (Yarn): inject `PhantomEntity#canTarget(EntityType)` → return `false`.
- MC `26+` (Mojmap, unobfuscated): inject `Phantom$PhantomAttackPlayerTargetGoal#canUse()` → `false`.

`PhantomBehavior.friendly` is a static boolean test seam (always `true` in
production; flipped off only by gametests to prove the mixin is the cause). There
are no items, blocks, config, or networking. The ModMenu screen is cosmetic.

The build is multi-**version** via Stonecutter (kikugie): one shared source tree,
per-version preprocessor comments (`//? if <1.22 { … }`). Yarn-mapped anchors
(1.18.2, 1.19.4, 1.20.4, 1.20.6, 1.21.8) use the central `build.gradle.kts`;
unobfuscated anchors (26.1.2, 26.2) use `build.unobfuscated.gradle.kts` and need
JDK 25. CI builds + gametests all anchors; a `v*` tag releases to GitHub +
Modrinth via `me.modmuss50.mod-publish-plugin`.

## 3. Scope of this project

| Target | Verdict | Coverage |
|---|---|---|
| Fabric | already shipped | 1.18.2 – 26.2 |
| Quilt | free (tag only) | Fabric jar runs on Quilt as-is (QFAPI discontinued Dec 2025) |
| NeoForge | new build | **1.20.4, 1.20.6, 1.21.8** (NeoForge starts at 1.20.2; 26.x deferred — see §8) |
| Server plugin | new build | one jar, Bukkit/Spigot/Paper/Purpur/Folia, MC 1.13 → current |

## 4. Architecture

Mono-repo. Two independent worlds:

```
Friendly-Phantoms/
├─ mod/                       # Stonecutter (versions) × Architectury (loaders)
│  ├─ common/                 # shared: PhantomBehavior, MOD_ID, logger, mixin
│  ├─ fabric/                 # ModInitializer + fabric.mod.json + ModMenu screen
│  └─ neoforge/               # @Mod entrypoint + LivingChangeTargetEvent listener
├─ plugin/                    # plain Gradle, spigot-api — NOT Stonecutter/Architectury
│  └─ src/main/java + plugin.yml
└─ docs/superpowers/specs/
```

### 4.1 Mod side — Stonecutter × Architectury

Follows the `stonecutter-template-architectury` pattern: each Stonecutter version
node contains the Architectury `common` / `fabric` / `neoforge` subprojects.

- **`common`** — the mixin (with its existing `//?` version branches) and
  `PhantomBehavior`. Fabric API exposes no target-change event, so Fabric keeps
  using the mixin. The mixin JSON lives here and is referenced by both loaders.
- **`fabric`** — `ModInitializer`, `fabric.mod.json`, the optional ModMenu screen.
- **`neoforge`** — `@Mod` class, `neoforge.mods.toml`. Behaviour implemented via
  **`LivingChangeTargetEvent`** (cancel when `event.getEntity() instanceof Phantom`),
  *not* a mixin — no mixin config, no mapping fragility. ModMenu screen omitted
  (ModMenu is Fabric-only).

Rationale for Architectury over Modstitch: user's choice; industry-standard,
better documented, more resilient across new Minecraft drops. Cost: the shared
code is tiny (one boolean), so `common` is nearly empty and the restructure is
the largest single piece of work.

### 4.2 Plugin side — separate module

`plugin/` is a standalone Gradle project (Bukkit API ≠ Minecraft/Yarn/Mojmap;
nothing is shared with the mod). Compile against **`spigot-api`** (`compileOnly`)
for maximum fork coverage. Single event listener:

```java
@EventHandler(ignoreCancelled = true)
public void onTarget(EntityTargetEvent e) {
    if (e.getEntity() instanceof Phantom && e.getTarget() instanceof Player) {
        e.setTarget(null);      // clear an already-assigned target
        e.setCancelled(true);   // block acquiring the player as a new target
    }
}
```

`plugin.yml`: `name`, `version`, `main`, `api-version`, and
**`folia-supported: true`** (ignored by Spigot/Paper/Purpur, required for Folia
to load the plugin). No shading (zero runtime deps). One jar covers the whole
fork chain because Bukkit ⊂ Spigot ⊂ Paper ⊂ Purpur and the listener is
region-local (Folia-safe: no scheduler, no cross-region access).

## 5. Version / artifact matrix

| MC anchor | Fabric (+Quilt tag) | NeoForge | Plugin |
|---|:-:|:-:|:-:|
| 1.18.2, 1.19.4 | ✅ | — | (n/a, one jar) |
| 1.20.4, 1.20.6, 1.21.8 | ✅ | ✅ | — |
| 26.1.2, 26.2 | ✅ (unobf script) | ⚠️ deferred | — |
| Plugin (single jar) | — | — | ✅ MC 1.13 → current |

Artifact version strings gain a loader dimension to avoid Modrinth slug / GitHub
asset-name collisions: `1.0.1+<loader>.<mc>` (e.g. `1.0.1+neoforge.1.21.8`,
`1.0.1+paper` for the plugin).

## 6. Testing

| Target | CI-automatable | Method |
|---|:-:|---|
| Fabric | ✅ | existing GameTest (A/B on `canTarget`) |
| NeoForge | ✅ | `runGameTestServer` (headless); reuse the same phantom assertion |
| Plugin (logic) | ✅ | MockBukkit — fire `EntityTargetEvent`(mock Phantom → mock Player), assert cancelled + target null |
| Folia / Quilt / full Paper server | ❌ | manual smoke |

## 7. Publishing (Modrinth only)

- Upgrade `me.modmuss50.mod-publish-plugin` **0.8.4 → 2.1.1**.
- **One Modrinth project** (`Jpkt8zkB`, type `mod`) holds all mod jars *and* the
  plugin jar. Modrinth's `modLoaders` in the publish plugin is free-form, so the
  same tooling publishes plugin loaders (`bukkit`/`spigot`/`paper`/`purpur`/`folia`).
- Fabric jars additionally tagged `quilt`.
- The plugin jar is a Modrinth version tagged with the five plugin loaders.

## 8. Risks & mitigations

- **Architectury restructure may break the working Fabric build.** Mitigation:
  Stage 2 is "Fabric parity, CI green" with no behaviour change; do it before any
  NeoForge work; verify every anchor still builds + gametests.
- **NeoForge 26.x via Architectury is bleeding-edge** (unobfuscated + Java 25 +
  Gradle 9.4). Mitigation: scope NeoForge to 1.20.4–1.21.8; keep 26.x Fabric-only
  through the existing `build.unobfuscated.gradle.kts`. Revisit 26.x NeoForge later.
- **Plugin: forgetting `setTarget(null)`** leaves the phantom finishing an
  in-flight target → still attacks. Covered by the MockBukkit test.
- **Plugin: forgetting `folia-supported: true`** → Folia silently refuses to load.
  Covered by a plugin.yml assertion in the build/test.
- **CI matrix growth.** Keep Stonecutter `chiseledBuild` building the whole
  version grid in one Gradle run; split GitHub jobs only by loader/JDK, never by
  MC version.

## 9. Alternatives considered (and rejected)

- **Modstitch** instead of Architectury — lighter for a one-listener mod, but user
  chose Architectury for ecosystem maturity.
- **Reusing the mixin on NeoForge** instead of the event — works (26+ mixin is
  already Mojmap), but the event is more resilient and avoids mixin config.
- **Data pack** — team + `friendlyFire=false` is the only working vanilla path;
  imperfect for phantom AI and consumes the team slot. Dropped.
- **Forge / LiteLoader** — dropped (see §1).

## 10. Delivery plan (separate PRs)

1. **PR: server plugin** — isolated `plugin/` module, listener, plugin.yml,
   MockBukkit test, Gradle build. Highest value, zero risk to existing build.
2. **PR: Architectury restructure (Fabric parity)** — move mod source into
   `mod/common` + `mod/fabric`, keep Stonecutter versions, no behaviour change,
   CI green on every anchor. Add Quilt tag.
3. **PR: NeoForge support** — `mod/neoforge` module, `LivingChangeTargetEvent`
   listener, `@Mod` entrypoint, gametest; anchors 1.20.4/1.20.6/1.21.8.
4. **PR: publishing** — bump publish plugin to 2.1.1, wire Modrinth loaders for
   NeoForge + plugin, update release workflow / version scheme.

## 11. Acceptance criteria

- `./gradlew build` produces: Fabric jars (all existing anchors), NeoForge jars
  (1.20.4/1.20.6/1.21.8), and one plugin jar.
- Fabric + NeoForge gametests pass headless in CI; plugin MockBukkit test passes.
- A `v*` tag publishes all artifacts to the one Modrinth project with correct
  loader tags (fabric+quilt, neoforge, bukkit/spigot/paper/purpur/folia).
- No regression: every currently-shipped Fabric anchor still builds and gametests.

# Server Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a single server-plugin jar that stops phantoms attacking players (keeping them spawnable and farmable) across Bukkit/Spigot/Paper/Purpur/Folia.

**Architecture:** A standalone `plugin/` Gradle module, independent of the mod's Stonecutter/Architectury build. One `JavaPlugin` registering one `EntityTargetEvent` listener. Compiled against `spigot-api` (`compileOnly`) for maximum fork coverage; `folia-supported: true` in `plugin.yml` adds Folia. No shading (zero runtime deps).

**Tech Stack:** Java 21, Gradle (Kotlin DSL), spigot-api 1.21.x, MockBukkit (test), JUnit 5.

## Global Constraints

- Behaviour: phantoms never acquire/keep a **Player** target; spawning and membrane drops untouched (listener only intercepts target acquisition).
- Listener must both `setTarget(null)` and `setCancelled(true)` — cancel alone leaves an in-flight target.
- `plugin.yml` MUST contain `folia-supported: true` (Folia refuses to load otherwise) and an `api-version`.
- Compile the API `compileOnly` — never bundle the server API.
- Group/id consistent with the mod: base name `friendly-phantoms`, package `online.slavok.plugin`.
- This module must NOT be pulled into the Stonecutter version grid.

---

### Task 1: Plugin module skeleton + build

**Files:**
- Create: `plugin/build.gradle.kts`
- Modify: `settings.gradle.kts` (register `plugin` as an ordinary Gradle subproject, outside Stonecutter)

**Interfaces:**
- Produces: a buildable `:plugin` subproject with `spigot-api` compileOnly, MockBukkit + JUnit testImplementation, Java 21 toolchain, `shadowJar`-free `jar`.

- [ ] **Step 1: Write `plugin/build.gradle.kts`**

```kotlin
plugins {
    java
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigot" }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") { name = "sonatype" }
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("org.spigotmc:spigot-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.75.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

base { archivesName = "friendly-phantoms-plugin" }
version = "${property("mod_version")}+paper"
group = property("maven_group") as String

tasks.test { useJUnitPlatform() }

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
```

> Note: verify exact `mockbukkit` artifact id/version at implementation time (`org.mockbukkit.mockbukkit:mockbukkit-v1.21`); adjust to the newest published coordinate for MC 1.21.

- [ ] **Step 2: Register the module in `settings.gradle.kts`**

Add, OUTSIDE the `stonecutter { … }` block (so Stonecutter does not chisel it):

```kotlin
include("plugin")
```

- [ ] **Step 3: Verify the project resolves**

Run: `./gradlew :plugin:dependencies --configuration compileClasspath -q`
Expected: resolves spigot-api without error.

- [ ] **Step 4: Commit**

```bash
git add plugin/build.gradle.kts settings.gradle.kts
git commit -m "build: add server-plugin module skeleton"
```

---

### Task 2: The listener (TDD)

**Files:**
- Create: `plugin/src/main/java/online/slavok/plugin/FriendlyPhantomsPlugin.java`
- Test: `plugin/src/test/java/online/slavok/plugin/FriendlyPhantomsPluginTest.java`

**Interfaces:**
- Produces: `FriendlyPhantomsPlugin extends JavaPlugin implements Listener` with
  `void onTarget(EntityTargetEvent e)` that, for a `Phantom` targeting a `Player`,
  calls `e.setTarget(null)` and `e.setCancelled(true)`.

- [ ] **Step 1: Write the failing test**

```java
package online.slavok.plugin;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class FriendlyPhantomsPluginTest {
    ServerMock server;
    FriendlyPhantomsPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(FriendlyPhantomsPlugin.class);
    }

    @AfterEach
    void tearDown() { MockBukkit.unmock(); }

    @Test
    void phantomTargetingPlayerIsCancelledAndCleared() {
        Player player = server.addPlayer();
        Phantom phantom = (Phantom) server.getWorlds().get(0)
                .spawnEntity(player.getLocation(), EntityType.PHANTOM);

        EntityTargetEvent event =
                new EntityTargetEvent(phantom, player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
        server.getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(), "phantom→player target must be cancelled");
        assertNull(event.getTarget(), "target must be cleared to null");
    }

    @Test
    void nonPhantomTargetingPlayerIsUntouched() {
        Player player = server.addPlayer();
        var zombie = server.getWorlds().get(0)
                .spawnEntity(player.getLocation(), EntityType.ZOMBIE);

        EntityTargetEvent event =
                new EntityTargetEvent(zombie, player, EntityTargetEvent.TargetReason.CLOSEST_PLAYER);
        server.getPluginManager().callEvent(event);

        assertFalse(event.isCancelled(), "non-phantom must be untouched");
        assertNotNull(event.getTarget());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :plugin:test`
Expected: FAIL — `FriendlyPhantomsPlugin` does not exist / not compiling.

- [ ] **Step 3: Write the minimal implementation**

```java
package online.slavok.plugin;

import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendlyPhantomsPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Friendly Phantoms loaded — phantoms will no longer attack.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Phantom && event.getTarget() instanceof Player) {
            event.setTarget(null);
            event.setCancelled(true);
        }
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :plugin:test`
Expected: PASS (both tests).

- [ ] **Step 5: Commit**

```bash
git add plugin/src
git commit -m "feat(plugin): cancel phantom→player targeting"
```

---

### Task 3: plugin.yml (with a load-safety test)

**Files:**
- Create: `plugin/src/main/resources/plugin.yml`
- Test: extend `FriendlyPhantomsPluginTest` with a plugin.yml assertion.

**Interfaces:**
- Consumes: `FriendlyPhantomsPlugin` (main class).
- Produces: a valid `plugin.yml` with `folia-supported: true` and `api-version`.

- [ ] **Step 1: Write the failing test (Folia flag + api-version present)**

```java
    @Test
    void pluginYmlDeclaresFoliaAndApiVersion() throws Exception {
        try (var in = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(in, "plugin.yml must be on the classpath");
            String yml = new String(in.readAllBytes());
            assertTrue(yml.contains("folia-supported: true"), "must declare Folia support");
            assertTrue(yml.contains("api-version:"), "must declare api-version");
        }
    }
```

- [ ] **Step 2: Run to verify it fails**

Run: `./gradlew :plugin:test --tests '*pluginYmlDeclaresFoliaAndApiVersion'`
Expected: FAIL — resource missing.

- [ ] **Step 3: Create `plugin/src/main/resources/plugin.yml`**

```yaml
name: FriendlyPhantoms
version: '${version}'
main: online.slavok.plugin.FriendlyPhantomsPlugin
api-version: '1.13'
folia-supported: true
description: Phantoms will no longer attack players, but stay farmable.
author: isSLAVOK
website: https://github.com/iSlavok/Friendly-Phantoms
```

> `api-version: '1.13'` is the phantom floor. If a modern server warns, bump to a
> recent value; the listener is forward-compatible regardless.

- [ ] **Step 4: Run to verify it passes**

Run: `./gradlew :plugin:test`
Expected: PASS (all three tests).

- [ ] **Step 5: Build the jar and sanity-check its contents**

Run: `./gradlew :plugin:jar && jar tf plugin/build/libs/friendly-phantoms-plugin-*.jar`
Expected: contains `plugin.yml` and `online/slavok/plugin/FriendlyPhantomsPlugin.class`.

- [ ] **Step 6: Commit**

```bash
git add plugin/src/main/resources/plugin.yml plugin/src/test
git commit -m "feat(plugin): plugin.yml with Folia support"
```

---

### Task 4: CI job for the plugin

**Files:**
- Modify: `.github/workflows/build.yml` (add a `plugin` job)

**Interfaces:**
- Consumes: `:plugin:build`.

- [ ] **Step 1: Add a plugin job**

```yaml
  plugin:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: 21
          distribution: temurin
      - uses: gradle/actions/setup-gradle@v4
      - name: Build & test plugin
        run: ./gradlew :plugin:build --stacktrace
      - uses: actions/upload-artifact@v4
        with:
          name: plugin-jar
          path: plugin/build/libs/*.jar
          if-no-files-found: error
```

- [ ] **Step 2: Verify the whole module builds locally**

Run: `./gradlew :plugin:build`
Expected: compiles, tests pass, jar produced.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/build.yml
git commit -m "ci: build and test the server plugin"
```

---

## Self-Review

- **Spec coverage:** §4.2 (plugin module, listener, spigot-api, folia flag, no shading) → Tasks 1–3. §6 (MockBukkit) → Task 2. Publishing (§7) is a later PR, not this one. ✓
- **Placeholders:** MockBukkit coordinate flagged for verification at implementation time — acceptable (external, version-drifting). No other placeholders.
- **Type consistency:** `FriendlyPhantomsPlugin`, `onTarget(EntityTargetEvent)`, `online.slavok.plugin` consistent across tasks. ✓

plugins {
    id("dev.kikugie.stonecutter")
}

// Active version used by the IDE and single-version tasks (build/runClient/...).
// Since Stonecutter 0.7 a plain `./gradlew build` builds every declared version.
stonecutter active "1.21.8"

# KoyClient

A lightweight Fabric client mod (Minecraft 1.21.4) with a Feather-style HUD suite.

**Modules:** FPS, CPS, Coordinates, Ping, Keystrokes, Armor Status, Toggle Sprint, Zoom.
**Menu:** Right Shift opens the module menu (toggle modules, "Edit HUD Layout" to drag/resize). Hold C to zoom.
Settings save to `config/koyclient.json`.

## Build

1. Install JDK 21.
2. The Gradle wrapper isn't included. Either run `gradle wrapper --gradle-version 8.12` once (needs Gradle installed),
   or generate a fresh project at https://fabricmc.net/develop and copy its `gradlew*` + `gradle/` folder in.
3. `./gradlew runClient` to test, `./gradlew build` for the jar in `build/libs/`.

If versions have moved on, update `gradle.properties` using the values on https://fabricmc.net/develop.

## Adding a module

Extend `TextModule` (one-line box) or `KoyModule` (custom drawing), then add it in `ModuleManager`.

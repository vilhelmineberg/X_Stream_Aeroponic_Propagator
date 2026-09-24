# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Android app tracking which plant type sits in each plug of one or more aeroponic propagation boxes. The user-visible / Google Play name is "Plug Tracker – Propagation" (kept generic to avoid the X-Stream trademark); the repo, package name, and internal docs keep the original "X-Stream Aeroponic Propagator" working title. Single module (`:app`), single Activity, Jetpack Compose UI, local Room database, no authentication or networking.

- Package/namespace: `se.vilhelmineberg.x_streamaeroponicpropagator`
- AGP 9.4.1 with built-in Kotlin (2.3.21 embedded) — do NOT apply `org.jetbrains.kotlin.android`, it's a hard error under AGP 9. The Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`, versioned to match the embedded Kotlin) and KSP (standalone 2.3.x versioning) are applied on top.
- Gradle 9.6, Java 11, minSdk 35 (java.time is used freely), target/compileSdk 37
- Dependencies are managed via the version catalog at `gradle/libs.versions.toml`; add new libraries there and reference them as `libs.*` in `app/build.gradle.kts`. Compose libraries come from the Compose BOM, except `material-icons-core` which is no longer in the BOM and carries an explicit version.
- Configuration cache is enabled (`gradle.properties`)

## Architecture

All code lives under `app/src/main/java/se/vilhelmineberg/x_streamaeroponicpropagator/`:

- `data/` — Room layer. `Box` (name + rows/cols grid) and `Plant` (boxId + 0-based row-major `position` + name + `plantedEpochDay`; unique index on boxId+position, FK cascade delete). `BoxPreset` enum defines the standard X-Stream sizes (20/40/80/120) and their grid layouts. `AppDatabase` is a manual singleton (`AppDatabase.get(context)`) — no DI framework.
- `ui/PropagatorViewModel.kt` — single `AndroidViewModel` holding all state as `StateFlow`s: box list, selected box (falls back to first box), plants of the selected box (`flatMapLatest`), distinct plant names for suggestions, and the transient set of selected empty plug positions.
- `ui/PropagatorScreen.kt` — the whole screen: top bar with box dropdown + new/delete box actions, plug grid (plain rows inside one `LazyColumn`, cells sized by `weight(1f).aspectRatio(1f)`), plant list with planted date and days-since, and a bottom selection bar. Plug interaction model: tapping an empty plug toggles selection (bulk planting via the bottom bar), tapping a filled plug opens the detail dialog (edit/delete).
- `ui/Dialogs.kt` — all dialogs (new box, plant-name entry with suggestions, plug detail, delete box) plus date/days formatting helpers. Dates are stored as epoch days; "days since" is computed against `LocalDate.now()`.
- `MainActivity.kt` — `ComponentActivity` with an inline green Material3 theme (no XML theming beyond the NoActionBar launch theme).

Editing a plant's name keeps its original planting date; replanting a plug (upsert on boxId+position) resets the date to today.

## Commands

All commands run from the repo root via the Gradle wrapper:

```sh
./gradlew assembleDebug                 # build debug APK
./gradlew test                          # JVM unit tests
./gradlew testDebugUnitTest --tests "se.vilhelmineberg.x_streamaeroponicpropagator.ExampleUnitTest"   # single unit test class
./gradlew connectedAndroidTest          # instrumented tests (requires device/emulator)
./gradlew lint                          # Android Lint
./gradlew installDebug                  # install on connected device/emulator
```

# Football Dynasty

Reconstruction and modernization of a legacy football-management game for current Android.

## Current phase

**Phase 1 — Technical reconstruction and behavioral parity.**

The project is being rebuilt on a modern Android foundation while preserving the original game content and behavior as the reference baseline.

### Non-negotiable data freeze

Until Phase 1 is declared complete, do **not** update, replace, rename, remove or add:

- players;
- player attributes/ratings;
- squads/rosters;
- clubs;
- competitions;
- competition formats and sporting rules represented by the legacy build.

See [`docs/DATA_FREEZE.md`](docs/DATA_FREEZE.md).

## Technical baseline

The new Android project starts with:

- Kotlin;
- Android Gradle Plugin 9.3.x;
- JDK 17;
- `compileSdk 37`;
- `targetSdk 36`;
- `minSdk 26`;
- Jetpack Compose + Material 3;
- AndroidX;
- architecture designed to evolve toward ViewModel, Coroutines, Room and separated game-engine/domain layers.

## Legacy reference policy

The decompiled APK is a **reference source**, not the new application architecture. Decompiled Java, SMALI and binary assets must not be copied wholesale into `main`. Missing Java decompilation must be reconstructed and validated against SMALI before being considered behaviorally equivalent.

See [`docs/LEGACY_INVENTORY.md`](docs/LEGACY_INVENTORY.md) and [`docs/MIGRATION_PLAN.md`](docs/MIGRATION_PLAN.md).

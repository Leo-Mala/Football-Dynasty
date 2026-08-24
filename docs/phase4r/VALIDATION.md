# Phase 4R — validation ledger

## Fixed inputs

- source branch at start: `phase4/core-game-domain@e1b5c516507fb38012093fff838929d26a6b2459`
- official legacy ZIP SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`
- official `.ban` count: 1,687

## Completed characterization gates

- ZIP recursive inventory: PASS
- manifest/version/package audit: PASS
- `.ban` read-only scan: PASS (`1687/1687`, failures `0`)
- `.ban` physical aggregate fingerprint: PASS
- `.ban` semantic aggregate fingerprint: PASS
- active immutable 2026 fixture established: PASS
- save family reidentified as `.a26 + .s26`: PASS
- critical save/load decompiler uncertainty classified for SMALI validation: PASS
- external sporting-data imports: NONE

## Android/build correction

The known AGP 9.x Kotlin DSL failure was caused by passing a `File` to `AndroidSourceDirectorySet.directories.add`, whose active API expects a String path. Phase 4R changes:

```kotlin
getByName("test").assets.directories.add("$projectDir/schemas")
```

The Room schema remains available to migration tests; no test or gate was removed.

## Required CI gate

`.github/workflows/phase4r-validation.yml` executes:

1. `./gradlew help --no-daemon`
2. `./gradlew :app:kspDebugKotlin :app:copyRoomSchemas --no-daemon --stacktrace`
3. `./gradlew :app:testDebugUnitTest --no-daemon --stacktrace`
4. JUnit summary with zero failures/errors and required core benchmark evidence
5. active Brasfoot 2026 fixture SHA-256 check
6. `./gradlew :app:assembleDebug --no-daemon --stacktrace`
7. Room V1/V2 integrity and no destructive migration fallback

The workflow uses `concurrency.cancel-in-progress: true` and does not auto-commit generated artifacts or mutate the validation branch.

## Merge policy

This ledger does not declare the PR merge-ready until the exact final head has a successful Android gate, the remaining domain/legacy impact documentation is complete, the base is re-audited, and no relevant review/conflict remains.

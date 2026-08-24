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

## Final certified CI evidence

`Phase 4R Validation` run `32693529780` completed successfully for PR #3 merge ref built from head `2f8b6d253af521cd472128eae18254b38093d657` over base `e1b5c516507fb38012093fff838929d26a6b2459`.

Validated gates:

1. `./gradlew help --no-daemon` — PASS
2. `./gradlew :app:kspDebugKotlin :app:copyRoomSchemas --no-daemon --stacktrace` — PASS
3. `./gradlew :app:testDebugUnitTest --no-daemon --stacktrace` — PASS
4. JUnit summary — `tests=45 failures=0 errors=0 skipped=0`
5. core performance evidence — `PHASE4_CORE_BENCHMARK commands=365 elapsedNanos=107533485 fingerprint=3a33ba602ed801f94fcfd5ea7ac69556a425b2e340558509c706719a3a743d34`
6. active Brasfoot 2026 fixture SHA-256 — PASS (`trepenne_smr.ban`)
7. `./gradlew :app:assembleDebug --no-daemon --stacktrace` — PASS
8. Room V1/V2 schema integrity — PASS
9. migration V1→V2 policy audit — PASS
10. `fallbackToDestructiveMigration` absence — PASS

The workflow uses `concurrency.cancel-in-progress: true`, does not weaken tests, and does not auto-mutate the branch.

## Final merge audit before documentation closure

At the certified head:

- PR #3 mergeability: true;
- branch relation: Phase 4R was 19 commits ahead and 0 behind its base;
- reviews: none pending;
- review threads: none pending;
- base remained `phase4/core-game-domain@e1b5c516507fb38012093fff838929d26a6b2459`;
- no external sporting-data changes were introduced.

Because this documentation commit changes the PR head after the certified run above, the exact documentation-closure head must receive the same Phase 4R Validation gate before merge. Merge remains prohibited until that exact final head is green.
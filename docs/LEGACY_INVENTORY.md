# Legacy Inventory

Source inspected: decompiled APK archive supplied for the reconstruction project.

## APK metadata

- Legacy package: `com.brasfoot.v2028`
- Version name: `2.7`
- Version code: `69`
- Legacy `minSdk`: `19`
- Legacy `targetSdk`: `29`
- Decompiled manifest reports `compileSdkVersion`: `23`

## Decompiled archive inventory

- Total files in archive: **7,982**
- Decompiled Java files: **575**
- SMALI files: **575**
- Kotlin files: **0**
- Java classes directly under `com/brasfoot/v2028`: **118**
- Approximate lines across those 118 game-package Java files: **47,870**
- Classes extending Android `Activity`: **53**
- `GfxCore.java`: approximately **22,713 lines**

## Incomplete Java decompilation

The Java output contains **36 methods** replaced by decompiler-generated `UnsupportedOperationException("Method not decompiled...")` stubs across **24 files**.

Affected files found in the first inventory include:

- `ActivityClass.java`
- `ActivityConvoca.java`
- `ActivityEditor.java`
- `ActivityEditorTeam.java`
- `ActivityEscolhaTimes.java`
- `ActivityEscala.java`
- `ActivityEstadio.java`
- `ActivityField.java`
- `ActivityJogo.java`
- `ActivityJornal.java`
- `ActivityLoad.java`
- `ActivityMainTeam.java`
- `ActivityPenalty.java`
- `ActivityPref.java`
- `ActivityProcura.java`
- `ActivityResults.java`
- `ActivitySavedTatics.java`
- `ActivityTimes.java`
- `DialogFieldInfo.java`
- `DialogIgrokInfo.java`
- `DialogTatics.java`
- `DialogTimeRodada.java`
- `GfxCore.java`
- `StartOptions.java`

The corresponding SMALI files exist in the archive and are therefore the primary recovery source for methods that Java decompilation lost.

## Legacy technical patterns observed

The first inventory also found extensive use of legacy Android patterns, including:

- `AsyncTask` references;
- `ListView` and `Spinner`-based screens;
- direct Java object serialization through `ObjectInputStream` / `ObjectOutputStream`;
- legacy external-storage access;
- large, highly coupled classes with UI, persistence and game logic mixed together.

## Reconstruction implication

The Java decompilation is useful for readability but cannot be treated as a complete source-of-truth project. Migration must use a three-way process:

1. readable decompiled Java for structure and intent;
2. SMALI for missing or suspicious behavior;
3. behavioral tests/parity checks before replacing legacy logic with cleaner Kotlin implementations.

# Phase 4R — Legacy Reference Baseline v2

## Official legacy corpus

- File: `Brasfoot.apk_Decompiler.com.zip`
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`
- Baseline date: 2026-08-24
- Legacy package: `com.brasfoot.v2020`
- `versionCode`: `202632`
- `versionName`: `Brasfoot.202632`
- Decompiled manifest `compileSdkVersion`: `36`
- Legacy `minSdkVersion`: `23`
- Legacy `targetSdkVersion`: `36`
- Total files: `13,889`
- Java: `5,395`
- SMALI: `5,489`
- `.ban`: `1,687`
- XML: `336`
- PNG: `477`
- WAV: `7`
- Native `.so`: `0`
- DEX files in decompiler ZIP: `0`

The manifest and corpus are consistent with the Brasfoot 2026/27 generation. `202632` is recorded as the version marker observed in the supplied artifact; no external football source is used to reinterpret or update its content.

## Git baseline

- Source branch: `phase4/core-game-domain`
- Source HEAD at rebaseline start: `e1b5c516507fb38012093fff838929d26a6b2459`
- Phase 4R branch: `phase4r/brasfoot-2026-rebaseline`
- Base branch remains `phase4/core-game-domain` unless a later audit proves otherwise.

## Policy

The new ZIP Brasfoot 2026/27 replaces the previous decompiled corpus as the official behavioral and factual legacy reference for Football Dynasty. The existing modern Kotlin/Compose/Room architecture remains the implementation target. Decompiled Java and SMALI are reverse-engineering evidence, not source code to copy wholesale into the application.

No external player, club, rating, competition or roster source is permitted during Phase 4R.

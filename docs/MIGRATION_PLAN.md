# Migration Plan

## Goal

Rebuild the legacy football-management application on a maintainable Android stack while preserving its game content and gameplay behavior during Phase 1.

## Phase 0 — Baseline and protection

- Freeze legacy football data.
- Inventory the decompiled Java and SMALI sources.
- Keep the original/decompiled archive out of the production source tree.
- Establish a modern Android bootstrap that can evolve independently.

## Phase 1 — Behavioral reconstruction

### 1. Application shell

- Kotlin + AndroidX.
- Jetpack Compose + Material 3.
- Single-activity navigation architecture.
- Clear separation between UI and game logic.

### 2. Domain mapping

Create a legacy-to-modern map for at least:

- player;
- club;
- squad;
- manager;
- match;
- fixture/calendar;
- competition;
- table/ranking;
- transfer;
- contract;
- finance;
- stadium;
- save/career state.

Do not rename concepts merely by guesswork. Each mapping must be supported by usage evidence from the legacy code/SMALI.

### 3. Recover incomplete methods

For each Java `Method not decompiled` stub:

1. identify the matching SMALI method;
2. document inputs, outputs and side effects;
3. reconstruct behavior in readable form;
4. add a parity test or reproducible behavioral check;
5. only then migrate it into the modern implementation.

### 4. Game engine extraction

Gradually break the responsibilities currently concentrated in legacy classes into isolated systems, for example:

- match engine;
- competition engine;
- calendar/season engine;
- squad/tactics engine;
- transfer engine;
- finance engine;
- player development engine;
- persistence/save engine.

The first implementation priority is equivalence, not redesign.

### 5. Persistence migration

- Identify the exact legacy save graph and serialization format.
- Build immutable legacy DTOs/readers as needed.
- Preserve the ability to interpret legacy data where technically feasible.
- Introduce modern persistence only behind repositories/adapters.
- Room is the target for structured application state; legacy save import remains a separate boundary.

### 6. UI migration

Migrate screen-by-screen after the required domain behavior is available.

Legacy screen names are evidence, not the target architecture. Modern UI may consolidate many Activities into Compose destinations while preserving equivalent functionality.

### 7. Parity gates

A subsystem may be marked migrated only when:

- its legacy inputs are known;
- its relevant side effects are known;
- missing decompiler logic has been recovered where applicable;
- a repeatable comparison exists;
- data-freeze rules remain intact.

## Future Phase 2 — Content updates

Only after Phase 1 is completed may the project consider updated players, clubs, squads, competitions, formats or factual football data. That work must be isolated from the reconstruction history.

# Data Freeze — Phase 1

Status: **ACTIVE**

This document is a project guardrail for the technical reconstruction phase.

## Frozen content

Until Phase 1 is formally completed, the following legacy content is immutable except for a byte-for-byte or semantics-preserving migration required to make the reconstructed application work:

- players and player identities;
- player attributes, ratings and positions;
- club rosters/squads;
- clubs and club identities;
- competitions;
- competition formats;
- sporting structures and rules encoded by the legacy game;
- factual football data bundled with the legacy application.

## Allowed work

Phase 1 may change only the technical implementation needed to preserve and run the game on current Android, including:

- project/build system;
- package/module organization;
- Kotlin migration;
- AndroidX migration;
- UI technology migration;
- threading/concurrency implementation;
- persistence implementation;
- storage APIs;
- dependency injection;
- tests and diagnostics;
- code naming and decomposition;
- compatibility fixes required by current Android.

## Parity rule

A technical rewrite is not considered complete merely because it compiles. For migrated gameplay logic, the reconstructed implementation must be compared against the legacy behavior and its legacy data inputs.

If a migration exposes an ambiguity in the decompiled Java source, the corresponding SMALI implementation is the reference for recovering behavior before any redesign is attempted.

## Forbidden during Phase 1

Do not use the modernization work as an opportunity to:

- update transfers or squads;
- import current-season football data;
- rebalance ratings;
- add/remove competitions;
- rename clubs or players for current-day accuracy;
- change competition formats because modern real-world rules differ;
- tune gameplay simply because the legacy behavior looks unusual.

Those changes belong to a future content phase after behavioral parity is established.

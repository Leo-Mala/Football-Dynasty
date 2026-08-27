# Phase 8 — `best.s.k` downstream actions and `best.o.g` club-season stats

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

This note closes two reachable mutation boundaries that sit between the already-characterized direct RNG tree and a mutable modern match runtime.

## `best.s.k(...)` after the direct event gate

`LegacyMatchMinuteRules` already owns the direct side/action RNG and probability bounds. The bytecode proves the following downstream work after one of those direct gates succeeds:

- legacy C branch:
  1. call `S(active)`;
  2. increment match counter `O` unconditionally;
  3. only when `S` returned a player, call `best.s.c(...)` for that player;
- legacy D branch:
  1. call `U(active)`;
  2. only when `U` returned a player, call `best.s.d(...)`;
  3. increment match counter `P` unconditionally after that conditional callback;
- type-5 branch:
  1. increment match counter `Q` unconditionally;
  2. resolve the selected side's club;
  3. call `T(active)`;
  4. only when `T` returned a player, call `best.s.a(5,-1,...)`;
- after all three direct gates miss, half `2` and minute index `>=5` call `j(half, minute)`.

The differences in counter timing are intentional. `O`, `P` and `Q` count the corresponding legacy gate path even when the downstream selector returns null. They are not normalized into counts of successfully materialized events.

`S`, `U` and `T` remain the exact selectors already represented by `LegacyMatchPlayerSelectionRules`; the new `LegacyMatchMinuteActionRules` composes those existing selectors with the counter/callback order rather than reimplementing their probability tables.

Every seventh minute the legacy refresh `s(half,minute)` occurs before the downstream player selector. The downstream rule exposes that refresh callback before its candidate-provider snapshot. The direct gate RNG remains owned by `LegacyMatchMinuteRules`; the new rule does not consume or duplicate those draws.

## `best.s.c(...)` card counter prerequisite

Before choosing between event type `2` and type `3`, legacy `best.s.c(...)` calls `best.o.J0()`. SMALI proves `J0()` is exactly `e0++`, and `best.o.w0()` returns the same `e0` field. Therefore the existing `LegacyMatchDisciplinaryRules.applyLegacyC(previousCount)` characterization correctly models the updated-count decision: exactly updated value `2` selects the second event type; other values select the first.

## `best.o.m(club)` hidden club-season mutation

The reachable injury method ends with private `best.o.g(5, club)`. That helper first calls `q0(club)` to find the **first** player-history entry whose persisted club id equals `club.W()` and whose persisted season id equals the current legacy season `best.b.J()`.

If no matching entry exists, a new serializable `best.e` is constructed for current season + club. It is appended only when the player's history list `U` itself is non-null. The method still mutates the newly constructed transient entry if `U` is null.

The `best.e` counter mapping is bytecode-simple and is represented with neutral field names:

- code `2` -> `legacyC++`;
- code `4` -> `legacyD++`;
- code `3` -> `legacyC++` then `legacyD++`;
- code `1` -> `legacyG++`;
- code `5` -> `legacyH++`;
- code `0` -> `legacyF++`;
- code `8` -> `legacyE++`.

An unknown code still creates/retains a missing entry before performing no counter increment, matching the ordering in `best.o.g(...)`.

For the match-engine path, injury uses code `5`, so the player receives one `legacyH` increment in the club-season entry after the injury duration/skill mutation. This persistent legacy effect must be applied before Phase 8 can claim a complete injury runtime integration.

## Modern boundary

`LegacyMatchMinuteActionRules` and `LegacyPlayerClubSeasonStatsRules` remain pure Kotlin. They perform no Android, Room, wall-clock or external-data work. A later transient match runtime can consume them together with `LegacyMatchEventApplicationRules`, `LegacyMatchInjuryRules`, and `LegacyMatchSubstitutionRules` without re-encoding the legacy branch order.

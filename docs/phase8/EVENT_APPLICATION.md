# Phase 8 — Recovered `best.s.a(...)` event application boundary

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

This note records the reachable event-application method `best.s.a(int,int,s,c0,o,o,int,int)` recovered from Java and confirmed against SMALI. The modern rule is intentionally an operation plan: it preserves the legacy write/call order without inventing a new mutable match runtime.

## Event materialization and side identity

The club is matched by object identity against the match's two club fields. The first club maps to legacy side `0`; the second maps to side `1`. If neither identity matches, legacy still constructs `best.l` with side `0`, while both active-list references remain null.

The event is populated in this order at the structural boundary:

1. legacy type;
2. original primary player;
3. subtype handling;
4. optional secondary player;
5. minute;
6. period;
7. club;
8. append to the match event list.

The apparent parameter order is easy to invert: the seventh Java/SMALI argument is the period and the eighth is the minute; `best.l.q(...)` stores the minute while `best.l.t(...)` stores the period.

## Subtype `2` overwrite/fallback quirk

When the requested subtype is exactly `2`, legacy calls `best.s.V(oppositeActive)`.

- If selection succeeds, the player stored in the event is overwritten with that opposite-side selection and subtype remains `2`.
- If selection returns null, subtype is changed to `1` and the original primary remains stored.
- Later player/stat/injury/removal/substitution effects still target the original primary argument, not the player that may have replaced it inside the event.

This split identity is deliberate parity and must not be normalized.

## Disciplinary stat mutations

After the event has already been appended:

- type `2` calls the player's recovered stat mutation `r0().m()`;
- type `4` calls `r0().n()`;
- type `3` calls `m()` and then `n()` in that order.

The modern rule keeps neutral operation names for these counters; their event meanings are already characterized elsewhere, but the internal `components.n2` field names remain obfuscated.

## Types `3` and `4`: removal and replacement routing

For a recognized club side, the original primary is removed from that side's active list for type `3` or `4` regardless of the club-mode flag.

Only after that removal, legacy may call:

`p1(side, true, match, originalPrimary, period, minute, false)`

The call requires all of:

- original primary position index `<= 13`;
- club mode flag false;
- recovered substitutions-remaining counter for that side greater than zero.

The second boolean being `true` is significant: `p1(...)` first chooses another active player through the recovered `W(...)` path rather than necessarily using the removed player as its `outgoing`. The final boolean is `false`, so the later legacy-l0 compatibility guard is not enabled.

## Type `5`: injury then removal then primary replacement routing

Type `5` preserves a different order:

1. if original primary is non-null, invoke the recovered player injury routine `best.o.m(c0)`;
2. if the club side was recognized and the club-mode flag is false, remove the original primary from the active list;
3. if the club object itself is non-null, the side-indexed substitutions counter is positive, and the club-mode flag is false, call:

`p1(side, false, match, originalPrimary, period, minute, true)`

This call keeps the injured original player as the outgoing candidate and enables the recovered legacy-l0 compatibility guard.

A subtle legacy quirk is preserved: for a non-null club object that is neither match club identity, side remains `0`; active-list removal is skipped because no list was resolved, but the final injury substitution condition can still call `p1` using side `0`.

## Operation-plan boundary

`LegacyMatchEventApplicationRules` returns the exact ordered operation plan and a recovered substitution request. `execute(...)` applies callbacks strictly in that order so a future mutable runtime does not have to re-encode the branching.

The characterization suite locks identity semantics, subtype-2 overwrite/fallback, minute/period placement, event-before-effects ordering, stat mutation order, removal thresholds, club-mode behavior, substitutions-remaining behavior, injury order, the unrecognized-club side-zero quirk, and the fact that later effects continue to reference the original primary even after event-primary replacement.

No football data, competition rule, Room schema, or user-facing feature is introduced by this boundary.

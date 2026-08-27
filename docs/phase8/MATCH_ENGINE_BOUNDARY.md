# Phase 8 — Legacy match-engine boundary

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

## Recovered boundary

`ActivityJogo.v()` is incomplete in decompiled Java. SMALI proves it controls the per-half clock/UI and delegates normal match mutation to the `best.s` match object. `ActivityJogo.s()` switches to half 2 and calls `best.s.j(2, 0)`.

`best.s.R0()` iterates queued matches and calls `best.s.Q0()`, which is a proven automatic-simulation entry point. The Java decompiler stubs `Q0()`, so SMALI is authoritative.

## Exact `Q0()` automatic-flow evidence

The SMALI proves this order:

1. create and attach `components.r3`;
2. apply the legacy pre-simulation conditions;
3. when the simulation branch is entered, draw **both** added-time values consecutively before any minute loop:
   - first half: `nextInt(3)` → `0..2`;
   - second half: `nextInt(5) + 1` → `1..5`;
4. simulate half 1 from minute index `0` while `minute < 45 + firstHalfAdded`;
5. each minute calls `best.s.k(match, 1, minute)` and then reads `components.r3.K()`; a returned event is stamped with its minute and half before being appended to `J()`;
6. call `j(2, 0)`;
7. simulate half 2 from minute index `0` while `minute < 45 + secondHalfAdded`, with the same event-stamping pattern and half `2`;
8. after simulation, the `Z && a0 && P0()` gate can call `o()`;
9. the legacy per-club flags are cleared with `E1(false)` on both sides.

An earlier branch characterization placed the second added-time draw after first-half simulation. Direct SMALI inspection disproved that ordering. `LegacyMatchScheduleRules` now preserves the actual pre-draw sequence `[bound 3, bound 5]` before any per-minute or halftime RNG is consumed.

The minute-loop boundary is also no longer open evidence: each half executes exactly `45 + addedMinutes` calls to `best.s.k(...)`, using indexes `0 until (45 + addedMinutes)`.

## Direct per-minute RNG in `best.s.k`

The proven direct chain is:

1. `nextInt(100) > 55` selects one side;
2. `nextInt(primaryBound) == 1` reaches legacy helper `c`;
3. only on miss, `nextInt(secondaryBound) == 1` reaches helper `d`;
4. only on miss, `nextInt(tertiaryBound) == 1` reaches event type 5;
5. only after all misses, second-half minute 5+ reaches `j`.

| Half | Segment | primary | secondary | tertiary |
|---|---:|---:|---:|---:|
| 1 | 0–14 | 70 | 1200 | 2000 |
| 1 | 15–29 | 40 | 900 | 1500 |
| 1 | 30+ | 30 | 800 | 1100 |
| 2 | 0–14 | 45 | 800 | 2000 |
| 2 | 15–29 | 40 | 700 | 1500 |
| 2 | 30+ | 30 | 550 | 1100 |

Tactic offsets are `[30, 10, 0]`; legacy indexes `>=3` map to bucket 0.

Counter order is exact: if `O > 5`, primary doubles; an `else if O > 10` branch is therefore unreachable and must remain so; then `P >= 2` replaces primary with `secondary*2`; finally `Q >= 1` replaces primary with `tertiary*5`.

Every seventh minute invokes player-state refresh before these event gates; the recovered helper contains no direct RNG.

## `P0()` reachability and resolution order

`P0()` is reachable from `Q0()`, `ActivityJogo` and `best.g0`; it is therefore a valid production reconstruction target.

The method contains no RNG. Its exact decision order is now represented by `LegacyMatchP0Rules` with neutral side names:

1. compare the first stored pair and count a result for either legacy side;
2. when the second stored state is enabled, compare its pair and increment the corresponding count, while also computing two aggregate totals;
3. resolve by result-count difference first;
4. only if still unresolved, compare the aggregate totals;
5. only if still unresolved and both legacy flags permit it, compare the retained second-state value against the first-state counterpart;
6. `P0()` returns `true` only when all applicable criteria still leave the state unresolved.

No sporting label is assigned to the third criterion until further evidence proves its exact user-facing meaning.

## Reachability boundary for `p` / `n0`

Corpus-wide SMALI search finds zero invocations of `best.s.p(Lbest/s;Z)[I`. `n0(Lbest/s;)I` has one caller, and that caller is `p` itself. Under `AGENTS.md`, method existence alone does not justify adding a gameplay path, so these methods remain evidence-only unless later reachability evidence appears.

## Remaining downstream work

`j`, `r`, `r0`, `P0`, the Q0 added-time order, Q0 minute boundaries and halftime transition are characterized on this branch.

The next reachable boundary is event production/materialization through `components.r3.K()` and `best.l`, followed by the proven score/player/club mutations and post-match side effects. Neutral names remain mandatory wherever the sporting semantics are not yet proven.

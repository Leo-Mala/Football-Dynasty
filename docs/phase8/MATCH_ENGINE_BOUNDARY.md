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

## Reachable `best.l` goal materialization

The automatic and accompanied match paths both reach `components.r3.K()`. Its successful event branch reaches `components.r3.b()/c()`, which creates `best.l` and calls `components.r3.f(...)`.

At that site the event type is explicitly set to `1`. `best.l` rendering and the bundled strings prove type `1` is a goal event. The direct subtype RNG is exactly one `nextInt(1000)` draw:

| Draw | Legacy subtype | Proven UI meaning |
|---:|---:|---|
| `0..899` | 1 | normal goal |
| `900..949` | 3 | penalty goal |
| `950..979` | 4 | free-kick goal |
| `980..989` | 2 | own goal |
| `990..994` | 5 | corner/olímpico goal |
| `995..999` | 1 | normal goal |

`LegacyMatchGoalEventRules` preserves this direct draw with `RandomSource`. A proven quirk is also retained: subtype `5` falls back to subtype `1` when the currently selected player's legacy `l0` value is zero; a null player does not trigger that fallback at this point.

Downstream author/secondary-player replacement is still handled by additional `r3.f` helpers and remains a separate characterization boundary; the direct subtype rule does not invent those selections.

## Reachable injury application (`best.o.m`)

Event type `5` reaches the player injury routine through `best.s.a(...)`. Decompiled Java and SMALI agree on the observable control flow represented by `LegacyMatchInjuryRules`.

The routine always consumes three RNG draws in this exact order:

1. `nextInt(14)` for the base duration;
2. `nextInt(20) + 5` for the older-age duration component;
3. `nextInt(100)` for the rare severity extension.

The bound-20 draw is consumed even when age `<= 20`, where its value does not participate in the duration. Energy modifies duration with strict thresholds: `<10 -> +5`, `<50 -> +1`, otherwise `+0`.

Age buckets are preserved exactly:

- `<=20`: base only;
- `21..25`: energy modifier + base + 1;
- `26..30`: energy modifier + base + 2;
- `31..35`: energy modifier + base + 3;
- `36..40`: energy modifier + base + older-age component;
- `41..45`: the same older-age formula;
- `>45`: energy modifier + base + 10 + older-age component.

For age `>=35`, the legacy routine decreases the proven skill value by 5. The clamp is intentionally peculiar: only a result `<0` is replaced with `1`; exactly zero remains zero.

The severity draw then adds `+70` only at value `1`; values `0`, `2`, `3` add `+40`; values `4..9` add `+20`; values `>=10` add nothing. The legacy injury-until timestamp is updated only when the final duration is positive. The pure modern rule exposes `durationDays`, updated skill, whether an injury-until value should be written, and event type `INJURY`; the timestamp write and surrounding match-state mutation remain an integration boundary rather than performing time/I/O work in the pure rule.

The same caller also proves the next reachable state transition: after `player.m(club)`, the injured player can be removed from the active match list and, when legacy conditions allow, the substitution path `best.s.p1(...) -> best.s.o1(...)` is entered.

## Reachable substitution selection and mutation (`best.s.p1` / `best.s.o1`)

`LegacyMatchSubstitutionRules` now represents the proven selection and mutation plan without introducing a new substitution mechanic.

When `p1` is asked to select the outgoing player automatically, it calls legacy `W(...)` in this exact fallback order:

1. active players with `g0` in `18..25`;
2. only if none, `14..17`;
3. only if still none **and the original event player has `g0 == 1`**, `2..25`.

Each `W` call filters, shuffles the eligible copy and returns its first item; the modern reconstruction reuses the explicit `RandomSource` shuffle. If automatic selection is disabled, the original event player itself is the outgoing player and no `W` RNG is consumed.

The incoming player is not selected from the outgoing player's position. `p1` deliberately passes the **original event player's** `g0` and `l0` into `components.y3.e(...)`. That helper contains no RNG: it performs an ordered first-match scan using the legacy `j0.Z1` and `j0.c2` tables and the candidate `l0/f0/R` fields. Its intermediate values are intentionally mutable across the nested scan loops, matching bytecode rather than replacing the search with a simplified position lookup.

When the final compatibility flag is enabled, `p1` aborts if the chosen outgoing player has nonzero `l0` while the selected incoming player has `l0 == 0`.

`o1` then performs the valid-side mutation in this order:

1. decrement that side's remaining substitution counter;
2. set incoming `g0` to outgoing `g0`;
3. if the original player's `g0 > 0`, overwrite incoming `g0` with that original value;
4. remove outgoing from the active list;
5. add incoming to the active list;
6. add incoming to the side's used/replacement list;
7. mark incoming with the legacy selected/used flag;
8. remove incoming from the bench list;
9. emit event type `6` (`SUBSTITUTION`) with outgoing and incoming players.

A final integer argument passed by `p1` as `-1` or `5` is present in the `o1` signature but is never read by the bytecode. It remains explicitly documented as a dead legacy parameter rather than being assigned invented semantics.

## Reachability boundary for `p` / `n0`

Corpus-wide SMALI search finds zero invocations of `best.s.p(Lbest/s;Z)[I`. `n0(Lbest/s;)I` has one caller, and that caller is `p` itself. Under `AGENTS.md`, method existence alone does not justify adding a gameplay path, so these methods remain evidence-only unless later reachability evidence appears.

## Remaining downstream work

`j`, `r`, `r0`, `P0`, the Q0 added-time order, Q0 minute boundaries, halftime transition, direct goal-subtype draw, `best.l` event-type/score mapping, disciplinary routing, injury duration/RNG and substitution selection/mutation order are characterized on this branch.

The next reachable boundaries are the remaining author/secondary-player selections inside `components.r3`, the match stat counters, event-driven player/club state application, and post-match side effects. Neutral names remain mandatory wherever the sporting semantics are not yet proven.

# Phase 8 — Legacy match-engine boundary

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

## Recovered boundary

`ActivityJogo.v()` is incomplete in decompiled Java. SMALI proves it controls the per-half clock/UI and delegates normal match mutation to the `best.s` match object. `ActivityJogo.s()` switches to half 2 and calls `best.s.j(2, 0)`.

`best.s.R0()` iterates queued matches and calls `best.s.Q0()`, which is a proven automatic-simulation entry point. SMALI shows `Q0()` drawing first-half added time with `nextInt(3)`, second-half added time with `nextInt(5)+1`, looping through both halves with `best.s.k(match, half, minute)`, and calling `j(2, 0)` at halftime.

The two added-time draws are now materialized as separate deterministic rules in `LegacyMatchScheduleRules`: first half `0..2` from bound `3`, second half `1..5` from bound `5` plus one. They deliberately remain separate because the per-minute simulation consumes RNG between those sites; pre-drawing both would change legacy draw order. Full Q0 minute-loop boundaries remain an evidence gate and are not inferred here.

The proven Q0 landmark order is also materialized without inventing those minute boundaries: first-half added-time draw -> first-half simulation callback -> halftime transition callback -> second-half added-time draw -> second-half simulation callback. The callbacks intentionally own the still-unrecovered minute ranges. A characterization test consumes RNG inside the first-half callback and verifies that this draw remains between the legacy bound-3 and bound-5 draws.

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

## Downstream RNG still open

Direct triggers can reach selectors `S/T/U/V/W`. Their direct bounds are respectively 100, 500, 200 and 1000 before positional filtering; `W` uses `Collections.shuffle`. Additional incomplete Java methods (`j`, `r`, `r0`, `p`, `n0`, `P0`, `Q0`) require SMALI reconstruction.

`j`, `r` and `r0` now have characterized modern structural rules on this branch; `Q0` has its added-time draws and proven landmark ordering materialized, but not its concrete minute-loop boundaries. The remaining Q0 loop boundaries and the still-open `p`, `n0` and `P0` paths must be recovered from SMALI before production semantics are extended.

The first modern rule therefore returns neutral labels `LEGACY_C`, `LEGACY_D`, `LEGACY_TYPE_5` and `SECOND_HALF_J`; it does not assign unsupported sporting semantics.

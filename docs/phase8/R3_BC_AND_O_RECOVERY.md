# Phase 8 — `r3.b()/c()` and automatic `best.s.o()` recovery

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

This recovery uses the official Java + SMALI corpus. It adds no external football data and assigns no new sporting meaning to obfuscated state.

## Reachable automatic `best.s.o()`

The `Q0()` post-loop gate can reach `best.s.o()`. Java and SMALI agree on the exact observable behavior:

1. consume `nextInt(7) + 2` as the first value;
2. consume another `nextInt(7) + 2` as the comparison value;
3. when `first >= comparison`, select legacy side `E`, write `d0[0] = first`, `d0[1] = first - 1`;
4. otherwise select legacy side `F`, write `d0[0] = first`, `d0[1] = first + 1`.

Equality therefore belongs to the first branch. There are exactly two bound-7 RNG draws and no retry/third draw.

`LegacyMatchPostGateORules` represents that pure rule with the shared injected `RandomSource`. `LegacyMatchAutomaticResolutionRules` composes it after the already-characterized automatic two-half flow and `Z && a0 && P0()` routing. If the gate does not request `o()`, no bound-7 draw is consumed. The final two-club flag clear remains an unapplied operation plan and is not reordered around `o()`.

## Reachable `components.r3.b()`

Java and SMALI agree on the recovered branch. The rule stores the opposite-side `u()` result in legacy `g`, derives three multipliers, selects a three-element table, consumes exactly one weighted `nextDouble()`, and then applies the selected counter/event route.

The base tables are:

- `B0 = [5.5, 35.55, 15.0]`;
- `C0 = [4.5, 40.55, 15.0]`;
- `D0 = [3.0, 40.55, 15.0]`;
- `E0 = [0.5, 40.55, 15.0]`.

Table selection uses recovered `h[current]`: `>=6 -> E0`, `>=5 -> D0`, `>=3 -> C0`, otherwise `B0`. A later override sets `D0` whenever `h[current] >= 2` and `opposite.p0 - current.p0 >= 2`.

`r3.e(a,b)` divides `(a-b)` by `10` when the recovered global `J >= 5`, otherwise by `8`.

Observable quirks preserved by `LegacyMatchR3EventRoutingRules`:

- when stored `g == 0` and the opposite club `Q0` flag is true, the second multiplier is replaced by the integer-rounded value of `second * 0.2`;
- for non-neutral side `0`, `second += 0.1` and then the third multiplier is **overwritten** as `second + 0.1`;
- for non-neutral side `1`, `second -= 0.1` and the third multiplier is **overwritten** as `second - 0.1`;
- second and third are independently clamped to at least `0.2`;
- the legacy `i[current]` increment happens **after** the weighted draw in `b()`.

## Reachable `components.r3.c()`

The recovered base tables are:

- `A = [7.8, 45.78, 53.52]` for minute `<30`;
- `B = [10.8, 43.78, 53.52]` for minute `30..69`;
- `C = [13.2, 36.78, 44.52]` for minute `>=70`.

Recovered `h[current]` overrides are `3 -> D`, `4 -> E`, `5 -> F`, `>=6 -> G`, where:

- `D = [7.8, 37.78, 45.52]`;
- `E = [5.8, 37.78, 45.52]`;
- `F = [2.8, 37.78, 45.52]`;
- `G = [1.8, 45.78, 53.52]`.

A later score/state override sets `H = [1.0, 55.78, 63.52]` whenever `h[current] >= 3` and `opposite.p0 - current.p0 >= 2`.

`c()` uses the already-characterized `r3.d(...)` difference helper. The legacy `>=9 -> 12` divisor branch remains unreachable because the preceding `>=5 -> 11` branch wins first.

Additional exact ordering quirks:

- non-neutral side `0` subtracts `0.1` from the second multiplier and sets the third to `0.9`;
- non-neutral side `1` subtracts `0.1` from the first multiplier;
- after those adjustments, stored legacy `g == 0` overwrites the first multiplier with `20.0`;
- unlike `b()`, `c()` increments legacy `i[current]` **before** the weighted `nextDouble()`.

## Shared selected-index outcomes

Both methods use the shared strict weighted helper (`target < cumulative`) and then route the selected index identically:

- index `0`: materialize the current-side goal path and increment match `y[current]`;
- index `1`: increment `y[current]`, then increment the primary player's recovered `r0().p()` counter only when a primary player exists;
- index `2`: increment match `z[current]`;
- any other index: no selected-index mutation beyond the already-required `i[current]` increment.

The pure modern rule returns these mutations as a plan instead of inventing a mutable match runtime. It also exposes whether the `i[current]` increment belongs before or after the weighted draw so later state integration cannot silently reorder RNG relative to mutation.

## Verification added in this block

The block adds three new suites:

- `LegacyMatchPostGateORulesTest` — 4 tests;
- `LegacyMatchAutomaticResolutionRulesTest` — 4 tests;
- `LegacyMatchR3EventRoutingRulesTest` — 15 tests.

Together they characterize 23 new cases before the next full Phase 8 certification. The remaining integration boundary is application of these returned plans to a proven modern match-state representation and any still-unrecovered post-match effects; that state must not be invented merely to make the rules executable.

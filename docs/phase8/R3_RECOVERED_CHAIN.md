# Phase 8 — Recovered `components.r3` decision chain

Source of truth: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

This note consolidates behavior already recovered from the reachable `components.r3` path and represented by the Phase 8 characterization rules. It does not assign new sporting meaning to obfuscated fields and does not add a gameplay path merely because a legacy method exists.

## Reachability

The automatic and accompanied match flows reach `components.r3.K()`. The recovered chain feeding that method includes the weighted helper used by `A/B`, player transform `g(best.o)`, aggregate helpers `y/u/z`, decisions `J/I`, the `K()` state advance, `a(side)`, and the reachable secondary selector `i(best.o)`.

## Weighted index helper (`A/B`)

`LegacyMatchWeightedChoiceRules` preserves the recovered helper exactly at the observable boundary:

1. multiply each base weight by its matching modifier;
2. sum those products;
3. consume one `nextDouble()` and calculate `target = draw * total`;
4. accumulate products in index order;
5. return the first index where `target < cumulative`;
6. if no index satisfies the strict comparison, return `weights.size`.

The strict comparison is significant. With equal normalized halves, an exact draw of `0.5` crosses to the second bucket rather than remaining in the first.

## Player numeric transform (`g(best.o)`)

`LegacyMatchR3PlayerValueRules` preserves the exact rounding order recovered for the player value used by the `r3` metrics. The legacy integer value is modified step-by-step with `Math.round`, not with one combined floating-point expression:

- the first recovered flag applies `* 0.7`;
- the next branch applies either `1.02/1.05` or, in its `else if`, `1.05/1.10`, depending on the recovered club state;
- the recovered club boolean can then apply another `* 1.05`;
- only after those integer roundings is the value returned divided by `10.0`.

Neutral legacy field names remain intentional until their user-facing semantics are proven.

## Aggregate metrics (`y/u/z`)

`LegacyMatchR3MetricRules` preserves three reachable aggregations, including their fixed divisors and low-population fallbacks:

- `y`: considers at most the first five active entries whose recovered position index is `10..17`; a club-state bucket contributes `0.00`, `0.04`, or `0.08`; the sum is always divided by `5.0`; fewer than three matching entries returns `0.01`;
- `u`: considers at most the first five entries with index `2..9`; the sum is always divided by `5.0`; fewer than three matching entries returns `0.1`;
- `z`: considers at most the first three entries with index `19..25`; the sum is always divided by `3.0`; no matching entry returns `0.0`.

The fixed divisors are preserved even when fewer than the maximum number of entries participate. Replacing them with division by the actual count would change legacy behavior.

## Difference helper and `J()`

`LegacyMatchR3DecisionRules.difference()` preserves the original conditional order. A recovered global integer uses divisor `11.0` when it is `>= 5`; a later `>= 9 -> 12.0` branch is therefore unreachable and deliberately remains characterized as dead legacy code. Values below five use divisor `8.0`.

`J()` derives two modifiers from opposite `y` differences plus `1.0`. For recovered side `0`, a non-neutral legacy state adds `0.3` to the first modifier. Each modifier is then independently clamped to at least `0.2`.

The resulting pair is passed to the recovered weighted helper with base weights `55.0/45.0`. Index `0` returns the current side and applies the characterized current/opposite mutation order; index `1` returns the opposite side and applies the mirrored recovered mutation order. Because selection uses the strict weighted comparison, the exact normalized `55%` boundary enters index `1`.

## `I()`

`I()` compares current-side `z` against opposite-side `u` through the same difference helper and starts from base weights `50.0/50.0`.

Recovered ordering details are preserved:

- if opposite `u` is exactly zero, the second modifier is reset to `0.1`;
- the same side-0/non-neutral `+0.3` adjustment is applied to the first modifier;
- if current `z` is exactly zero, the first modifier is then reset to `0.1`, after that possible bonus;
- both modifiers are finally clamped to `0.2` minimum;
- the opposite `u` value is retained in the recovered state represented by the characterization rule.

Index `0` and index `1` route to their respective recovered counter mutations. With equal modifiers, an exact `0.5` draw selects index `1` because the comparison is strict.

## `K()` state advance

`LegacyMatchR3AdvanceRules` represents the reachable direct control flow of `K()` without inventing labels for the obfuscated counters:

1. resolve `J()` for the current side;
2. when `J()` returns the current side, resolve `I()`;
3. when that `I()` result is zero, increment the recovered current-side `W` counter and materialize the reachable goal-event branch; this path consumes no direct `nextInt(100)` in `K()`;
4. otherwise consume one direct `nextInt(100)`; values `< 50` mutate the recovered opposite-side `Q0` counter and values `>= 50` mutate the recovered current-side `A0` counter;
5. the same direct `nextInt(100)` split is used when `J()` did not return the current side;
6. increment the recovered tick and toggle side `0 <-> 1` after the branch.

The RNG consumed internally by `J()`/`I()` remains separate from this direct `K()` bound-100 draw and retains its original order through the injected `RandomSource`.

## `a(side)` recovered percentage state

`LegacyMatchR3ApplyARules` preserves the reachable update performed by `a(side)`: increment the chosen entry in the recovered two-element counter, recompute the two percentages from the new total, convert both operands to `float` before division, multiply by `100`, and use `Math.round(float)`. The float conversion is intentional parity with SMALI.

## Reachable secondary selector (`i(best.o)`)

`LegacyMatchGoalSecondarySelectionRules` represents the recovered secondary-player selector reached downstream of goal materialization.

The selector first consumes `nextInt(100)`. Values `81..99` return null immediately. Accepted values `0..80` continue to a weighted `nextDouble()` draw. Eligible active entries exclude the primary object by identity and require a recovered position index inside the characterized base-weight table.

The weight calculation preserves the legacy ordered `if/else if` trait branches and the recovered club/`l0` bonuses. It also preserves an observable SMALI quirk: the `target <= cumulative` comparison occurs outside the eligibility block. Consequently, an exact zero target can return an excluded primary entry, and a zero-total non-empty invalid list can return its first entry. These cases are regression-tested rather than normalized away.

## Current reconstruction boundary

The items above are characterized and individually required by the Phase 8 CI gate. They should not be reimplemented through cleaner formulas that alter rounding, comparison, branch, counter, or RNG order.

Still open for Phase 8 are the remaining proven interactions inside goal `b()/c()/f(...)` that are not yet represented by the current materialization rules, the user-facing meaning/integration of recovered match-stat counters where evidence is incomplete, event-driven player/club state application, production orchestration of the pure rules into the modern match runtime, and post-match side effects. New implementation must remain gated by Java↔SMALI reachability evidence; absence of that evidence is not permission to infer gameplay.
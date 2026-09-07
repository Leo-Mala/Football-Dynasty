# Fase 15 — `best.o.s()` growth accumulation from official SMALI

Status: **RATE + RETAINED `N` ACCUMULATION IMPLEMENTED / LATER CAP SECTION STILL OPEN**

Corpus authority: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

The raw archive was re-opened and SHA-256 verified before this characterization. Executable `smali/best/o.smali` is authoritative over the decompiled Java.

## Exact boundary frozen

`best.o.e()` reaches private `best.o.s()` only after the existing non-null club guard. The first half of `s()` computes one `double` increment and adds it directly to retained field `N`.

The SMALI proves the base rate matrix from raw club band and raw player `e`:

| effective club band | `e <` thresholds | rates |
|---|---|---|
| `>=19` | 20 / 23 / 29 / else | .16 / .12 / .10 / .08 |
| `>=15` | 18 / 21 / 29 / else | .12 / .10 / .08 / .06 |
| `>=11` | 18 / 21 / 29 / else | .10 / .08 / .06 / .04 |
| `<11` | 18 / 21 / 29 / else | .08 / .06 / .04 / .02 |

For `R0==false`, the executable rewrites the effective band from `p0` to 20 (`p0>=4`), 18 (`p0==3`) or 12 (otherwise) and adds `.03` after the later club-type adjustment.

The exact modifier order is then:

1. `M==true` adds `.04`;
2. raw player `j`: 30..40 `-.02`, 41..50 `-.03`, 51..70 `-.04`, 71..100 `-.05`;
3. when `d0>0`: `<50 -> -.05`, `<70 -> -.02`; then raw `m>=9 -> +.07`, else `m>=7 -> +.05`;
4. `W0==true -> +.02`, otherwise `O0==true -> +.01`;
5. raw club `J/j0` adjustment;
6. non-`R0` `.03` bonus;
7. strictly negative result becomes `.01`; exact zero remains zero;
8. result is added to the retained `N` with ordinary JVM `double` addition.

Raw club `J` adjustment is frozen exactly, including the executable quirk in the `J==1` branch. The SMALI tests the same `j0` against mutually incompatible constants sequentially, so its apparent exemption conjunction cannot succeed; effective behavior is simply `rate > .06 -> rate-.02`.

`LegacyAnnualSeniorGrowthAccumulationRules.calculate(...)` freezes the rate. `accumulate(currentN, ...)` now freezes the immediately following retained-state mutation `N = N + increment` without rounding, clamping, resetting, or consuming RNG. Those later effects remain owned by the already-separated cap/finalization boundaries.

## Tests

`LegacyAnnualSeniorGrowthAccumulationRulesTest` freezes:

- all base band boundaries;
- non-`R0` band rewrite + `.03`;
- modifier ordering and `W0` precedence over `O0`;
- `J==0` special raw-id branches;
- executable `J==1` impossible-conjunction quirk;
- strict `> .06` threshold;
- negative-to-.01 guard versus exact zero;
- retained fractional `N` accumulation without rounding;
- exact-zero increment preserving the previous `N`.

## Persistence

No schema change in this checkpoint. The executable mutation strengthens the existing conclusion that `N: double` is retained runtime state and cannot be reconstructed from final overall alone. V15 remains blocked until the middle cap/max section and the aggregate senior annual persistent-state map are closed together.

Room remains V14.

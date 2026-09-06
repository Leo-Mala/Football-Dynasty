# Fase 15 — `best.o.s()` target/cap selection from official SMALI

Status: **IMPLEMENTED_AND_TESTED / EXACT-SHA CERTIFICATION PENDING**

Corpus authority: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

The raw archive was materialized again and its SHA-256 revalidated before this implementation. Executable `smali/best/o.smali` is authoritative.

## Reachable position in `best.o.s()`

This boundary runs immediately after the already-frozen `N = N + increment` mutation and immediately before the already-frozen high-`d0` RNG adjustment/finalization block.

The executable first derives a baseline target from raw club state.

When `R0()==true`, raw `O()` maps to:

- `0` or `4` → `30`;
- `1` → `100`;
- `2` → `60`;
- `3` → `40`;
- anything else → `20`.

When `R0()==false`, raw `p0()` maps to:

- `5` or `4` → `100`;
- `3` → `75`;
- `2` → `40`;
- `1` → `30`;
- anything else → `20`.

The executable then derives a second club-specific cap from raw `J()/j0()`:

| `J()` | `j0()` set | cap |
|---|---|---:|
| `0` | `3,72,104,65,97` | 95 |
| `0` | `154,85` | 90 |
| `0` | `21,162` | 80 |
| `0` | other | 70 |
| `1` | `29,11` | 90 |
| `1` | `195,46,42` | 80 |
| `1` | `151,150` | 70 |
| `1` | other | 60 |
| `2` | `10,129,57` | 75 |
| `2` | `141,169,190` | 70 |
| `2` | other | 60 |
| `3` | `107,49` | 75 |
| `3` | `98,9,59,43` | 70 |
| `3` | other | 60 |
| `4` | `131` | 80 |
| `4` | `68` | 70 |
| `4` | `51` | 65 |
| `4` | other | 55 |
| `5` | `143` | 60 |
| `5` | other | 45 |
| other | any | 100 |

The target handed to the later high-`d0` block is exactly `min(baselineTarget, clubSpecificCap)`.

No RNG, persistence write, rounding, clamp beyond that explicit minimum, or additional sporting interpretation exists in this boundary.

## Modern implementation

`LegacyAnnualSeniorGrowthTargetRules.calculate(...)` preserves the raw fields and exposes:

- `baselineTarget`;
- `clubSpecificCap`;
- `cappedTarget`.

`LegacyAnnualSeniorGrowthTargetRulesTest` covers both baseline matrices, all special `J/j0` groups/defaults, and both sides of the final minimum.

## Composition status

The executable computational sections of growth are now separately frozen as:

1. rate + retained `N` accumulation → `LegacyAnnualSeniorGrowthAccumulationRules`;
2. target/cap selection → `LegacyAnnualSeniorGrowthTargetRules`;
3. high-`d0` RNG adjustment + final mutation → `LegacyAnnualSeniorGrowthFinalizationRules` / `LegacyAnnualRandomRules`.

This closes the previously-open middle target/cap computation itself. End-to-end annual senior growth is still not promoted to fully implemented until the retained senior state owners (`M`, `N`, raw payroll `n`, and adjacent `j0/d-W0` lifecycle where applicable) are mapped durably and the boundaries are composed through the real runtime.

Room remains V14; no schema change is justified by this pure computation checkpoint.

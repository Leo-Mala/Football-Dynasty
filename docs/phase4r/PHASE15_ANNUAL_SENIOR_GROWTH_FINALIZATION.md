# Phase 15 — annual senior growth finalization (`best.o.s()`)

## Authority

Pinned official corpus: `Brasfoot.apk_Decompiler.com.zip`

- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`
- package: `com.brasfoot.v2020`
- versionCode: `202632`
- executable authority for this method: `smali/best/o.smali`

The Java decompiler does not provide an executable body for `best.o.s()`, so SMALI is authoritative.

## Frozen reachable block

This checkpoint characterizes the final mutation block of `best.o.s()` after the preceding club-specific cap has been computed and after the method has already accumulated its fractional growth contribution into persistent `best.o.N`.

SMALI order:

1. take the previously computed cap;
2. when `d0 >= 60`, consume exactly one `new Random().nextInt(5)` draw;
3. for `m=7/8/9/10`, add respectively `5/15/25/30 + draw` to that existing cap; for other `m`, the draw is still consumed but the cap is unchanged;
4. clamp the adjusted cap to `100`;
5. evaluate strict `N > 1.0`;
6. only when current `j < 100` does the growth/cap branch execute;
7. if `j < effectiveCap`, increment `j` exactly once and subtract exactly `1.0` from `N`;
8. otherwise, set `N = 1.0` exactly;
9. finally clamp `j` to at most `100`.

The `N = 1.0` cap-block behavior is significant: excess fractional accumulation is not preserved when `N > 1.0`, `j < 100`, and the effective cap prevents growth. Conversely, `j >= 100` bypasses that branch and therefore leaves `N` unchanged.

## Modern implementation

`LegacyAnnualSeniorGrowthFinalizationRules` freezes this block as a pure rule and composes the already-characterized high-`d0` RNG boundary through `LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment(...)`.

Regression coverage proves:

- strict `N > 1.0` rather than `>=`;
- at most one overall increment per invocation;
- subtraction of exactly one accumulated point after growth;
- cap-block collapse to exactly `N = 1.0`;
- `j == 100` preserving `N`;
- high-`d0` cap adjustment preceding the cap comparison;
- one RNG draw at `d0 >= 60`, including for `m` values with no bonus;
- zero draws below that boundary.

## Classification

- final `best.o.s()` growth/cap mutation block: `IMPLEMENTED_AND_TESTED`
- high-`d0` RNG sub-branch: `IMPLEMENTED_AND_TESTED`
- complete `best.o.s()` preceding growth-weight/cap derivation: `PARTIALLY_IMPLEMENTED`
- durable legacy `M/N` mapping: still an open persistence blocker; no schema change is authorized by this checkpoint alone.

No sporting semantics, defaults, backfills, or destructive migration are introduced here.

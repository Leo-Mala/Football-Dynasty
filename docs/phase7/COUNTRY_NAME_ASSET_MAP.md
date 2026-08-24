# Phase 7 — Country/name asset map

Official corpus: `Brasfoot.apk_Decompiler.com.zip`

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Mapping

`best.u.c(int d)` resolves the name asset code through the sorted `best.g` country list. `best.g.a(d)` searches by the original country id, so the effective mapping remains the `best.y` entry `P<d> -> code`.

The official `best.y` initializer contains exactly 221 entries, `P0` through `P220`, with 221 unique three-character asset codes and no index gaps.

Examples locked by regression tests include:
- `P0 -> AFG`
- `P11 -> ARG`
- `P29 -> BRA`
- `P44 -> CPR`
- `P45 -> TML`
- `P47 -> CNG`
- `P63 -> ELQ`
- `P64 -> ESV`
- `P81 -> GUI`
- `P101 -> IRN`
- `P110 -> KOS`
- `P115 -> LBN`
- `P143 -> NOZ`
- `P145 -> PGA`
- `P153 -> PRI`
- `P192 -> TUR`
- `P200 -> ZAM`
- `P216 -> GIB`
- `P217 -> GDA`
- `P218 -> GMA`
- `P219 -> MTI`
- `P220 -> GFR`

Reproducible mapping fingerprint definition: concatenate the 221 rows in ascending index order as `index<TAB>displayName<TAB>code<LF>`.

SHA-256: `288adda3a9630d946699da2a13d905e0da9260a3fac3578469c7b63b7bd0c1fe`

`LegacyCountryAssetCodes` stores only the code lookup needed by the modern asset boundary; it does not introduce or update sporting facts.

## Asset completeness

The corpus contains:
- 221 `resources/assets/names/*.txt` files;
- 221 `resources/assets/surnames/*.txt` files;
- 442 files total;
- 495,785 uncompressed bytes.

Every one of the 221 `best.y` codes has exactly one matching names file and one matching surnames file. There are no missing or extra country-code files in those two directories.

Reproducible physical-manifest definition: for all 442 files sorted by full path, concatenate `path<TAB>sha256(file bytes)<TAB>size<LF>`.

Physical manifest SHA-256: `4b8c6db9b6343221cb6e945aef7ba0d7dc081c4744e30bb1f9a0eba3c57e444b`

## Exact legacy filtering

`best.u.b()` keeps a line only when it is non-empty, does not contain `NumberFormat.f12825o`, and does not match `.*\d+.*`. The official `NumberFormat.f12825o` constant is `.`. The legacy method does not trim accepted lines before adding them.

After that exact filter every one of the 442 files remains usable; the smallest filtered file contains 23 entries.

Reproducible semantic-manifest definition: for each file sorted by full path, filter with the rule above, hash the accepted lines joined with `LF` plus terminal `LF`, then concatenate `path<TAB>inner_sha256<TAB>filtered_count<LF>`.

Filtered semantic manifest SHA-256: `d4cb2a6fd770a7bcdb850ee63b31d6442b66a3da70611f7a100e264b1f2c625f`

## Consequence

For the official corpus, `best.u.c(d)` has a complete asset-backed source for every valid `d`. The `best.o.X0(d)` fallback does not need to be replaced with fabricated generic names.

The runtime integration can preserve the original `names/<CODE>.txt` and `surnames/<CODE>.txt` content byte-for-byte behind an injected loader. The complete decompiled corpus is not committed merely to make CI self-contained.

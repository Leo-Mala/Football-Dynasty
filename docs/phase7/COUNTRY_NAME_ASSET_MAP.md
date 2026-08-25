# Phase 7 — Country/name asset map

Official corpus: `Brasfoot.apk_Decompiler.com.zip`

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Mapping

`best.u.c(int d)` resolves the name asset code through the recovered legacy country table. `LegacyCountryAssetCodes` preserves exactly 221 unique `P0..P220 -> code` entries and introduces no external sporting facts.

Examples locked by regression tests include `P0 -> AFG`, `P11 -> ARG`, `P29 -> BRA`, `P44 -> CPR`, `P45 -> TML`, `P47 -> CNG`, `P63 -> ELQ`, `P64 -> ESV`, `P81 -> GUI`, `P101 -> IRN`, `P110 -> KOS`, `P115 -> LBN`, `P143 -> NOZ`, `P145 -> PGA`, `P153 -> PRI`, `P192 -> TUR`, `P200 -> ZAM`, and `P216..P220 -> GIB/GDA/GMA/MTI/GFR`.

Recovered mapping fingerprint SHA-256: `288adda3a9630d946699da2a13d905e0da9260a3fac3578469c7b63b7bd0c1fe`.

## Asset completeness

The branch now materializes the original APK layout directly under `app/src/main/assets`:

- 221 `names/*.txt`;
- 221 `surnames/*.txt`;
- 442 files total;
- 495,785 raw bytes.

Every recovered code has exactly one names file and one surnames file. There are no missing or extra country-code files.

Physical-manifest definition: for all 442 files sorted by legacy full path, concatenate `path<TAB>sha256(file bytes)<TAB>size<LF>`.

Physical manifest SHA-256: `4b8c6db9b6343221cb6e945aef7ba0d7dc081c4744e30bb1f9a0eba3c57e444b`.

## Exact legacy filtering

`best.u.b()` keeps a line only when it is non-empty, does not contain `.`, and does not match `.*\d+.*`. Accepted lines are not trimmed. Malformed UTF-8 is replaced by the reader, matching Android legacy behavior; the raw source files remain byte-identical.

After that exact filter all 442 files remain usable; the smallest filtered file has 23 entries.

Semantic-manifest definition: for each file sorted by legacy full path, filter as above, hash accepted lines joined with `LF` plus terminal `LF`, then concatenate `path<TAB>inner_sha256<TAB>filtered_count<LF>`.

Filtered semantic manifest SHA-256: `d4cb2a6fd770a7bcdb850ee63b31d6442b66a3da70611f7a100e264b1f2c625f`.

## Consequence

For every valid legacy country id the runtime has the official asset-backed source. `best.o.X0(d)` is not replaced with fabricated generic names. The real corpus is now self-contained in the Phase 7 branch and guarded by CI fingerprints.

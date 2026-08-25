# Phase 7 — Procedural name source boundary

## Legacy source

`best.p.d(...)` calls `best.o.i0(d)` when the caller does not provide a name. `best.o.i0` first calls `best.u.c(d)` and falls back to `best.o.X0(d)` only when that result is null or empty. `best.u.c` loads `names/<COUNTRY_CODE>.txt` and `surnames/<COUNTRY_CODE>.txt`.

The authorized source is `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`. The recovered corpus contains 221 names files plus 221 surnames files: 442 files and 495,785 raw bytes.

## Proven country/path mapping

`best.u.c(int d)` resolves through the legacy country table. `LegacyCountryAssetCodes` preserves all 221 recovered `P0..P220 -> code` mappings without importing external football facts. A valid legacy country id therefore maps only to:

- `names/<CODE>.txt`
- `surnames/<CODE>.txt`

The mapping and fingerprints are documented in `COUNTRY_NAME_ASSET_MAP.md`.

## Materialized modern boundary

The official 442 files are committed byte-for-byte under `app/src/main/assets/names` and `app/src/main/assets/surnames`. `LegacyProceduralNameAssetLoader` opens those paths directly through `AssetManager`, applies the characterized `best.u.b()` filter, caches each country, and delegates deterministic selection to `LegacyProceduralNameRules` with the injected `RandomSource`.

There is no generic/fabricated name fallback. A missing official asset for a valid country is an I/O failure.

## Exact legacy filtering and encoding

`best.u.b()` keeps a line only when it is non-empty, does not contain `.`, and does not match `.*\d+.*`. Accepted lines are not trimmed.

`resources/assets/names/CRN.txt` contains one malformed UTF-8 byte. Legacy Android `InputStreamReader` replaces malformed input; the modern loader deliberately preserves that semantic boundary rather than normalizing the source bytes. The source asset itself remains byte-identical.

## Mandatory CI asset gate

`Phase 7 Validation` refuses to certify a head unless the direct assets contain:

- exactly 221 `names/*.txt` and 221 `surnames/*.txt` files;
- exactly one names/surnames file for every recovered country code;
- exactly 442 files and 495,785 raw bytes;
- physical-manifest SHA-256 `4b8c6db9b6343221cb6e945aef7ba0d7dc081c4744e30bb1f9a0eba3c57e444b`;
- filtered semantic-manifest SHA-256 `d4cb2a6fd770a7bcdb850ee63b31d6442b66a3da70611f7a100e264b1f2c625f`.

The temporary bootstrap transport was hash-gated and removed after materialization. Runtime and CI no longer depend on transport archives or fragments.

`best.o.X0(d)` remains a legacy fallback characterization boundary and is not replaced with fabricated names.

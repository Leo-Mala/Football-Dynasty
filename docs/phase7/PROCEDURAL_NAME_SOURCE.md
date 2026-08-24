# Phase 7 — Procedural name source boundary

## Legacy source

`best.p.d(...)` calls `best.o.i0(d)` when the caller does not provide a name. `best.o.i0` first calls `best.u.c(d)` and falls back to `best.o.X0(d)` only when that result is null or empty.

`best.u.c` loads `names/<COUNTRY_CODE>.txt` and `surnames/<COUNTRY_CODE>.txt`.

The official Brasfoot 2026/27 corpus contains 221 files in each directory, 442 files total and 495,785 bytes uncompressed. The smallest filtered list contains 23 entries, so the normal asset-backed path is materially present throughout the corpus.

## Proven country/path mapping

The `best.u.c(int d)` lookup resolves through the legacy country table, and the effective mapping is the recovered `best.y` entry `P<d> -> code`. `LegacyCountryAssetCodes` preserves all 221 P0..P220 codes without importing external football facts.

Therefore a valid legacy country id maps only to these virtual APK paths:

- `names/<CODE>.txt`
- `surnames/<CODE>.txt`

The mapping and official corpus manifests are documented in `COUNTRY_NAME_ASSET_MAP.md`.

## Implemented modern boundary

`LegacyProceduralNameRules` reproduces the complete list-selection and composition behavior of `best.u.c` once the two filtered lists are supplied: first-list selection; >=1000-entry bias; zero-index rewrite; one-word surname composition; two-word optional short-name composition; and exact short-circuit draw behavior.

`LegacyProceduralNameAssetLoader` now provides the Android asset boundary. It:

- maps the legacy country id through `LegacyCountryAssetCodes`;
- opens only the recovered `names/<CODE>.txt` and `surnames/<CODE>.txt` paths;
- applies the exact characterized `best.u.b()` filter;
- does not trim or normalize accepted lines;
- delegates deterministic selection to `LegacyProceduralNameRules` and the injected `RandomSource`;
- has no generic/fabricated name fallback;
- treats a missing official asset for a valid country as an I/O failure rather than silently inventing content.

The loader signature matches the resolver boundary already consumed by `LegacyProceduralPlayerRules.generateAnnualDraft(...)`.

## Exact legacy filtering

`best.u.b()` keeps a line only when it:

1. is non-empty;
2. does not contain `NumberFormat.f12825o`;
3. does not match `.*\d+.*`.

The official `NumberFormat.f12825o` constant is `.`. Accepted lines are not trimmed before being stored.

## Mandatory CI asset gate

`Phase 7 Validation` now refuses to certify a head unless `app/src/main/assets` contains the official corpus:

- exactly 221 `names/*.txt` files;
- exactly 221 `surnames/*.txt` files;
- exactly one file for every recovered P0..P220 code;
- exactly 442 files total;
- exactly 495,785 bytes;
- physical-manifest SHA-256 `4b8c6db9b6343221cb6e945aef7ba0d7dc081c4744e30bb1f9a0eba3c57e444b`;
- filtered semantic-manifest SHA-256 `d4cb2a6fd770a7bcdb850ee63b31d6442b66a3da70611f7a100e264b1f2c625f`.

This makes the missing factual corpus an explicit technical gate rather than a documentation-only warning.

## Remaining blocker

The 442 official files are not currently committed on the Phase 7 branch and their bytes are not available in the current conversation/library surfaces. They must be copied byte-for-byte from the authorized `Brasfoot.apk_Decompiler.com.zip` baseline; no substitute corpus may be created.

Until those exact bytes are available, the loader and deterministic algorithm can be compiled/tested with synthetic non-factual unit fixtures, but the real asset-backed procedural generation path cannot be declared complete and PR #6 must remain Draft.

`best.o.X0(d)` remains a fallback characterization boundary and must not be replaced with fabricated names.

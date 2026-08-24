# Phase 7 — Procedural name source boundary

## Legacy source

`best.p.d(...)` calls `best.o.i0(d)` when the caller does not provide a name. `best.o.i0` first calls `best.u.c(d)` and falls back to `best.o.X0(d)` only when that result is null or empty.

`best.u.c` loads `names/<COUNTRY_CODE>.txt` and `surnames/<COUNTRY_CODE>.txt`.

The official Brasfoot 2026/27 corpus contains 221 files in each directory, 442 files total and roughly 496 KB uncompressed. The smallest inspected filtered list still contains more than two entries, so the normal asset-backed path is materially present in the corpus.

## Implemented in this tranche

`LegacyProceduralNameRules` reproduces the complete list-selection and composition behavior of `best.u.c` once the two lists are supplied: first-list selection; >=1000-entry bias; zero-index rewrite; one-word surname composition; two-word optional short-name composition; and exact short-circuit draw behavior.

## Not yet claimed

The modern project does not yet claim that an integer legacy `d` has been wired to the exact matching country-code asset. That mapping is obtained indirectly in the APK through `best.g.d().get(best.g.a(d)).d()` and the `best.y` country table. Until that mapping and the 442 files are imported/verified, Phase 7 must not invent names or silently use a generic fallback.

`best.o.X0(d)` is therefore still a fallback characterization boundary. It is not needed to test the proven `best.u.c` list algorithm and must not be substituted with fabricated names.

# FASE 8 — MOTOR DE PARTIDAS LEGADO DETERMINÍSTICO

## Baseline

- integração da sequência: `phase4/core-game-domain`;
- merge certificado da Fase 7: `2cac79d3ae0fa31633e8a1490716efe5b431b301`;
- FINAL_HEAD da Fase 7: `19e18b44d8d7770ed17293babd212fb3c67cd1bc`;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

## Objective

Reconstruct the reachable Brasfoot 2026/27 match engine with bytecode-proven structural parity, explicit deterministic `RandomSource`, event/player/club mutation parity and a persistence-independent modern runtime, without fabricating or updating sporting facts.

## Completed scope

Phase 8 now covers the required reachable core around `ActivityJogo`, `best.s`, `components.r3`, `best.l`, `best.o` and related helpers:

- direct `best.s.k` RNG bounds/order and `S/T/U/V/W` selection;
- automatic `Q0()` pre-drawn added time, exact half loops, halftime transition, post-loop `P0()/o()` routing and flag clear;
- accompanied `ActivityJogo.D -> best.s.q` sharing the same `k -> r3.K -> stamp/append` core;
- `j`, `r`, `r0`, reachable `P0`;
- `components.r3` A/B, g, y/u/z, J/I/K, b/c, a(side), player selectors, goal routing/materialization and mutation application;
- `best.l` event fields/types and score reconstruction;
- cards, injury, substitution, player×club×season stats and reference-identity roster mutation;
- transient runtime integrating the proven mutation plans;
- automatic runtime integration on one shared `RandomSource`;
- mapping to existing modern `domain.model.Match`.

Detailed evidence remains in `MATCH_ENGINE_BOUNDARY.md`, `R3_RECOVERED_CHAIN.md`, method-specific recovery notes, and `PHASE8_FINAL_AUDIT.md`.

## Explicit evidence boundaries

`best.s.p(best.s, boolean)` has no corpus caller and `n0` is only called by it; neither is added as gameplay. Android clock/UI/audio/navigation remain presentation. Phase 8 does not invent a Room table for transient match state; Room V3 remains authoritative unless later evidence requires a schema change.

## Exit gate

Phase 8 is complete only after a frozen FINAL_HEAD passes, on the exact same SHA:

1. `Phase 8 Validation`;
2. `Phase 8 Final Certification`;
3. `Phase 7 Validation` regression;
4. aggregate diff/review audit with no factual-data change, raw RNG, destructive migration, temporary corpus artifact, conflict or unresolved material review;
5. PR #12 head/base stability and mergeability.

After those gates, mark PR #12 Ready and merge it into `phase4/core-game-domain`. The next phase must be derived from the next real legacy/application boundary after the merge rather than from a pre-invented feature list.

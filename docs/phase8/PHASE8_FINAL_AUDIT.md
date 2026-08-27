# Phase 8 — Final Audit and Closure

Official corpus: `Brasfoot.apk_Decompiler.com.zip`
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`
Legacy package/version: `com.brasfoot.v2020`, versionCode `202632`.

## Closure boundary

Phase 8 reconstructs the reachable deterministic match core, not the Android presentation layer. Java is accepted only when consistent with bytecode; SMALI is authoritative for truncated or contradictory methods.

The final branch contains characterized and executable boundaries for:

- automatic `Q0()` added-time pre-draws, exact `45 + added` loops, halftime `j(2,0)`, `k -> r3.K -> stamp/append`, post-loop `P0()/o()` routing and flag clear;
- accompanied `ActivityJogo.v() -> D(false) -> best.s.q(minute, period)`, where `q()` is proven to execute the same `k -> r3.K -> stamp/append` core;
- direct minute RNG and downstream `S/U/T`, O/P/Q counter timing, disciplinary events, injury, and second-half `j` fallback;
- `components.r3` weighted helpers, player value/metrics, `J/I/K`, `b()/c()` routing, mutation application, goal subtype/materialization and score ledger;
- `best.l` event structure/types, score reconstruction, cards, injuries, substitutions and reference-identity list mutation;
- `best.o.m()` injury RNG/skill effect and player×club×season legacy stat update;
- transient persistence-independent runtime applying the proven player/club/list/event mutations;
- stable mapping of the transient result into the existing modern `domain.model.Match` result model.

## Accompanied match boundary

Direct SMALI proves:

1. `ActivityJogo.v()` owns Android clock/presentation behavior;
2. it calls `D(false)` while the match is active;
3. `D()` reaches `best.s.q(currentMinute, currentPeriod)`;
4. `best.s.q()` creates `r3` when absent, calls `best.s.k(match, period, minute)`, then `r3.K()`, stamps a non-null event with the supplied minute/period, and appends it;
5. end-of-accompanied routing rebuilds event-derived score when required and selects the penalty UI path only behind the recovered `J0 && P0 && (home.Q0 || away.Q0)` gate.

The modern core therefore does not create a second accompanied simulation engine. Android text, images, Handler timing, sounds and navigation remain presentation responsibilities for a later UI phase.

## Runtime integration

`LegacyMatchMinteRuntimeRules` applies resolved `k()` decisions to the transient runtime.
`LegacyMatchR3RuntimeRules` applies recovered r3 mutation plans and materializes goal events into the same ledger/score.
`LegacyMatchAutomaticRuntimeRules` executes the recovered automatic half loops while using the same `RandomSource` for direct minute routing and downstream selectors/effects.
`LegacyMatchModernResultMapper` exports the final event-derived score into the already-existing modern `Match` model.

No Room change is required by this closure: the existing schema already remains valid and the legacy evidence recovered in Phase 8 does not prove an additional persistent match-runtime table that must be added here. Calendar/UI orchestration consumes the modern match result in the next functional boundary rather than fabricating persistence during match-engine reconstruction.

## Explicit exclusions

- `best.s.p(best.s, boolean)` remains unreachable in corpus-wide call-graph evidence; `n0` is only called from that unreachable method. They are not production gameplay ports.
h- Android UI timing/rendering/audio/navigation are not simulation-core semantics.
- No external football data, player update, club update, rating update or competition update is introduced.
- No raw RNG and no destructive Room migration are permitted.

## Final certification

The Phase 8 FINAL_HEAD is acceptable only when all of the following are green on that exact SHA:

- Phase 8 Validation;
- Phase 8 Final Certification;
- Phase 7 Validation regression;
- all required JUnit characterization/integration suites with zero failures/errors/skips;
- Room V3 identity and no `fallbackToDestructiveMigration`;
- official corpus/name fixture integrity;
- `assembleDebug`;
- aggregate PR audit: no factual sporting changes, raw RNG, temporary corpus files, schema drift, weakened gates, conflicts or pending material review threads.

When these conditions hold, PR #12 is eligible to leave Draft and merge into `phase4/core-game-domain`.

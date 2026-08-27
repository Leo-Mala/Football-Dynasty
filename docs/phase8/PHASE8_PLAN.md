# FASE 8 — MOTOR DE PARTIDAS LEGADO DETERMINÍSTICO

## Baseline

- merge certificado da Fase 7: `2cac79d3ae0fa31633e8a1490716efe5b431b301`;
- FINAL_HEAD da Fase 7: `19e18b44d8d7770ed17293babd212fb3c67cd1bc`;
- corpus oficial: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

The exact current Phase 8 head and its workflow runs must always be discovered from GitHub; this plan intentionally does not pin a mutable Phase 8 SHA.

## Objective

Reconstruct the legacy match engine with provable structural parity, explicit deterministic RNG, and no update or fabrication of sporting data.

The modern domain already has match/career/season models. Phase 8 reconstructs the proven legacy simulation boundary around `ActivityJogo`, `best.s`, `components.r3`, `best.l`, `best.o` and related reachable helpers before integrating any remaining effects into the modern career flow.

## Recovered scope to date

The branch has characterization coverage for:

- direct per-minute RNG tree and exact bounds in `best.s.k(...)`;
- `S/T/U/V/W` player selectors and explicit deterministic shuffle behavior;
- `Q0()` added-time pre-draw order and exact minute-loop boundaries;
- pure automatic `Q0()` orchestration through both half loops, including exact `k -> K -> event stamp/append` ordering and `j(2, 0)` halftime transition;
- post-loop `Q0()` short-circuit routing for `Z && a0 && P0()`, with optional `o()` routing before the aggregate two-club flag clear and without inventing the internal effects of `o()`;
- second-half / transition paths `j`, `r`, `r0`;
- reachable `P0()` resolution ordering;
- `best.l` event fields, event types and score reconstruction;
- disciplinary routing `best.s.c/d`;
- goal subtype draw and reachable `components.r3.f(...)` materialization ordering;
- `components.r3.n()` primary author and `j()` own-goal author selectors;
- truncated reachable `components.r3.i(primary)` secondary-player selector;
- shared strict weighted helper `components.r3.A/B`;
- player-value transform `components.r3.g(best.o)`;
- metrics `components.r3.y/u/z`;
- decision layer `components.r3.J/I`;
- advance/short-circuit routing `components.r3.K`;
- composed shared-RNG `J -> I -> K` ordering and reusable pure one-step orchestration;
- percentage update `components.r3.a(side)` including the Java-vs-bytecode float-division correction;
- injury duration/RNG and age/energy effects in `best.o.m`;
- substitution selection and mutation ordering in `best.s.p1/o1`.

The Phase 8 workflow requires the corresponding characterization suites and rejects raw RNG APIs in modern gameplay domains.

## Explicit non-targets unless new evidence appears

Corpus reachability work established that `best.s.p(Lbest/s;Z)[I` has no callers and `n0(Lbest/s;)I` is called only by `p`. They are not priority production ports merely because their methods exist. They remain evidence-only unless later reachability evidence proves a live gameplay path.

## Remaining work order

1. keep exact-head Phase 7 regression and Phase 8 characterization/build gates green after every material change;
2. keep `MATCH_ENGINE_BOUNDARY.md` and `R3_RECOVERED_CHAIN.md` synchronized with behavior already represented by code/tests;
3. use authoritative legacy evidence to close any remaining reachable `components.r3.b()/c()` routing details not already captured by goal materialization;
4. characterize remaining event-driven player/club state effects only when their callers and ordering are proven;
5. characterize the internal effects of automatic-path `o()` and any remaining required post-match side effects for automatic/accompanied paths only from authoritative evidence;
6. integrate the proven pure-engine plans into a modern match runtime and then calendar/career flow only after the required mutation/application boundaries are closed;
7. alter Room only if a newly proven persistent state cannot be represented by the current schema;
8. freeze a FINAL_HEAD and run final exact-head audit before making the PR ready/mergeable for Phase 8 completion.

If the raw external corpus/SMALI required for a truncated method is not available to the active task, do not infer missing behavior from names or earlier notes. Record that evidence boundary and continue only where committed evidence is sufficient.

## Permanent rules

- use `RandomSource` in the modern gameplay domain; raw RNG remains forbidden;
- preserve draw order, exact bounds, comparisons, short-circuits and draw count;
- preserve mutation ordering and proven legacy quirks, including dead branches;
- do not claim equivalence to the APK's implicit RNG seed unless actually proven;
- do not alter players, clubs, ratings, attributes, squads, competitions or sporting facts from external sources;
- `fallbackToDestructiveMigration` remains forbidden;
- Codex is unavailable; review uses independent diff inspection, Java↔SMALI evidence already available, tests, fixtures and GitHub Actions;
- do not merge while a proven reachable Phase 8 function in scope is replaced by inference or left materially unresolved.

## Exit gate

Phase 8 ends only when:

- automatic and accompanied match paths needed by the modern integration are mapped to the required proven boundary;
- reachable RNG sites in scope are explicit and deterministic;
- required truncated reachable methods are recovered or explicitly bounded by evidence;
- event/score/player/club/post-match effects required by those paths are characterized;
- factual-data freeze and Room policy are intact;
- Phase 7 regression and Phase 8 validation are green on the exact FINAL_HEAD;
- the aggregate PR diff has no accidental factual-data change, raw RNG, weakened test, destructive migration or temporary artifact;
- the PR is conflict-free, mergeable and has no material review finding pending.

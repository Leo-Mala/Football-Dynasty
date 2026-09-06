# Fase 15 — matriz de paridade completa com o legado

Status: **ACTIVE / MARCO C**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — `com.brasfoot.v2020` — versionCode `202632`.

A matriz é incremental e não converte inferência em gameplay. `SMALI/executável` prevalece sobre Java decompilado em caso de divergência. O ZIP bruto oficial foi reaberto e o SHA-256 foi reconfirmado neste checkpoint; novos detalhes abaixo são promovidos somente quando extraídos do executável.

| LEGACY_FUNCTION | CALL_PATH | MODERN_EQUIVALENT | STATUS | EVIDENCE / TEST | ACTION |
|---|---|---|---|---|---|
| `best.m.z()` | `best.n.n()` → `best.b.d()` → `best.c0.l1()` → `best.m.z()` | `LegacyFinanceLedgerRule.resetPeriod()` + Room transition | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_FINANCE_PERIOD_RESET_REACHABILITY.md`; finance/Room regressions | retain; re-open only on regression |
| `best.c0.l1()` | opening pass of `best.b.d()` | iterate only materialized club-finance runtime rows | `IMPLEMENTED_AND_CERTIFIED` | null-check/call ordering frozen; public Room transition regression | retain |
| `best.b.d()` (`NovoAno`) | `ActivityFimAno.e()` → `best.n.n()` | annual stages represented by separately proven boundaries | `PARTIALLY_IMPLEMENTED` | annual router/lifecycle evidence + certified junior/finance/maintenance subflows | compose only after remaining material callees below close |
| `best.n.n()` | end-year route | thin router | `CHARACTERIZED` | `PHASE15_ANNUAL_ROUTER_LIFECYCLE_EVIDENCE.md` | no independent persistence |
| `best.n.m()` | annual stage 4 | `LegacyAnnualNMRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | exact order, unconditional `nextInt(100)`, 50/51 boundary and route tests | compose substantive callees below; no duplicate RNG policy |
| `best.b.d4()` / `components.o2` | optional first maintenance stage | V14 `career_active_loans` + `LegacyAnnualDeferredTransferExecutionRule` | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_ANNUAL_DEFERRED_STATE_MAPPING.md` | no V15; retain normalized loan lifecycle |
| `best.b.e4()` / `components.y1` | optional stadium maintenance | V14 `career_stadium_constructions` + stadium runtime/completion stores | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_ANNUAL_DEFERRED_STATE_MAPPING.md` | no V15; retain normalized construction lifecycle |
| `best.b.j2(1)` + `best.a.J(1)` | unconditional stage in `best.n.m()` | `LegacyAnnualJ2CommandRules` | `PARTIALLY_IMPLEMENTED` | source-order dispatch, cD guard, no-op/unknown and unconditional clear are tested | close only substantive callees still open: downstream tournament bootstrap, `p`, `A`; senior payroll raw-N persistence |
| `best.a.r()` (`dJ`) | `J(1)` command `dJ` | `CareerFinanceBorrowingStore.applyMonthlyBorrowingCharges()` | `IMPLEMENTED_AND_CERTIFIED` | monthly borrowing regression | retain |
| `best.a.s()` (`ds` routing) | `J(1)` command `ds` | `LegacyAnnualClubPayrollRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | source-order clubs + `Y0(month)` filter tested | compose end-to-end after senior raw `n` durable mapping closes |
| `best.c0.z()` / `q()` / `E(long)` | callee of annual `ds` | `LegacyAnnualClubPayrollCompositionRules` + `LegacyFinanceRuntimeRule` | `PARTIALLY_IMPLEMENTED` | `q()` and `E(long)` implemented; official SMALI proves `m0()==best.o.n` and `u()==best.p.i`; V14 junior `legacyI` mapped | only senior raw `best.o.n` durable owner remains open |
| `best.o.m0()` | senior contribution to `q()` | `seniorContributionFromLegacyN()` | `IMPLEMENTED_AND_TESTED` | official `smali/best/o.smali`: direct `iget n:I; return`; scalar tests | include raw senior `n` in minimal durable-state audit |
| `best.p.u()` | junior contribution to `q()` | `juniorContributionFromLegacyI()` + V14 `CareerJuniorDraftEntity.legacyI` | `IMPLEMENTED_AND_TESTED` | official `smali/best/p.smali`: direct `iget i:I; return`; scalar/V14 mapping | retain |
| `best.a.n(false)` (`cS`) | `J(1)` command `cS` | `LegacyAnnualNEmploymentRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | N1 guard, K/y filters, source order and overwrite behavior tested | close downstream `best.b.A(f0,false)` / `best.n.g` lifecycle |
| `best.a.n(true)` (`cSempregado`) | `J(1)` command `cSempregado` | `LegacyAnnualNEmploymentRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | N1 guard, K filter, source order and overwrite behavior tested | close downstream `best.b.A(f0,false)` / `best.n.g` lifecycle |
| `best.b.A(best.f0,false)` / `best.n.g` | callee of annual employment routing | none proven | `REACHABLE_NOT_IMPLEMENTED` | routing boundary frozen; substantive callee/lifecycle still open | inspect official executable corpus; do not infer semantics |
| `best.a.q()` (`cw`) / `konrent.b0.V()` | `J(1)` command `cw` | `LegacyAnnualTournamentBootstrapRoutingRules` + `LegacyTournamentBootstrapRules` | `PARTIALLY_IMPLEMENTED` | q single-call router plus raw-SMALI fallback `J>1`, first missing `J=2/3/4/5` tier fills and exact local `nextInt(3)` permutation map are tested; `PHASE15_TOURNAMENT_BOOTSTRAP_SMALI.md` | remaining: prior competition participant recovery, `Collections.shuffle` compatibility ownership and `konrent.a0`/`konrent.f0` construction/state mapping |
| `best.a.p()` (`cD`) | guarded `J(1)` command `cD` | none end-to-end | `REACHABLE_NOT_IMPLEMENTED` | official raw SMALI confirms broader tournament/list mutation flow; routing guard already frozen | prove modern competition/list ownership before implementation |
| `best.b.p()` annual player sweep | `J(1)` command `aj` | `LegacyAnnualPlayerProgressionSweepRules` + junior/senior boundaries | `PARTIALLY_IMPLEMENTED` | senior-before-junior orchestration tested; junior side certified | compose frozen senior growth boundaries + durable senior state |
| `best.o.e()` | senior pass inside `best.b.p()` | `LegacyAnnualSeniorProgressionRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | null-club early return; age 31/32 split; clear-M ordering tested | compose after full growth + persistence mapping |
| `best.o.t()` | decline branch from `best.o.e()` | `LegacyAnnualSeniorDeclineRules` | `IMPLEMENTED_AND_CERTIFIED` | exact N accumulation, age/club tier weights, strict `N>1.0`, floors tested | persist N only after aggregate state map closes |
| `best.o.s()` rate accumulation into `N` | growth branch prefix | `LegacyAnnualSeniorGrowthAccumulationRules` | `IMPLEMENTED_AND_TESTED` | official `best/o.smali`; complete `.16..02` base matrix, modifier order, raw club J/j0 branches, negative guard + retained-N tests; `PHASE15_SENIOR_GROWTH_RATE_SMALI.md` | retain; no rate re-investigation absent regression |
| `best.o.s()` target/cap selection | after retained `N` accumulation, before high-`d0` RNG | `LegacyAnnualSeniorGrowthTargetRules` | `IMPLEMENTED_AND_TESTED` | official `best/o.smali`; complete `R0/O`, `p0`, `J/j0` matrices and final `min` tested; `PHASE15_SENIOR_GROWTH_TARGET_SMALI.md` | compose through real senior runtime after durable-state mapping |
| `best.o.s()` high-`d0` RNG branch | growth branch | `LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment` | `IMPLEMENTED_AND_CERTIFIED` | `d0>=60`, one `nextInt(5)`, 7/8/9/10 bonuses, pointless-draw quirk tested | retain; no seed-parity claim |
| `best.o.s()` final cap/fraction block | growth branch | `LegacyAnnualSeniorGrowthFinalizationRules` | `IMPLEMENTED_AND_CERTIFIED` | strict `N>1.0`, one-point max, cap-block reset to `N=1.0`, overall-100 preservation tested | retain |
| senior `M` (`S()/s1(Boolean)`) | lineup/match use → annual `best.o.e()` | no durable V14 field proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | lineup/substitution writers + annual reader/clear mapped | include in minimal persistence delta after reader/writer closure |
| senior `N: double` | `best.o.s()` + `best.o.t()` | no durable V14 field proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | both annual branches use retained fractional accumulator; complete growth computation now characterized in separate boundaries | include in minimal persistence delta with remaining senior fields |
| senior payroll raw `n:I` | `best.o.m0()` → annual `best.c0.q()` | no durable V14 owner proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | direct SMALI getter proved; payroll contribution exact | audit writers/readers and group with minimal senior persistence delta |
| senior `d/W0` | player runtime flags → `W0()` / `F()` | no durable V14 field proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | official `best/o.smali`: serialized Boolean backing field `d`; `z1(Boolean)` writer and readers mapped | include only `d` in durable-state audit; legacy `j0:I` is transient and must not be persisted |
| senior `j0:I` | annual/reset runtime counter | runtime-only owner required | `TRANSIENT_RUNTIME_STATE` | official `best/o.smali` declares `j0:I` as `transient`; `j0()`/`d1()` access mapped | never add Room column solely for this legacy field; compose runtime lifecycle only |
| `best.b.F()` | final `best.n.m()` route when `E1()==true` | `LegacyAnnualFResetRules` + lower-level rules | `PARTIALLY_IMPLEMENTED` | exact three-pass order + first-`z0()` quirk tested | close `k0.c()` internals and durable `d/W0` mapping; keep j0 transient |
| `best.o.d1(0)` | pass 2 of `best.b.F()` | `LegacyAnnualPlayerD0Rules.resetGlobalCounter()` | `IMPLEMENTED_AND_CERTIFIED` | global `j0=0`, unrelated state preserved, idempotence tested | keep `j0` transient; compose only the proven runtime lifecycle |
| `best.o.D0()` | pass 3 of `best.b.F()` | `LegacyAnnualPlayerD0Rules` | `IMPLEMENTED_AND_CERTIFIED` | increment-before-guards and code/threshold matrix 2/3/4 frozen | transient `j0` runtime + durable `d/W0` mapping still open |
| `best.k0.c(index)` traversal | pass 1 of `best.b.F()` | `LegacyAnnualTournamentEntryResetRules` | `PARTIALLY_IMPLEMENTED` | exact selector sequence `[0,1,2,2,5,6,6,3,3,4,4]` and multiplicity tested | prove `components.n1` thresholds, `best.h0` collections/player flag/persistence |
| `F2(true)` / `M0` | original `P0()==0` in `best.n.m()` | router flag only; no independent persistence justified | `CHARACTERIZED` | readers/writers/lifecycle route already mapped | do not create schema field absent new contradictory evidence |
| `g4()` → `components.n3` → `best.f` selection | gate `best.n.m() > 50` | `LegacyAnnualRandomRules` + `LegacyAnnualSelectionRules` | `PARTIALLY_IMPLEMENTED` | gates/ranges/filters/draw counts and explicit deterministic compatibility policy already characterized | compose exact runtime candidate collections/source order using existing RandomSource policy; no seed-parity claim |
| `T1(target,value,true,false,false)` annual g4 mutation | selected transfer result | `LegacyAnnualG4TransferExecutionRule` → `CareerManagerRuntimeStore.commitTransfer()` | `IMPLEMENTED_AND_CERTIFIED` | exact flags/contract/financial effects tested | connect only when selector composition closes |
| junior tryout `best.b.h2(c0)` | `ActivityJuniores.j()` | `CareerJuniorRuntimeStore.runTrial()` | `IMPLEMENTED_AND_CERTIFIED` | six positions, cap 18, raw finance 9, interleaved persisted RNG, rollback | no action absent regression |
| `best.p.b()` junior development | youth draft annual development | `LegacyJuniorRuntimeRules.progressDevelopment()` + Room V14 draft | `IMPLEMENTED_AND_CERTIFIED` | SMALI-authoritative strict `D>1.0`, fractional state, reopen tests | no action absent regression |
| manual `best.t.e(FALSE,p,c0)` | `ActivityJuniores` promotion | `CareerJuniorManualPromotionStore.promote()` | `IMPLEMENTED_AND_CERTIFIED` | no pre-promotion final player; 30-senior cap; RNG/Room atomicity and rollback | no action absent regression |
| annual `best.p.c(c0)` + `best.t.e(TRUE,p,c0)` | annual youth lifecycle | `CareerJuniorAnnualLifecycleStore.run()` | `IMPLEMENTED_AND_CERTIFIED` | original-list snapshot, immediate replacement generation, deferred list edits, reopen/rollback | no action absent regression |
| `ActivityJuniores` surface | youth-team UI | substantive seams above; remaining behavior deferred to UI milestone | `PRESENTATION_ONLY` | complete reachable surface classified | Phase 17 presentation reconstruction only |
| `ActivityFimAno.e()` entry | end-year UI entry | characterized route into annual lifecycle | `PARTIALLY_IMPLEMENTED` | entry order known; substantive effects tracked row-by-row above | close remaining annual blockers before whole-surface promotion |
| `best.c0.y()` manager replacement path | several reachable manager flows | Marco B manager replacement runtime | `IMPLEMENTED_AND_CERTIFIED` | Fase 14 manager evidence/tests | re-open only on contradictory evidence |

## Fase 15.1 — Juniores closure checkpoint

Fase 15.1 is **IMPLEMENTED_AND_CERTIFIED**. V14 persists the pre-promotion draft separately from materialized players and the runtime preserves trial, dismissal, development, manual promotion, annual promotion/replacement, RNG ordering, finance atomicity, rollback and save/reopen. No junior work is reopened without concrete regression evidence.

## Current closure blockers

The raw official archive is available and SHA-verified. The middle target/cap computation of `best.o.s()` is no longer a blocker. The remaining material areas are:

1. minimal durable mapping and runtime composition of senior `M/N` plus payroll raw `n`;
2. durable mapping of `d/W0`; legacy `j0:I` is proven transient and must remain runtime-only;
3. internals of `best.k0.c(index)` (`components.n1`, `best.h0`, player flag/lifecycle);
4. remaining `konrent.b0.V()` ownership (`Collections.shuffle`, prior participant recovery, `konrent.a0`/`konrent.f0`) plus `best.a.p()` mutation; fallback tier selection and fixed permutation mapping are now frozen;
5. `best.b.A(best.f0,false)` + lifecycle of `best.n.g`;
6. object-level `best.f` candidate collection/source-order composition using the already-frozen deterministic compatibility RNG policy.

`d4/o2`, `e4/y1`, junior persistence/runtime, payroll getter formulas, the thin `best.a.q()` router, tournament fallback tier selection/permutation mapping and the now-frozen senior rate/target/high-d0/finalization computations are **not** blockers and must not be re-investigated absent regression.

## Status rules

- `IMPLEMENTED_AND_CERTIFIED`: implementation exists and the relevant boundary is covered by tests on a certified branch checkpoint.
- `IMPLEMENTED_AND_TESTED`: implementation and regression exist on the active checkpoint; certification follows exact-SHA CI.
- `PARTIALLY_IMPLEMENTED`: a reachable parent/subsystem has implemented sub-boundaries, but at least one substantive callee/state mapping remains open.
- `CHARACTERIZED`: behavior/reachability is proven but no independent runtime implementation is required for the isolated seam.
- `PERSISTENT_RUNTIME_GAP_PROVEN`: durable legacy state is reachable and no equivalent V14 state has yet been proven.
- `TRANSIENT_RUNTIME_STATE`: executable state is proven transient in the legacy serialization and therefore requires runtime composition only, not a Room compatibility column by itself.
- `REACHABLE_NOT_IMPLEMENTED`: executable behavior is proven reachable and no modern equivalent exists yet.
- `PRESENTATION_ONLY`: no substantive gameplay mutation remains in this surface.
- `UNKNOWN_NEEDS_INVESTIGATION`: evidence is incomplete; no gameplay is inferred.

The Fase 13 historical conclusion about `best.m.z()` having no caller is explicitly superseded by the Fase 15 corpus-wide chain documented in `PHASE15_FINANCE_PERIOD_RESET_REACHABILITY.md`; historical files are not rewritten.
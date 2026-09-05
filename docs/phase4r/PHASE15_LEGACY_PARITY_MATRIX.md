# Fase 15 — matriz de paridade completa com o legado

Status: **ACTIVE / MARCO C**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — `com.brasfoot.v2020` — versionCode `202632`.

A matriz é incremental e não converte inferência em gameplay. `SMALI/executável` prevalece sobre Java decompilado em caso de divergência. Este checkpoint consolida apenas evidência já congelada e certificada no branch; quando o ZIP bruto não estiver materializado, nenhum detalhe novo de gameplay deve ser inferido.

| LEGACY_FUNCTION | CALL_PATH | MODERN_EQUIVALENT | STATUS | EVIDENCE / TEST | ACTION |
|---|---|---|---|---|---|
| `best.m.z()` | `best.n.n()` → `best.b.d()` → `best.c0.l1()` → `best.m.z()` | `LegacyFinanceLedgerRule.resetPeriod()` + Room transition | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_FINANCE_PERIOD_RESET_REACHABILITY.md`; finance/Room regressions; current certified branch baseline | retain; re-open only on regression |
| `best.c0.l1()` | opening pass of `best.b.d()` | iterate only materialized club-finance runtime rows | `IMPLEMENTED_AND_CERTIFIED` | null-check/call ordering frozen; public Room transition regression | retain |
| `best.b.d()` (`NovoAno`) | `ActivityFimAno.e()` → `best.n.n()` | annual stages represented by separately proven boundaries | `PARTIALLY_IMPLEMENTED` | annual router/lifecycle evidence + certified junior/finance/maintenance subflows | compose only after remaining material callees below close |
| `best.n.n()` | end-year route | thin router | `CHARACTERIZED` | `PHASE15_ANNUAL_ROUTER_LIFECYCLE_EVIDENCE.md` | no independent persistence |
| `best.n.m()` | annual stage 4 | `LegacyAnnualNMRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | exact order, unconditional `nextInt(100)`, 50/51 boundary and route tests | compose substantive callees below; no duplicate RNG policy |
| `best.b.d4()` / `components.o2` | optional first maintenance stage | V14 `career_active_loans` + `LegacyAnnualDeferredTransferExecutionRule` | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_ANNUAL_DEFERRED_STATE_MAPPING.md`; writer `best.o.q()` proves active-loan lifecycle; return T1 flags frozen | no V15; retain normalized loan lifecycle |
| `best.b.e4()` / `components.y1` | optional stadium maintenance | V14 `career_stadium_constructions` + stadium runtime/completion stores | `IMPLEMENTED_AND_CERTIFIED` | `PHASE15_ANNUAL_DEFERRED_STATE_MAPPING.md`; `ActivityEstadio` writer and completion semantics frozen | no V15; retain normalized construction lifecycle |
| `best.b.j2(1)` + `best.a.J(1)` | unconditional stage in `best.n.m()` | `LegacyAnnualJ2CommandRules` | `PARTIALLY_IMPLEMENTED` | source-order dispatch, cD guard, no-op/unknown and unconditional clear are tested | close only substantive callees still open: `q`, `p`, `A`; payroll value mapping |
| `best.a.r()` (`dJ`) | `J(1)` command `dJ` | `CareerFinanceBorrowingStore.applyMonthlyBorrowingCharges()` | `IMPLEMENTED_AND_CERTIFIED` | monthly borrowing regression freezes charge order/categories/negative cash behavior | retain |
| `best.a.s()` (`ds` routing) | `J(1)` command `ds` | `LegacyAnnualClubPayrollRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | source-order clubs + `Y0(month)` filter tested | implement `best.c0.q()/E(long)` only after exact value mapping is proven |
| `best.c0.z()` / `q()` / `E(long)` | callee of annual `ds` | finance input/runtime pieces exist, numerical equivalence not yet proven | `PARTIALLY_IMPLEMENTED` | payload/call shape characterized; `CareerClubFinanceInputResolver` is not itself parity proof | prove senior `m0()` + junior `u()` value composition before transaction wiring |
| `best.a.n(false)` (`cS`) | `J(1)` command `cS` | `LegacyAnnualNEmploymentRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | N1 guard, K/y filters, source order and overwrite behavior tested | close downstream `best.b.A(f0,false)` / `best.n.g` lifecycle |
| `best.a.n(true)` (`cSempregado`) | `J(1)` command `cSempregado` | `LegacyAnnualNEmploymentRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | N1 guard, K filter, source order and overwrite behavior tested | close downstream `best.b.A(f0,false)` / `best.n.g` lifecycle |
| `best.b.A(best.f0,false)` / `best.n.g` | callee of annual employment routing | none proven | `REACHABLE_NOT_IMPLEMENTED` | routing boundary frozen; substantive callee/lifecycle still open | reopen official executable corpus; do not infer semantics |
| `best.a.q()` (`cw`) | `J(1)` command `cw` | none end-to-end | `REACHABLE_NOT_IMPLEMENTED` | known tournament rebuild path consumes implicit shuffle + raw `nextInt(3)` | characterize exact collections/order/RNG before implementation |
| `best.a.p()` (`cD`) | guarded `J(1)` command `cD` | none end-to-end | `REACHABLE_NOT_IMPLEMENTED` | reachable tournament mutation characterized at routing level | prove list/state semantics before implementation |
| `best.b.p()` annual player sweep | `J(1)` command `aj` | `LegacyAnnualPlayerProgressionSweepRules` + junior/senior boundaries | `PARTIALLY_IMPLEMENTED` | senior-before-junior orchestration tested; junior side certified | close remaining `best.o.s()` calculation + durable senior state |
| `best.o.e()` | senior pass inside `best.b.p()` | `LegacyAnnualSeniorProgressionRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | null-club early return; age 31/32 split; clear-M ordering tested | compose after full growth + persistence mapping |
| `best.o.t()` | decline branch from `best.o.e()` | `LegacyAnnualSeniorDeclineRules` | `IMPLEMENTED_AND_CERTIFIED` | exact N accumulation, age/club tier weights, strict `N>1.0`, floors tested | persist N only after aggregate state map closes |
| `best.o.s()` high-`d0` RNG branch | growth branch | `LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment` | `IMPLEMENTED_AND_CERTIFIED` | `d0>=60`, one `nextInt(5)`, 7/8/9/10 bonuses, pointless-draw quirk tested | retain; no seed-parity claim |
| `best.o.s()` final cap/fraction block | growth branch | `LegacyAnnualSeniorGrowthFinalizationRules` | `IMPLEMENTED_AND_CERTIFIED` | strict `N>1.0`, one-point max, cap-block reset to `N=1.0`, overall-100 preservation tested | retain |
| remaining preceding block of `best.o.s()` | growth branch before finalization | none complete | `SMALI_REQUIRED` | Java decompiler insufficient; exact rate/cap branch mapping not frozen | reopen official SMALI; do not map `0.16..0.02` rates by inference |
| senior `M` (`S()/s1(Boolean)`) | lineup/match use → annual `best.o.e()` | no durable V14 field proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | lineup/substitution writers + annual reader/clear mapped | include in minimal persistence delta only after reader/writer closure |
| senior `N: double` | `best.o.s()` + `best.o.t()` | no durable V14 field proven | `PERSISTENT_RUNTIME_GAP_PROVEN` | both annual branches use retained fractional accumulator | include in minimal persistence delta after full growth proof |
| `best.b.F()` | final `best.n.m()` route when `E1()==true` | `LegacyAnnualFResetRules` + lower-level rules | `PARTIALLY_IMPLEMENTED` | exact three-pass order + first-`z0()` quirk tested | close `k0.c()` internals and durable `j0/d` mapping |
| `best.o.d1(0)` | pass 2 of `best.b.F()` | `LegacyAnnualPlayerD0Rules.resetGlobalCounter()` | `IMPLEMENTED_AND_CERTIFIED` | global `j0=0`, unrelated state preserved, idempotence tested | persist only with proven aggregate j0 lifecycle |
| `best.o.D0()` | pass 3 of `best.b.F()` | `LegacyAnnualPlayerD0Rules` | `IMPLEMENTED_AND_CERTIFIED` | increment-before-guards and code/threshold matrix 2/3/4 frozen | durable `j0` + `d/W0` mapping still open |
| `best.k0.c(index)` traversal | pass 1 of `best.b.F()` | `LegacyAnnualTournamentEntryResetRules` | `PARTIALLY_IMPLEMENTED` | exact selector sequence `[0,1,2,2,5,6,6,3,3,4,4]` and multiplicity tested | prove `components.n1` thresholds, `best.h0` collections/player flag/persistence |
| `F2(true)` / `M0` | original `P0()==0` in `best.n.m()` | router flag only; no independent persistence justified | `CHARACTERIZED` | readers/writers/lifecycle route already mapped | do not create schema field absent new contradictory evidence |
| `g4()` → `components.n3` → `best.f` selection | gate `best.n.m() > 50` | `LegacyAnnualRandomRules` + `LegacyAnnualSelectionRules` | `PARTIALLY_IMPLEMENTED` | gates/ranges/filters/draw counts characterized | compose exact runtime candidate collections/source order + implicit RNG policy |
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

Only the following material areas still block Phase 15 completion:

1. full preceding calculation/cap block of `best.o.s()` plus minimal durable mapping of `M/N`;
2. durable mapping of `j0` and `d/W0`, only after full reader/writer audit;
3. internals of `best.k0.c(index)` (`components.n1`, `best.h0`, player flag/lifecycle);
4. `best.a.q()` / tournament rebuild and `best.a.p()` mutation;
5. `best.b.A(best.f0,false)` + lifecycle of `best.n.g`;
6. exact numerical composition of annual club payroll `best.c0.q()/E(long)`;
7. object-level `best.f` candidate collection/source-order composition and implicit RNG compatibility.

`d4/o2`, `e4/y1`, junior persistence/runtime and the already-frozen senior sub-blocks are **not** blockers and must not be re-investigated absent regression.

## Status rules

- `IMPLEMENTED_AND_CERTIFIED`: implementation exists and the relevant boundary is covered by tests on a certified branch checkpoint.
- `PARTIALLY_IMPLEMENTED`: a reachable parent/subsystem has certified sub-boundaries, but at least one substantive callee/state mapping remains open.
- `CHARACTERIZED`: behavior/reachability is proven but no independent runtime implementation is required for the isolated seam.
- `PERSISTENT_RUNTIME_GAP_PROVEN`: durable legacy state is reachable and no equivalent V14 state has yet been proven.
- `REACHABLE_NOT_IMPLEMENTED`: executable behavior is proven reachable and no modern equivalent exists yet.
- `PRESENTATION_ONLY`: no substantive gameplay mutation remains in this surface.
- `SMALI_REQUIRED`: Java is insufficient/ambiguous and executable evidence is still required.
- `UNKNOWN_NEEDS_INVESTIGATION`: evidence is incomplete; no gameplay is inferred.

The Fase 13 historical conclusion about `best.m.z()` having no caller is explicitly superseded by the Fase 15 corpus-wide chain documented in `PHASE15_FINANCE_PERIOD_RESET_REACHABILITY.md`; historical files are not rewritten.

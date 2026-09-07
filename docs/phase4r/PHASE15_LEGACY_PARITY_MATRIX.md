# Fase 15 — matriz final de paridade completa com o legado

Status: **CLOSURE_CANDIDATE / FINAL_HEAD_PENDING_CI**

Corpus oficial: `Brasfoot.apk_Decompiler.com.zip` — SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465` — package `com.brasfoot.v2020` — versionCode `202632`.

Esta matriz substitui as classificações conservadoras dos checkpoints intermediários da Fase 15. SMALI/executável continua prevalecendo sobre Java decompilado. Nenhuma linha abaixo autoriza inventar gameplay, regra esportiva, dado factual, backfill, default ou seed parity inexistente.

## Regra de promoção do FINAL_HEAD

O commit que contém esta matriz é o candidato a `FINAL_HEAD`. Se, nesse **mesmo SHA**, `Phase 7 Validation`, `Phase 8 Validation` e `Phase 8 Final Certification` concluírem `SUCCESS`, todas as linhas marcadas `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` ficam promovidas a `IMPLEMENTED_AND_CERTIFIED` para o Marco C **sem novo commit documental**. Se qualquer gate falhar, a Fase 15 continua aberta e o erro deve ser corrigido antes da promoção.

| LEGACY_FUNCTION / STATE | CALL_PATH | MODERN_EQUIVALENT | STATUS | EVIDENCE / OWNERSHIP | PHASE 15 ACTION |
|---|---|---|---|---|---|
| `best.m.z()` | `best.n.n()` → `best.b.d()` → `best.c0.l1()` | `LegacyFinanceLedgerRule.resetPeriod()` + Room transition | `IMPLEMENTED_AND_CERTIFIED` | reachability + finance/reopen regressions | none |
| `best.c0.l1()` | opening pass of `best.b.d()` | deterministic iteration of materialized finance runtimes | `IMPLEMENTED_AND_CERTIFIED` | null semantics/order frozen | none |
| `best.b.d()` (`NovoAno`) | `ActivityFimAno.e()` → `best.n.n()` | composition of proven annual boundaries | `COMPOSED_BY_PROVEN_BOUNDARIES` | all substantive reachable callees below have owners; no material orphan remains | none |
| `best.n.n()` | end-year route | thin annual router | `CHARACTERIZED_NO_INDEPENDENT_STATE` | router/lifecycle evidence | none |
| `best.n.m()` | annual stage 4 | `LegacyAnnualNMRoutingRules` + callees below | `IMPLEMENTED_AND_CERTIFIED` | exact order, draw and branch boundaries | none |
| `best.b.d4()` / `components.o2` | optional maintenance | V14 active-loan runtime + `LegacyAnnualDeferredTransferExecutionRule` | `IMPLEMENTED_AND_CERTIFIED` | normalized loan lifecycle | none |
| `best.b.e4()` / `components.y1` | optional maintenance | V14 stadium-construction runtime | `IMPLEMENTED_AND_CERTIFIED` | normalized construction lifecycle | none |
| `best.b.j2(1)` + `best.a.J(1)` | unconditional annual stage | `LegacyAnnualJ2CommandRules` + proven command callees | `COMPOSED_BY_PROVEN_BOUNDARIES` | `cw`, `ds`, `aj`, `cD`, `dJ`, `cS`, `cSempregado`, clear/no-op behavior covered | none |
| `best.a.r()` (`dJ`) | `J(1)` | borrowing-charge runtime | `IMPLEMENTED_AND_CERTIFIED` | finance regression | none |
| `best.a.s()` (`ds`) | `J(1)` | `LegacyAnnualClubPayrollRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | source order + month filter | none |
| `best.c0.z()` / `q()` / `E(long)` | annual payroll | `LegacyAnnualClubPayrollCompositionRules` + finance runtime | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | senior raw `n` now durable in V15; junior raw `i` already V14 | final CI only |
| `best.o.m0()` | senior payroll contribution | `CareerPlayerRuntimeEntity.legacyRawPayrollN` + exact scalar rules | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | direct getter + writer/recompute boundary | final CI only |
| `best.p.u()` | junior payroll contribution | V14 junior `legacyI` | `IMPLEMENTED_AND_CERTIFIED` | direct getter/mapping | none |
| `best.a.n(false/true)` | `cS` / `cSempregado` | `LegacyAnnualNEmploymentRoutingRules` | `IMPLEMENTED_AND_CERTIFIED` | guards/order/overwrite routing | none |
| `best.b.A(best.f0,false)` | annual employment routing | `LegacyAnnualEmploymentCandidateRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | exact source pools/order, matrices, quotas and legacy quirks; shuffle routed through project `RandomSource` policy | final CI only |
| `best.n.g` | result of annual employment selection | `LegacyAnnualNEmploymentTransientRules` | `TRANSIENT_RUNTIME_STATE_CLOSED` | static/session value: overwritten, read by `k()`, cleared by `j()`; no serialization owner required | none |
| `best.a.q()` / `konrent.b0.V()` | `cw` | routing + `LegacyTournamentBootstrapRules` + `LegacyTournamentBootstrapExecutionRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | prior recovery, `J>1` fallback, tier fills, shuffle, permutation and exact `a0/f0` construction call frozen | final CI only |
| `best.a.p()` | guarded `cD` | `LegacyAnnualPCompetitionRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | executable list/tournament mutation plan frozen | final CI only |
| `best.b.p()` | `aj` annual player sweep | `LegacyAnnualPlayerProgressionSweepRules` + senior/junior runtimes | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | senior-before-junior ordering; both substantive sides owned | final CI only |
| `best.o.e()` | senior annual progression | `LegacyAnnualSeniorProgressionRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | club-null path, growth/decline routing and clear-`M` composition | final CI only |
| `best.o.t()` | decline | `LegacyAnnualSeniorDeclineRules` | `IMPLEMENTED_AND_CERTIFIED` | exact retained `N`, weights, threshold/floor behavior | none |
| `best.o.s()` accumulation | growth | `LegacyAnnualSeniorGrowthAccumulationRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | complete base matrix/modifiers from SMALI | final CI only |
| `best.o.s()` target/cap | growth | `LegacyAnnualSeniorGrowthTargetRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | complete target matrices/final min | final CI only |
| `best.o.s()` high-`d0` RNG | growth | `LegacyAnnualRandomRules.bestOSApplyHighD0CapAdjustment` | `IMPLEMENTED_AND_CERTIFIED` | exact one-draw branch; no seed-parity claim | none |
| `best.o.s()` finalization | growth | `LegacyAnnualSeniorGrowthFinalizationRules` | `IMPLEMENTED_AND_CERTIFIED` | strict threshold, one-point cap, retained-N reset behavior | none |
| senior `M` | match/lineup → annual progression | `CareerPlayerRuntimeEntity.legacyAnnualM` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | V15 nullable migration, match writers, annual read/clear, fail-closed historical unknown | final CI only |
| senior `N:double` | senior growth/decline | `CareerPlayerRuntimeEntity.legacyAnnualN` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | V15 nullable migration, atomic progression/reopen | final CI only |
| senior payroll `n:int` | `m0()` → payroll | `CareerPlayerRuntimeEntity.legacyRawPayrollN` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | V15 nullable migration + exact recomputation boundary | final CI only |
| senior `d/W0` | player flag/runtime | existing `CareerPlayerRuntimeEntity.worldTop` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | official SMALI proves `d`; no duplicate column introduced | final CI only |
| senior `j0:int transient` | annual counter/reset | transient runtime only | `TRANSIENT_RUNTIME_STATE_CLOSED` | official field is `transient`; `d1/j0` access frozen | never persist solely for compatibility |
| `best.b.F()` | final annual route | `LegacyAnnualFResetRules` + `LegacyAnnualTournamentEntryResetRules` + player reset rules | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | three-pass order, first-`z0` quirk, k0 traversal and player flags closed | final CI only |
| `best.o.d1(0)` | `F()` pass 2 | `LegacyAnnualPlayerD0Rules.resetGlobalCounter()` | `IMPLEMENTED_AND_CERTIFIED` | exact reset/idempotence | none |
| `best.o.D0()` | `F()` pass 3 | `LegacyAnnualPlayerD0Rules` | `IMPLEMENTED_AND_CERTIFIED` | increment-before-guards + code/threshold matrix | none |
| `best.k0.c(index)` + `components.n1` + `best.h0` | `F()` pass 1 | `LegacyAnnualTournamentEntryResetRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | exact `[0,1,2,2,5,6,6,3,3,4,4]`, U-sort, D0 threshold, unique selection, paired clubs, k0.i first eligible, conditional `o1(TRUE)` | final CI only |
| `F2(true)` / `M0` | original `P0()==0` route | router flag only | `CHARACTERIZED_NO_INDEPENDENT_STATE` | no separate durable owner justified | none |
| `g4()` → `components.n3` → `best.f` | annual automatic selection | `LegacyAnnualBestFSourceRules` + `LegacyAnnualBestFExecutionRules` | `IMPLEMENTED_AND_TESTED_PENDING_FINAL_CI` | exact source order, q/n/p routes, `Z0/A1/mode2`, D0 marking, fallback and draw policy | final CI only |
| `T1(target,value,true,false,false)` | selected transfer mutation | `LegacyAnnualG4TransferExecutionRule` | `IMPLEMENTED_AND_CERTIFIED` | exact flags/contract/financial effects | none |
| junior tryout `best.b.h2(c0)` | `ActivityJuniores.j()` | `CareerJuniorRuntimeStore.runTrial()` | `IMPLEMENTED_AND_CERTIFIED` | Fase 15.1 | none |
| `best.p.b()` junior development | youth annual | `LegacyJuniorRuntimeRules.progressDevelopment()` + V14 draft | `IMPLEMENTED_AND_CERTIFIED` | Fase 15.1 | none |
| manual `best.t.e(FALSE,p,c0)` | manual junior promotion | `CareerJuniorManualPromotionStore.promote()` | `IMPLEMENTED_AND_CERTIFIED` | Fase 15.1 | none |
| annual `best.p.c(c0)` + `best.t.e(TRUE,p,c0)` | junior annual lifecycle | `CareerJuniorAnnualLifecycleStore.run()` | `IMPLEMENTED_AND_CERTIFIED` | Fase 15.1 | none |
| `ActivityJuniores` | UI surface | substantive seams above | `PRESENTATION_ONLY` | gameplay/runtime side certified | Phase 17 UI only |
| `ActivityFimAno.e()` | end-year UI entry | route into the composed annual lifecycle | `CHARACTERIZED_NO_INDEPENDENT_STATE` | no remaining substantive child gap in Phase 15 | Phase 17 presentation only |
| `best.c0.y()` | manager replacement flows | Marco B manager runtime | `IMPLEMENTED_AND_CERTIFIED` | Fase 14 | none |

## Persistência Room V15

V15 é o delta mínimo provado para o runtime senior:

- `legacyAnnualM INTEGER NULL`;
- `legacyAnnualN REAL NULL`;
- `legacyRawPayrollN INTEGER NULL`.

`MIGRATION_14_15` é apenas aditiva: três `ALTER TABLE`, sem `UPDATE`, sem backfill, sem default e sem `fallbackToDestructiveMigration`. Saves V14 preservam desconhecido histórico como `NULL` e os stores falham de forma fechada quando reconstrução segura é impossível. `best.o.d/W0` reutiliza `worldTop`; `best.o.j0` permanece transitório.

## Contagem de gaps materiais do Marco C

- `REACHABLE_NOT_IMPLEMENTED` material: **0**
- `UNKNOWN_NEEDS_INVESTIGATION` material: **0**
- `PERSISTENT_RUNTIME_GAP_PROVEN` da Fase 15: **0**
- `SMALI_REQUIRED` material não resolvido: **0**
- alterações de dados esportivos no diff do PR #16: **0**
- review comments conhecidos no checkpoint de fechamento: **0**

Esses números não dispensam CI: a promoção depende dos três workflows obrigatórios no exact FINAL_HEAD.

## Handoff explícito para a Fase 16 — não são gaps funcionais da Fase 15

A Fase 16 deve auditar durabilidade/save-load/reopen/corrupção e ownership normalizado de agregados serializáveis do legado, incluindo quando materialmente necessário:

- `best.k0` / `components.n1` / `best.h0` e coleções derivadas;
- `konrent.a0` / `konrent.f0` e estado de rodada/fixture construído pelo bootstrap;
- equivalência global de load/save após múltiplos ciclos anuais.

A Fase 15 já congela a **função alcançável e a construção/mutação** desses fluxos. A Fase 16 decide e prova a forma durável correta; isso não autoriza reabrir regras esportivas já caracterizadas nem inventar colunas sem evidência.

## Documentos históricos superseded

Os documentos abaixo preservam evidência útil, mas suas classificações de “gap aberto” não representam mais o estado atual:

- `PHASE15_MATRIX_CONSOLIDATION_CHECKPOINT.md`;
- `PHASE15_CLOSURE_SPRINT.md`;
- `PHASE15_ANNUAL_F_RUNTIME_GAP_AUDIT.md`;
- `PHASE15_ANNUAL_SENIOR_M_PERSISTENCE_AUDIT.md`;
- `PHASE15_ANNUAL_SENIOR_PROGRESSION_GAP.md`;
- `PHASE15_ANNUAL_J2_REMAINING_CALLEE_EVIDENCE.md`.

Eles não devem ser apagados: ficam como trilha histórica. Esta matriz é o índice normativo de estado do Marco C.

## Gate de encerramento

Antes de declarar a Fase 15 concluída:

1. revalidar o HEAD remoto e o PR #16;
2. manter esta matriz com zero gap material alcançável;
3. confirmar 0 review threads/blockers materiais;
4. auditar o diff contra `phase4/core-game-domain` para dados esportivos e destructive migration;
5. exigir no **mesmo FINAL_HEAD**:
   - `Phase 7 Validation` — `SUCCESS`;
   - `Phase 8 Validation` — `SUCCESS`;
   - `Phase 8 Final Certification` — `SUCCESS`;
6. somente depois promover o Marco C/Fase 15 e avaliar a entrada na Fase 16.

# Manager method evidence — official Brasfoot 2026/27 corpus

Status: **ACTIVE / AUTHORITATIVE FOR MARCO B**

Official Phase 4R corpus: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`. Historical Phase 1 `com.brasfoot.v2028` names remain historical only.

## Current manager anchors

| Role | Java/decompiler method | SMALI method | Useful instructions | Branches |
|---|---|---|---:|---:|
| stadium host | `ActivityEstadio.onCreate(Bundle)` | `ActivityEstadio.onCreate(Landroid/os/Bundle;)V` | 259 | 11 |
| tactics host | `DialogTatics.onCreate(Bundle)` | same | 171 | 19 |
| lineup commit host | `ActivityEscalacao.B()` (bytecode `y`) | `ActivityEscalacao.y()V` | 212 | 22 |
| player search proposal | `ActivityProcura.t(best.o,best.c0,int)` | same descriptor | 136 | 14 |
| club-selection validation | `ActivityEscolhaTimes.i(String)` | same descriptor | 38 | 9 |
| player-info host | `DialogIgrokInfo.onCreate(Bundle)` | same | 530 | 28 |
| team proposal | `ActivityTimes.s(best.o,best.c0,int)` | same descriptor | 133 | 14 |
| career-club hub | `ActivityMainTeam.onStart()` | same | 93 | 15 |
| saved tactics | `ActivitySavedTatics.g()` | `g()V` | 103 | 8 |
| club invitation acceptance | `ActivityConvite.onClickAccept(View)` | same descriptor | 39 | 8 |
| manager transfer dispatcher | `best.b.G(best.c0,best.f0,best.f0)` | `G(Lbest/c0;Lbest/f0;Lbest/f0;)V` | 5 | 2 |
| manager leaves club | `best.f0.l(best.f0)` | `l(Lbest/f0;)V` | 100 | 5 |
| manager joins club | `best.f0.e(best.c0)` | `e(Lbest/c0;)V` | 38 | 3 |
| replacement-manager resolver | `best.c0.y()` | `y()Lbest/f0;` | 103 | 22 |
| coach post-match statistics | `best.f0.j(best.s)` | `j(Lbest/s;)V` | characterized | characterized |
| coach post-match adjustment | `best.f0.i(best.s)` | `i(Lbest/s;)V` | characterized | characterized |
| competition raw subtype | `konrent.t.x0()` | `x0()I` | returns private `F` | producer chain characterized |
| relegation input | `konrent.t.R0()` | `R0()I` | returns private `K` | `LoadLigaOptions.nRebaixados` |

## Phase 11 characterized behavior

The official class is `ActivityEscalacao`, not historical `ActivityEscala`. The following behavior is now characterized directly from Java↔SMALI and represented by pure modern rules:

- `v()` + `best.o.K0()`: available/unavailable classification. The two legacy exclusion predicates remain opaque instead of being renamed speculatively. Contract time is checked only when mode is false, a club exists and its nullable `Q0` is true. Mode appends converted auxiliary entries. Available players sort by skill then energy descending (`f3.w`); unavailable players use the established position/subrole/skill/star sort (`f3.s`).
- `I()` + `components.y3.e/c`: automatic formation uses exact `best.j0.Z1/c2/e2` tables, progressive position/side/subrole relaxation, source-order selection and exact starter display-slot priority. `best.b0.g(...)` is side-effect free at this call and its unused return is intentionally not substituted for the actual `y3.e` loop.
- `J(g3)`: saved formation clamps its index only for `club.x1(0, ...)`, retains only player objects still present by identity in the eligible roster, requires both parallel lists to have size 11, then uses the raw formation index for the label path; an invalid raw index is therefore not silently normalized.
- `k()/l()`: bench selection uses exact preference slots `{1,2,4,4,12,15,15,20,20,23,23}` then appends remaining eligible players. `l()` compares `components.o1` wrappers against `best.o` player objects, so every unavailable player is appended in source order; this type-mismatch quirk is preserved.
- `Q()`: snapshots current starter player references + slot codes and the current formation index.
- `U/V/W`: bench reorder, starter↔bench and starter↔starter swaps preserve snapshot-write counts `0/1/2`.
- `x()`: only identity-eligible non-null starters with slot `1..25` count; at least 11 triggers `K()` and background `B()`. Otherwise it shows the select-players message; when global `D1` is active it literally writes formation `4`, immediately `0`, rebuilds with `I()` and retries.
- `B()` / SMALI `y()V`: club starters are cleared and rebuilt from non-null slots `1..25`; each gets `B1(slot)` and `s1(TRUE)`. Club bench is cleared and rebuilt only from identity-eligible players, assigning contiguous `B1` codes starting at 26; bench receives no `s1(false)` write. For `q==0`, match lists `u0/w0/y0` are replaced; for `q==1`, `v0/x0/z0` are replaced; other `q` values leave match-side lists untouched. Final order preserves `club.E1(true)`, optional finish of `ActivityMainTeam.I`, clearing that static activity, `G=true`, `H=false`, `best.n.i()`, then finishing lineup.
- `DialogTatics`: `onCreate`, `j`, `k`, special-player cleanup and `v2` picker are fully characterized; exact keys are `cap`, `bFaltas`, `bEscanteios`, `fNove`, and `Q0()==false` blocks mutations.
- `ActivitySavedTatics.g/e/b/f` and `ActivityEscalacao.onActivityResult(101,-1)`: saved tactic create/list/delete/load/result behavior is characterized, including upper-bound-only index guards and preserved negative-index failure behavior.

All three Phase 11 evidence hosts (`ActivityEscalacao.B()`, `DialogTatics.onCreate`, `ActivitySavedTatics.g`) are semantically characterized. Aggregate Marco B remains Draft until the Fases 11–14 gate is certified together.

## Phase 14 employment, replacement and continuation

The manager employment and replacement chain is no longer blocked at `best.c0.y()`:

- `best.b.G(target,outgoing,incoming)` is an ordered dispatcher: `outgoing.l(incoming)` when outgoing is non-null, then `incoming.e(target)` when incoming is non-null.
- `best.f0.l(incoming)` appends manager-change history with current career Y/M/D and previous club legacy ID, captures previous club/country/division (`divisionValue - 1`), performs the user-controlled departure branch when `K()==true`, optionally resets `T.A(0)`, clears the club manager, and clears the manager current club. Country code `29` additionally writes global `H3(state)`; every user departure writes `I3(country)`. ArrayList-like first-match removal is preserved.
- `best.f0.e(target)` assigns manager↔club references, writes raw manager values `G=100`, `H=80`, `M=0`, and calls `target.j1()`. For user-controlled managers it also preserves the proved club-control, roster and youth-reset side effects as explicit effects rather than inferred replacements.
- `best.c0.y()` replacement selection has been characterized and promoted through the deterministic modern boundary. All legacy random decisions used by the promoted path consume `CareerState.random` through `CareerManagerProgressionRandomStore`; RNG state is persisted atomically, and rollback/reopen regressions prevent hidden `Random`, timestamp or collection-order substitutes.
- dismissal continuation from `best.n.l()/m()` is characterized and connected to the same manager runtime; no alternate modern dismissal path is invented.
- V9 `career_manager_ticket_runtime.rawH` remains authoritative for the previously proved `H` input. V11 `career_coach_runtime` and `career_coach_season_club_records` persist the coach state and ordered season-club records. V11 rematerialization preserves the already certified anti-cascade behavior.

## Phase 14 post-match `f0.j()/f0.i()` evidence

The reachable post-match chain is characterized from official Java plus SMALI, with the SMALI result controlling any decompiler ambiguity.

### Caller and branch contract

- The normal persisted match path resolves the manager identities attached to the home and away clubs in legacy world order before applying coach mutation.
- Competition type `7` applies `f0.j()` and excludes `f0.i()` exactly as proved by the legacy branch.
- Reachable competition types `{1,2,3,4,5,6,8}` keep the `f0.j()` path and enter `f0.i()` only through the characterized legacy branch set; unsupported/presentation-only paths are not synthesized.
- If neither match club has an attached persisted manager, coach post-match handling is an exact no-op. Missing unrelated V9 club-manager rows must not turn a no-coach match into a failure.
- Once a manager is actually attached, every state/input consumed by the promoted path is required losslessly; missing evidence fails closed before the atomic match commit.

### Exact `f0.i()` inputs

1. **Club strength (`c0.f0()`)**
   - `best.c0(e.t)` calls `w1(tVar.getNivel())`.
   - `w1(int)` keeps values `1..25` and uses the legacy fallback only outside that range.
   - the modern import chain is lossless: `LegacyTeamSnapshot.level → ClubDataV1.level → ClubEntity.level`.
   - therefore the runtime reads `ClubEntity.level`; no recalculated rating or external sporting data is allowed.

2. **Relegation count (`c0.I()` / `konrent.t.R0()`)**
   - `konrent.t.R0()` returns private `K`.
   - constructor evidence initializes `K = LoadLigaOptions.nRebaixados`.
   - Room V12 persists this exact raw value as nullable `career_competitions.legacyRelegationCount`.
   - migration `11→12` only adds the nullable column. It has no default, UPDATE, INSERT or invented backfill; old states with no exact source remain fail-closed.

3. **Competition subtype (`konrent.t.x0()`)**
   - `x0()` returns private `F`.
   - Java+SMALI producer tracing shows `F` is the raw competition-instance subtype/ordinal propagated by the legacy constructors; continuations inherit an existing `x0()` and `best.x.Z()` creates the league instances in the proved ordinal sequence.
   - it is **not** equivalent to modern `legacyFormatCode`, season number, table position or a reconstructed division label.
   - Room V13 persists the exact raw value separately as nullable `career_competitions.legacyLeagueSubtype`.
   - migration `12→13` is additive and performs no backfill. A V12 save without exact `x0()` stays NULL and fails closed only when a reachable coach path actually needs it.

4. **Standing/cash conditionals**
   - standing position and table size are read from the persisted pre-match competition table, preserving the call order before the match result is committed to standings.
   - `legacyRelegationCount` stays independent from table size/position; it is never recalculated.
   - club cash is resolved only on characterized `f0.i()` branches that actually consume it; missing required cash fails closed rather than substituting a default.

### Atomic integration and regressions

`CareerCoachPostMatchPersistedResolver` feeds the same `CareerMatchAtomicCommitter` used by persisted match/RNG/player/finance writes. The promoted chain therefore has one transaction boundary for the state that is mutated together.

Required regressions include:

- `CareerCoachPostMatchPersistedResolverTest`: exact type-1 inputs, `j → i` ordering, missing `nRebaixados`, rejection of `legacyFormatCode` as an `x0()` shortcut and no-manager no-op.
- `CareerMatchExecutionCoordinatorTest`: type-7 persisted post-match/reopen, fail-closed missing V11 coach, type-1 end-to-end commit/reopen, and type-1 missing-`x0()` rollback of match, career/RNG, schedule and coach state.
- `CareerCompetitionStoreTest`: raw V12/V13 competition inputs survive persistence and reopen.
- `Migration12To13Test` plus the historical migration chain: V13 adds no invented subtype to old careers.
- `V1AdaptersIdentityTest`: `e.t` team level remains identical through the V1 adapters to `ClubEntity.level`, and the ordered migration registry is pinned through V13.

The Phase 7 gate explicitly requires the V13 migration and Phase 14 persisted resolver/coordinator evidence; Phase 8 and Phase 8 Final still require the complete unit-test set to have zero failures/errors/skips before certification.

## Phase 14 closure state

Implementation state: **FINAL_HEAD CANDIDATE — certification must be established by CI on the exact SHA containing this document.**

The reconstructed runtime now covers the characterized career/manager surfaces needed by Fase 14: user career/controlled club state, manager identity/employment, replacement, dismissal continuation, deterministic manager RNG, coach state persistence, and reachable post-match progression including `f0.i()` inputs with fail-closed V12/V13 persistence. This document does not waive the repository rule that all mandatory workflows must be green on the same exact SHA before Fase 14 is called certified.

## Behavioral rule

Structural recovery alone never unlocks gameplay. Promotion requires characterized inputs, branches, state mutation, ordering, persistence and tests. No external sporting facts or inferred gameplay semantics are introduced here.

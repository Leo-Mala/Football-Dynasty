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

All three Phase 11 evidence hosts (`ActivityEscalacao.B()`, `DialogTatics.onCreate`, `ActivitySavedTatics.g`) are now semantically characterized. This closes the legacy evidence blocker for Fase 11; aggregate Marco B remains Draft until Fases 12–14 and the full manager-loop gate are certified together.

## Phase 14 characterized employment transition

The direct employment mutation reached by club-invitation acceptance is now characterized separately from replacement-manager selection:

- `best.b.G(target,outgoing,incoming)` is only an ordered dispatcher: `outgoing.l(incoming)` when outgoing is non-null, then `incoming.e(target)` when incoming is non-null.
- `best.f0.l(incoming)` appends manager-change history with current career Y/M/D and previous club legacy ID, captures the outgoing manager's previous club/country/division (`divisionValue - 1`), performs the user-controlled departure branch when `K()==true`, optionally resets `T.A(0)`, clears the club manager, and clears the manager's current club. Country code `29` additionally writes global `H3(state)`; every user departure writes `I3(country)`. The world controlled-club collection remains ArrayList-like: removal removes only the first matching club entry.
- The user-controlled departure branch preserves ordered helper effects `club.t1(false)`, `club.j1()`, `club.k1()`, first-match removal from `world.g1()`, and `manager.R(null)`. Helpers whose deeper state is outside the current modern boundary remain explicit typed effects rather than invented replacements.
- `best.f0.e(target)` assigns manager↔club references, writes raw manager values `G=100`, `H=80`, `M=0`, and calls `target.j1()`. When incoming manager `K()==true`, it marks the club controlled, appends it to `world.g1()`, calls `N1()`, applies player `c(180,true)` across the roster, and calls `c1()` (which includes youth reset/recreation in the legacy code).
- `best.c0.y()` is recovered at `103` useful instructions / `22` branches but remains **not promoted**. It chooses replacement managers through `best.b.t`, `best.b.B` and related paths that use legacy `Random.nextInt(100)` and `Collections.shuffle`. Those paths must be migrated through the project `RandomSource` with exact draw/order characterization before the replacement resolver can leave the fail-closed boundary.

Therefore `CLUB_MANAGER_TRANSFER_G_L_E` is characterized, while the whole club-invitation/career-progression surface remains blocked pending `best.c0.y()` plus downstream `best.n.l()/m()` continuation/dismissal semantics.

## Behavioral rule

Structural recovery alone never unlocks gameplay. Promotion requires characterized inputs, branches, state mutation and ordering. No external sporting facts or inferred gameplay semantics are introduced here.

# Phase 13 — Coach raw H lifecycle evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

Status: **CHARACTERIZED / PERSISTENCE NOT YET PROMOTED**

## Why this field matters to Phase 13

`best.k.b(best.s)` computes stadium attendance using the home club manager expression `home.y0() != null ? home.y0().o() / 100.0 : 0.80`. SMALI confirms `best.f0.o()` returns raw integer field `H`.

Therefore ticket finance cannot safely substitute a constant `80` after the career has advanced. The same raw field is mutated by reachable manager progression paths.

## Initialization / employment

The `best.f0` constructor initializes `H = 80`.

`best.f0.e(best.c0)` also writes `H = 80` when a manager joins a club, alongside the already-characterized `G = 100` and `M = 0` writes.

`LegacyCoachRawHRule.initialValue()` and `afterEmployment()` preserve this exact reset value.

## Post-match update — `best.s.f()` → `best.f0.i(best.s)`

`best.s.f()` calls each participating club manager's `i(match)` only when an actual competition exists and `competition.E()` is one of:

`1, 2, 3, 4, 5, 6, 8`

Type `7` is not included. A missing competition also consumes no H update.

Inside `best.f0.i(best.s)`, the manager's own goals/opponent goals and home/away side are selected by identity against the match clubs. The H-only result branch is:

| Result | Home | Away |
|---|---:|---:|
| draw | `h(0)` | `h(+2)` |
| win, margin < 3 | `h(+3)` | `h(+5)` |
| win, margin >= 3 | `h(+3)` then `h(+3)` | `h(+5)` then `h(+7)` |
| loss, margin < 3 | `h(-5)` | `h(-3)` |
| loss, margin >= 3 | `h(-5)` then `h(-5)` | `h(-3)` then `h(-2)` |

`best.f0.h(int)` adds the delta and immediately clamps H to `0..100`. The multiple-write branches therefore remain sequential rather than being collapsed into one invented normalized update.

`LegacyCoachRawHRule.afterMatch(...)` reconstructs only this proven H projection. Other `best.f0.i` fields/effects remain outside this rule.

## Annual recovery — `best.b.s()`

During the reachable annual reset, `best.b.s()` iterates every club. When `club.y0() != null`, it calls:

- `club.y0().g(50)` for another raw manager field;
- `club.y0().h(50)` for H.

The H path is therefore exact `+50` through the same immediate `0..100` clamp. `LegacyCoachRawHRule.afterAnnualRecovery(...)` represents this isolated proven effect.

## ActivityMainTeam floor quirk

`ActivityMainTeam.F()` reads both manager progress values for the club hub. When its legacy Boolean `h` flag is true and `manager.o() < 30`, it invokes `manager.N(30)`; SMALI confirms `N(int)` is a direct write to H.

The same method independently floors the other displayed manager field with `M(30)`. The H branch is:

`ActivityMainTeam.h == true && manager.H < 30 -> manager.N(30)`

The flag can be set when the current match competition code held by the screen is `7` or `9`, and during activity initialization when the selected club's manager points back to that same club. This is an observable legacy UI-triggered state mutation, not presentation-only formatting.

`LegacyCoachRawHRule.afterMainTeamRefresh(rawH, legacyFloorEnabled)` preserves only the proven H mutation. It does not invent when a modern screen is opened.

## Persistence consequence

V8 currently persists club finance, stadium sectors and manager employment-facing state, but does not contain durable per-club/per-manager H.

A future persistence promotion must preserve all proven sources above:

1. fresh source club manager starts H at `80`;
2. manager employment resets H to `80`;
3. eligible match completion applies the exact home/away/result/margin sequence;
4. annual reset applies `+50` with clamp;
5. the reachable main-team hub can floor H to `30`.

Existing V8 careers cannot be assigned `80` during migration without inventing state, because matches/annual progression may already have changed H. Any schema promotion must therefore remain fail-closed for previously advanced careers unless a lossless current value is available.

## Phase 13 boundary after this characterization

Ticket calculation may now rely on an exact pure H lifecycle rule, but the match ticket caller must continue supplying the current H from a characterized runtime boundary until durable H materialization is added. This prevents a false-green implementation that silently resets manager H to `80` for every match.

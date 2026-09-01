# Phase 13 — Coach raw H lifecycle evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

Status: **CHARACTERIZED / V9 PARTIALLY PROMOTED / POST-MATCH STILL FAIL-CLOSED**

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

**Promotion boundary:** the H-only post-match projection is deliberately not wired to `CareerMatchAtomicCommitter` yet. `best.f0.i(best.s)` / `best.f0.j(best.s)` must be reconstructed field-by-field from Java↔SMALI before the production post-match mutation is persisted. Persisting only H would create a false partial lifecycle and is prohibited by the project parity contract.

## Annual recovery — `best.b.s()`

During the reachable annual reset, `best.b.s()` iterates every club. When `club.y0() != null`, it calls:

- `club.y0().g(50)` for another raw manager field;
- `club.y0().h(50)` for H.

The H path is therefore exact `+50` through the same immediate `0..100` clamp. `LegacyCoachRawHRule.afterAnnualRecovery(...)` represents this isolated proven effect.

V9 now persists this independently complete H slice through `CareerTicketRuntimeStore.applyCoachAnnualRecovery(...)`. The store mutates the first matching manager in legacy world order, preserves duplicate legacy manager IDs, fails closed for an absent/unmaterialized manager, and writes inside a Room transaction. Reopen coverage proves the resulting value is durable.

## ActivityMainTeam floor quirk

`ActivityMainTeam.F()` reads both manager progress values for the club hub. When its legacy Boolean `h` flag is true and `manager.o() < 30`, it invokes `manager.N(30)`; SMALI confirms `N(int)` is a direct write to H.

The same method independently floors the other displayed manager field with `M(30)`. The H branch is:

`ActivityMainTeam.h == true && manager.H < 30 -> manager.N(30)`

The flag can be set when the current match competition code held by the screen is `7` or `9`, and during activity initialization when the selected club's manager points back to that same club. This is an observable legacy UI-triggered state mutation, not presentation-only formatting.

`LegacyCoachRawHRule.afterMainTeamRefresh(rawH, legacyFloorEnabled)` preserves only the proven H mutation. It does not invent when a modern screen is opened.

V9 now persists this independently complete H slice through `CareerTicketRuntimeStore.applyCoachMainTeamRefresh(...)`. The caller must still supply the already-characterized legacy floor flag; the persistence layer does not invent a modern-screen trigger. Disabled-floor behavior is an exact no-op and reopen coverage proves the enabled mutation is durable.

## V9 persistence consequence

V9 contains durable career-scoped manager runtime rows with legacy world order, numeric legacy manager identity and raw H. The schema promotion is additive and fail-closed.

Migration V8→V9 intentionally does **not** synthesize `H = 80` for existing careers. A V8 career may already have advanced through matches, annual progression or the main-team floor path, so assigning 80 during migration would invent state.

The current V9 boundary therefore preserves these rules:

1. fresh source manager state may materialize the proven constructor value `80` only when that source state is actually being created;
2. manager employment resets H to `80` only through the characterized employment transition;
3. annual reset `+50` with immediate clamp is persistable now because that slice is independently complete;
4. the reachable main-team floor to `30` is persistable now because that slice is independently complete and its trigger remains explicit;
5. post-match H is **not** persisted yet, despite the pure H projection being characterized, because `best.f0.i/j` contain additional effects that must be reconstructed and committed together when the legacy ordering requires it;
6. ticket input resolution reads only materialized V9 manager H and fails closed when the required state is unavailable; it never substitutes a constant 80.

## Remaining Phase 13/14 boundary

Ticket calculation now resolves current manager H from persisted V9 state. Annual recovery and the main-team floor are durable. The remaining manager blocker is specifically the complete post-match lifecycle:

`best.s.f()` → eligibility/order → `best.f0.i(best.s)` / `best.f0.j(best.s)` → all field mutations/effects → persistence ordering.

Until that Java↔SMALI reconstruction is complete, no H-only production post-match write may be added. This keeps the manager lifecycle fail-closed instead of falsely claiming parity from one extracted field.

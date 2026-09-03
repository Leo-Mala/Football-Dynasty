# Phase 13 — Ticket finance evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven call chain

`best.k.n(best.s)` → `match.H().b(match)` → `best.k.b(best.s)` → `best.s.f1(int[4])` + `best.s.h1(int)` → later post-match `best.s.h()` → `home.B(ticketIncome, 5)` → `best.m.a(ticketIncome, 5)`.

`best.s.h()` skips the credit when `competition.E()` is `7` or `5`, and also skips it when the home club `Q0()` flag is false. Otherwise `best.c0.B` increments cash without a balance clamp and routes category `5` to the ticket-income accumulator.

## `best.k.b` structural fingerprint

The calculation consumes the stadium's four raw capacities independently. It cannot be reconstructed from a single aggregate capacity after career mutations without losing behavior.

For each match it:

1. normalizes a missing/wrong-length capacity vector to `[1000,30000,1000,1000]`, and negative individual capacities to zero;
2. seeds four attendance values from a six-row table selected by home `p0()` (invalid → `3`);
3. applies competition-type multiplier `0.7` for type `3` or `0.4` for type `0`;
4. adds a capacity share: normally `0.30`, `0.45` when `match.A() instanceof konrent.a0`, plus `0.30` for type `4` or plus `0.15` for types `6/8`;
5. adjusts all sectors by the absolute away/home `p0()` gap using `[0,.05,.10,.15,.20,.25]`, preserving the raw unchecked array-index behavior;
6. applies home `y0().o()/100`, or `0.80` when `y0()==null`;
7. performs sector-ordered RNG draws using the home `O()` row (invalid → `3`), consuming no draw only where that sector's bound is zero;
8. clamps each attendance to `[0, sectorCapacity]`;
9. selects the exact `best.j0.z0` price row according to competition type and raw home fields;
10. computes `sum(attendance[i] * price[i])`, stores attendance in the match and stores gross ticket income for the later finance credit.

SMALI confirms the Java structure and exact table values, including the type `6/8` second price row `[20,25,40,120]`.

## Persisted/source provenance of every input

The modern resolver now has a durable provenance for every characterized input:

- **competition type** — single persisted match→competition link → `CareerCompetitionEntity.legacyCompetitionType`;
- **home/away `p0()`** — immutable official `.ban` reputation replayed by `LegacyTicketClubSourceRule` with the legacy read clamp;
- **home `J()`** — official club country projected through the exact 221-row country-group table;
- **home `Q0()`** — `CareerClubManagerRuntimeEntity.active`;
- **four stadium capacities** — V8 `career_stadium_runtime`;
- **home `O()`** — V9 `career_club_ticket_runtime.rawDivisionCode`, preserving the career-mutable raw value instead of source `level`;
- **home manager identity / `H`** — V9 club numeric manager id plus ordered `career_manager_ticket_runtime`; duplicate ids are allowed and the first source ordinal wins exactly like `best.b.b1(id)`. Stored `-1` means no manager; any other dangling id fails closed;
- **`match.A() instanceof konrent.a0`** — V9 `career_match_construction_source`, mapped from the proven constructor origin: `LEAGUE_T=false`, `KNOCKOUT_F0=true`, `FRIENDLY_A=false`.

Room V9 is additive and fail-closed. Migration 8→9 creates these state tables without synthesizing rows for old careers. See `PHASE13_TICKET_RUNTIME_V9_EVIDENCE.md`.

## Proven initial sector materialization

`best.c0.c(String,int)` constructs `new best.k(stadiumName, aggregateCapacity, club)`, whose constructor immediately computes four sectors:

1. aggregate outside `1000..120000` becomes `10000`;
2. sector 0 = `round(capacity * 0.15)`;
3. sector 2 = `round(capacity * 0.09)`;
4. sector 3 = `round(capacity * 0.009)`;
5. sector 1 = exact remaining aggregate;
6. sector caps `[18000,80000,9000,700]` are applied independently without redistribution.

`CareerStadiumRuntimeStore.materializeFromSourceClub(...)` replays this only for newly initialized proven state. V7→V8 never backfills old careers because past per-sector expansions cannot be inferred from immutable aggregate capacity.

## Match/RNG order and atomic integration

SMALI `best.s.Q0()` proves that stadium attendance/ticket calculation runs before later match RNG sites. The later `best.s.h()` only credits the already-computed gross and consumes no RNG.

The modern order therefore is:

1. resolve every ticket input from V9/source persistence and require home finance + four-sector stadium state;
2. `LegacyTicketFinanceRule.calculate(...)` consumes the career `RandomSource` before match simulation;
3. match simulation uses that same already-advanced `RandomSource`;
4. after simulation, `LegacyTicketFinanceRule.applyHomeTicketIncome(...)` applies the type/Q0 credit gate without consuming RNG;
5. `CareerMatchAtomicCommitter` commits score, calendar, RNG, player effects and ticket finance inside one Room transaction.

`CareerMatchTicketRngOrderTest` locks the order. For raw `O=0`, bounds `[10,20,5,0]` require exactly three ticket draws before the match callback; a fourth callback draw must be present in persisted career RNG state.

No secondary RNG, guessed manager H, immutable-level substitution or inferred parent class is used.

## Next manager lifecycle seam

Ticket demand consumes the manager's **pre-match** raw `H`. Post-match legacy flow is separate: `best.s.g()` invokes each match's `f(); i(); h(); e();`, and `best.s.f()` calls manager `j(match)` for both coaches and conditionally `i(match)` for competition types 1,2,3,4,5,6,8 before later ticket cash credit. Those `best.f0.i/j` methods mutate more than a simple ±1 H delta and must be characterized completely before V9 manager-state mutation is connected. Until that characterization is complete, ticket input persistence is valid but post-match manager mutation remains intentionally fail-closed/outside this checkpoint.

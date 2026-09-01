# Phase 13 — Ticket finance evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven call chain

`best.k.n(best.s)` → `match.H().b(match)` → `best.k.b(best.s)` → `best.s.f1(int[4])` + `best.s.h1(int)` → `best.s.h()` → `home.B(ticketIncome, 5)` → `best.m.a(ticketIncome, 5)`.

`best.s.h()` skips the credit when `competition.E()` is `7` or `5`, and also skips it when the home club `Q0()` flag is false. Otherwise `best.c0.B` increments cash without a balance clamp and routes category `5` to the ticket-income accumulator.

## `best.k.b` structural fingerprint

The calculation consumes the stadium's four raw capacities (`best.k.b:[I`) independently. It cannot be reconstructed from a single aggregate capacity without losing behavior.

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
10. computes `sum(attendance[i] * price[i])`, stores attendance in `best.s.j` and gross ticket income in `best.s.l`.

SMALI confirms the Java structure and exact table values, including the type `6/8` second price row `[20,25,40,120]`.

## Persistence consequence

The previous V7 schema stores only aggregate immutable source `ClubEntity.capacity` plus construction records. That is insufficient for this reachable legacy calculation because expansions and match revenue depend on each of the four sectors separately.

V8 therefore adds `career_stadium_runtime(careerId, clubId, sector0Capacity..sector3Capacity)` as additive career-local state. Migration V7→V8 intentionally creates no rows: no mathematically valid inverse exists from aggregate capacity to the four legacy sectors, so synthesizing them would invent gameplay. Materialization is explicit and fail-closed from proven source/runtime values.

## Remaining integration seam

The pure calculation and durable sector state are reconstructed here. Final match integration must execute this calculation with the same persisted career `RandomSource` used for the match and commit the resulting ticket finance mutation in the same Room transaction as the match result/RNG advance. A separate RNG or a post-commit finance write would violate `AGENTS.md` atomicity and is not permitted.

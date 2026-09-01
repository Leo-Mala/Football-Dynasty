# Phase 13 — Ticket finance evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven call chain

`best.k.n(best.s)` → `match.H().b(match)` → `best.k.b(best.s)` → `best.s.f1(int[4])` + `best.s.h1(int)` → `best.s.h()` → `home.B(ticketIncome, 5)` → `best.m.a(ticketIncome, 5)`.

`best.s.h()` skips the credit when `competition.E()` is `7` or `5`, and also skips it when the home club `Q0()` flag is false. Otherwise `best.c0.B` increments cash without a balance clamp and routes category `5` to the ticket-income accumulator.

## `best.k.b` structural fingerprint

The calculation consumes the stadium's four raw capacities (`best.k.b:[I`) independently. It cannot be reconstructed from a single aggregate capacity after career mutations without losing behavior.

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

## Proven persisted/source input provenance

Java + SMALI now close four of the non-stadium inputs that were previously caller-owned:

- **competition type** — `best.s.B()` returns the actual `best.k0` competition and ticket logic calls `B().E()`. The modern match→competition link already resolves a single `CareerCompetitionEntity`, whose `legacyCompetitionType` is the persisted `E()` projection.
- **home/away `p0()`** — `best.c0` initializes its raw reputation field from `e.t.getReputacao()`. `p0()` clamps that field to `[0,5]`. Modern immutable `ClubEntity.reputation` comes from the same official `.ban` source, so `LegacyTicketClubSourceRule` replays exactly this read-time clamp.
- **home `J()`** — SMALI confirms `best.c0.J()` evaluates `best.y.valueOf("P" + country).g()`. `ClubEntity.country` is the same serialized source country and `LegacyCountryAssetCodes.groupForLegacyCountry()` already contains the exact 221-row `best.y.g()` table.
- **home `Q0()`** — SMALI confirms `Q0()` returns the club Boolean control flag and `t1(Boolean)` writes it. The same flag is already represented by `CareerClubManagerRuntimeEntity.active`; this is also the flag used by the characterized legacy transfer finance path.

`CareerMatchTicketInputResolver` now resolves these four fields from persisted/source state and requires exactly one persisted competition link for a ticket-bearing match. Callers can no longer override them with arbitrary raw values.

Three inputs remain explicitly fail-closed because the V8 model does not yet prove their complete durable representation:

- **home `O()`** — `best.c0.O()` returns raw field `j`; `konrent.t.f1()` writes it with league field `F` through `c0.s1(F)`. This is career-mutable division state and is **not** the immutable `.ban` `level` field.
- **home coach `H`** — ticket logic reads `home.y0()?.o()`, where `best.f0.o()` returns mutable `H`. Constructors initialize `H=80`, manager employment resets it to `80`, and legacy match/year logic mutates it through `h(int)`/`N(int)`. Therefore using a constant `80` for every non-null coach would be incorrect.
- **parent `a0` class identity** — ticket logic tests `best.s.A() instanceof konrent.a0`, while `best.s.B()` is the actual competition. Current competition persistence retains type/format but does not yet prove a lossless parent runtime-class discriminator.

## Proven initial sector materialization

The corpus also proves how a fresh club obtains the four capacities. `best.c0.c(String,int)` constructs `new best.k(stadiumName, aggregateCapacity, club)`. The constructor immediately calls private `best.k.a(int)`.

`best.k.a(int)`:

1. replaces aggregate capacity outside `1000..120000` with `10000`;
2. computes sector 0 as `round(capacity * 0.15)`;
3. computes sector 2 as `round(capacity * 0.09)`;
4. computes sector 3 as `round(capacity * 0.009)`;
5. computes sector 1 as the remaining aggregate capacity;
6. individually caps the sectors at `[18000,80000,9000,700]` without redistributing capacity removed by those caps.

`LegacyStadiumInitialSectorRule` is the modern pure reconstruction. `CareerStadiumRuntimeStore.materializeFromSourceClub(...)` replays that constructor from immutable `ClubEntity.capacity` for newly initialized career state.

This does **not** change the V7→V8 migration rule: an already-running V7 career may contain stadium expansions that changed individual sectors, so the current sector vector cannot be reconstructed from the immutable aggregate source capacity. Migration therefore remains additive and fail-closed with no synthesized rows.

## Persistence consequence

V8 adds `career_stadium_runtime(careerId, clubId, sector0Capacity..sector3Capacity)` as additive career-local state. Match revenue reads this durable vector rather than splitting aggregate capacity heuristically.

## Match/RNG atomic integration

The modern match path now accepts only `CareerMatchTicketUnpersistedEvidence` for the three still-unmaterialized inputs. `CareerMatchTicketInputResolver` joins that evidence with the persisted/source fields above before calculation. Then:

1. persisted home finance state is required;
2. persisted four-sector stadium state is required;
3. the match simulation runs on the career `RandomSource`;
4. `LegacyTicketFinanceRule.calculate(...)` consumes the same `RandomSource`, using the persisted sector vector;
5. `LegacyTicketFinanceRule.applyHomeTicketIncome(...)` applies the proven type/Q0 credit gate;
6. `CareerMatchAtomicCommitter` commits score, calendar, career/RNG state, player effects and ticket finance inside one outer Room transaction;
7. `CareerManagerRuntimeStore.commitFinanceState(...)` keeps its expected-before stale-state guard inside that transaction, so a rejected finance mutation rolls the match/RNG writes back as well.

No secondary RNG and no post-match finance transaction are introduced.

## Remaining boundary

The unresolved ticket inputs are now only the three genuinely career-mutable/class-identity fields above: `O()`, coach `H`, and `A() instanceof konrent.a0`. They must be connected to durable modern state only after their complete legacy lifecycle/materialization is proved. Until then the boundary remains explicit rather than substituting source `level`, constant `80`, or a guessed competition-type mapping.

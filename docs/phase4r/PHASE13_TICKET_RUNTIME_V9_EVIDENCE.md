# Phase 13 — Ticket runtime Room V9 evidence

Official legacy evidence remains the already-certified ticket, club-division, manager-identity and match-parent characterization documents. This checkpoint changes persistence only; it does not add or reinterpret gameplay rules.

## Why V9 is required

The ticket formula consumes three values that cannot be reconstructed losslessly from V8 state:

- club `best.c0.O()` is mutable division state and is not immutable source `level`;
- club manager identity is the numeric `best.f0.G()` stored by the club, and ticket demand consumes that manager's mutable `H` through `y0()?.o()`;
- `match.A() instanceof konrent.a0` depends on the exact match-construction source, not the numeric competition type.

## Additive fail-closed schema

Room V9 adds exactly three tables:

1. `career_club_ticket_runtime(careerId, clubId, rawDivisionCode, legacyManagerId)`;
2. `career_manager_ticket_runtime(careerId, sourceOrdinal, legacyManagerId, rawH)`;
3. `career_match_construction_source(careerId, matchId, sourceCode)`.

Migration 8→9 creates the tables and indexes only. It inserts **no rows**. An existing V8 career therefore cannot silently acquire guessed division, coach or construction-source values.

The manager identity index `(careerId, legacyManagerId)` is intentionally non-unique. Legacy `best.b.b1(id)` scans an ordered `ArrayList` and returns the first matching manager, so duplicate ids remain observable and the persisted `sourceOrdinal` preserves that lookup order.

A stored manager id of `-1` resolves to no manager through the already-characterized `LegacyManagerIdentityRule.clubStoredManagerId(null)`. Any other stored id without a matching materialized manager fails closed instead of being converted into a fabricated default coach.

## Persistence tests

`CareerTicketRuntimeStoreTest` verifies:

- a negative raw division code survives reopen unchanged;
- duplicate manager ids survive and resolve the first source ordinal;
- manager raw `H` survives reopen;
- `KNOCKOUT_F0` construction source survives reopen;
- a non-absent dangling manager id fails closed;
- non-contiguous manager source order is rejected.

`Migration8To9Test` runs the explicit migration chain through V9 and verifies that all three new tables remain empty while existing career metadata survives.

## Room identity gate

The V9 schema identity was computed from Room's canonical entity identity algorithm before the branch moved and is pinned by CI:

`e246b6749364b3d2f7891177c2179fb4`

The Phase 7 gate still checks immutable V1/V2/V3 schemas, all pre-V9 table contracts, the complete explicit migration chain, the non-unique manager identity index and the exact V9 identity hash. No destructive fallback or weaker schema check was introduced.

# Phase 8 — transient legacy match runtime

Authoritative source remains `Brasfoot.apk_Decompiler.com.zip` with the Phase 8 SHA-256 recorded in the phase plan.

## Purpose

`LegacyMatchTransientRuntime` is the first mutable, persistence-independent application layer for the already-characterized Phase 8 rules. It does not introduce a second simulation algorithm. It executes the proven operation plans over reference-identity wrappers that correspond to the mutable legacy `best.s` / `best.o` match objects.

The runtime deliberately does not modify `domain.model.Player`, Room, career persistence, calendar state, or external sporting data.

## Reference identity

The legacy `best.o` class does not override `equals`. Match-list calls such as `ArrayList.remove(player)` therefore resolve the actual match object by reference identity. Runtime `Player` and `Club` wrappers are regular classes rather than Kotlin data classes so structurally equal wrappers remain distinct objects.

## Proven event application

`applyEvent(...)` composes the existing `LegacyMatchEventApplicationRules` with the already-recovered injury, player-club-season-stat, substitution, event-ledger, and score rules.

The runtime preserves:

- event append before downstream player mutations;
- type 2 -> player stat M;
- type 4 -> player stat N;
- type 3 -> stat M then stat N;
- dismissal removal before optional automatic substitution;
- injury resolution before the private legacy `best.o.g(5, club)` club/season statistic mutation;
- injury removal/substitution routing already characterized from `best.s.a(...)`;
- the legacy subtype-2 quirk where the event display primary can be replaced from the opposite active list while later effects continue targeting the original primary player;
- substitution event type 6, subtype -1, primary=outgoing, secondary=incoming, with recovered period/minute and side;
- exact valid-side `o1(...)` list/counter mutation order;
- score reconstruction from the event ledger rather than maintaining an independent invented score mutation path.

## Injury timestamp boundary

`best.o.m(club)` computes an absolute injury-until timestamp from the legacy career calendar when the final duration is positive. The transient runtime retains the fully characterized `LegacyMatchInjuryRules.Result` on the player but does not fabricate an absolute timestamp before the career-calendar adapter is wired. The recovered duration, skill mutation and `best.o.g(5, club)` statistic update are applied now; absolute date persistence remains an explicit integration boundary.

## Null/unknown club behavior

The runtime does not silently redirect a null injury club to home/away. Legacy `best.o.m(null)` reaches club-dependent code after applying injury calculations, so a null club is treated as an invalid application boundary instead of normalizing it. A non-null third club retains the recovered default side-0 routing where already represented by `LegacyMatchEventApplicationRules`.

## Tests

`LegacyMatchTransientRuntimeTest` covers:

1. goal ledger -> score reconstruction;
2. first yellow card count/stat mutation without removal;
3. second-yellow removal plus automatic substitution and emitted substitution event;
4. red-card removal with no substitutions available;
5. injury duration/skill + club-season stat + substitution composition;
6. subtype-2 display-primary replacement while effects remain on original primary;
7. reference-identity removal with structurally identical players;
8. null injury club not being silently redirected to the home side.

## Remaining integration boundary

The runtime is intentionally transient. The next Phase 8 step is to compose minute decisions/actions and r3 event materialization into this state, then adapt the resulting proven state/effects into the modern career flow. Room changes remain prohibited unless a proven persistent field cannot be represented by the existing schema.

# Fase 15 — `konrent.b0.V()` tournament bootstrap executable fragments

## Authority

Official corpus: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

The raw Library archive was materialized again for this checkpoint and its bytes were SHA-256 verified before reading `smali/konrent/b0.smali`. SMALI is authoritative over decompiled Java.

## Proven executable fragments

`konrent.b0.V()` first clears eight owned references (`o`, `D`, `E`, `F`, `H`, `G`, `J`, `I`). `LegacyTournamentBootstrapRules.clearOwnedReferences()` freezes that exact initial reset as a pure boundary: every one of the eight owned references is null before participant recovery begins. No Room state or RNG is involved in this reset.

### Prior competition participant recovery

The raw SMALI then performs six guarded source recoveries in this exact order:

1. `best.b.y0()` present → `E = y0.m(0)` → invoke `y0.t0(E)`;
2. `best.b.v0()` present → `F = v0.m(0)` → invoke `v0.v0(F)`; if that first `F` is non-null and `F.j0()==131`, replace only slot `F` with `v0.k0()`;
3. `best.b.x0()` present → `H = x0.m(0)` → invoke `x0.b0(H)`;
4. `best.b.w0()` present → `G = w0.m(0)` → invoke `w0.j0(G)`;
5. `best.b.z0()` present → `J = z0.m(0)` → invoke `z0.c0(J)`;
6. `best.b.B0()` present → `I = B0.m(0)` → invoke `B0.c0(I)`.

The callback is invoked whenever the source competition object exists, even if `m(0)` itself returns null. In the `v0` special case the callback remains anchored to the original `m(0)` participant and occurs before the `j0()==131` replacement.

`LegacyTournamentBootstrapRules.recoverPriorParticipants(...)` freezes this exact source-to-slot mapping and callback order as an opaque call plan. It deliberately does **not** assign gameplay semantics such as “remove champion” to the obfuscated callback methods; the executable method names and ordering are preserved without interpretation.

For the fallback candidate pass, the executable:

1. traverses `best.b.E0()` in source order;
2. copies only clubs whose raw `best.c0.J()` is strictly greater than `1` into a temporary `ArrayList`;
3. invokes `Collections.shuffle(...)` on that temporary list;
4. traverses the shuffled result and fills only still-null slots using the first encountered club with raw `J()==2`, `3`, `4`, or `5`;
5. stops scanning as soon as all four fallback slots (`G/H/I/J`) are non-null.

The modern `LegacyTournamentBootstrapRules.eligibleCandidates(...)` and `fillMissingTierSlots(...)` freeze only this deterministic projection around the already-shuffled order. They deliberately do not claim ownership of the legacy unseeded `Collections.shuffle` lifecycle.

When all six required participant references (`E`, `F`, `G`, `H`, `I`, `J`) are non-null, the executable creates the four-club array `[G,H,I,J]` and constructs exactly three index permutations:

- draw `0` → `[2,0,3,1]`;
- draw `1` → `[3,2,1,0]`;
- draw `2` → `[0,2,3,1]`.

The choice is made by a newly constructed local `java.util.Random` followed by `nextInt(3)`. `LegacyTournamentBootstrapRules.fourClubOrder(...)` freezes the draw-to-order mapping while intentionally leaving ownership of that local unseeded RNG unresolved.

After the chosen order is applied, `V()` builds `konrent.a0`, then `konrent.f0`, and invokes `f0.b(...)`. Those object-level competition constructors/state owners remain open and must be mapped before the substantive tournament bootstrap can be classified as implemented end-to-end.

## Regression

`LegacyTournamentBootstrapRulesTest` covers:

- exact clearing of all eight bootstrap-owned references before participant recovery;
- exact six-source participant-to-slot routing and callback order;
- callback emission for a present source even when `m(0)` is null;
- the `v0` raw `j0()==131` replacement occurring after and without retargeting its first-participant callback;
- strict raw `J()>1` eligibility while preserving pre-shuffle source order;
- first-match-only fallback for `J=2/3/4/5` without overwriting preexisting slots;
- the exact three `nextInt(3)` permutation mappings.

## Status

- initial eight-reference reset: **IMPLEMENTED_AND_TESTED**;
- prior competition participant recovery and opaque callback plan: **IMPLEMENTED_AND_TESTED**;
- fallback candidate/filter/tier-slot semantics: **IMPLEMENTED_AND_TESTED**;
- local draw-to-permutation mapping: **IMPLEMENTED_AND_TESTED**;
- `Collections.shuffle` compatibility ownership: **OPEN**;
- `konrent.a0`/`konrent.f0` state construction: **REACHABLE_NOT_IMPLEMENTED**;
- no Room/schema change is justified by these pure fragments.

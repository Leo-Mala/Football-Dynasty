# Fase 15 — `konrent.b0.V()` tournament bootstrap executable fragments

## Authority

Official corpus: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

The raw Library archive was materialized again for this checkpoint and its bytes were SHA-256 verified before reading `smali/konrent/b0.smali`. SMALI is authoritative over decompiled Java.

## Proven executable fragments

`konrent.b0.V()` first clears eight owned references (`o`, `D`, `E`, `F`, `H`, `G`, `J`, `I`). `LegacyTournamentBootstrapRules.clearOwnedReferences()` now freezes that exact initial reset as a pure boundary: every one of the eight owned references is null before participant recovery begins. No Room state or RNG is involved in this reset.

The executable then recovers some prior tournament participants through existing competition objects. Those recovery paths remain separate modern-ownership work and are not inferred here.

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
- strict raw `J()>1` eligibility while preserving pre-shuffle source order;
- first-match-only fallback for `J=2/3/4/5` without overwriting preexisting slots;
- the exact three `nextInt(3)` permutation mappings.

## Status

- initial eight-reference reset: **IMPLEMENTED_AND_TESTED**;
- fallback candidate/filter/tier-slot semantics: **IMPLEMENTED_AND_TESTED**;
- local draw-to-permutation mapping: **IMPLEMENTED_AND_TESTED**;
- `Collections.shuffle` compatibility ownership: **OPEN**;
- prior competition participant recovery and `konrent.a0`/`konrent.f0` state construction: **REACHABLE_NOT_IMPLEMENTED**;
- no Room/schema change is justified by these pure fragments.

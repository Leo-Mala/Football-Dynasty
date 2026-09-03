# Phase 13 — Match parent class identity evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Why this matters

`best.k.b(best.s)` increases the stadium-share component from `0.30` to `0.45` when `match.A() instanceof konrent.a0`. `best.s.A()` is the first `best.k0` constructor argument; `best.s.B()` is a separate actual-competition reference. The class test therefore cannot be replaced by `B().E()` or a guessed competition-type mapping.

## Exhaustive full-match construction paths

The official SMALI contains five invocations of the full `best.s(k0,int,c0,c0,int,k0,k)` constructor, but they collapse to exactly three source methods/classes:

1. **League path — `konrent.t.X(...)`**
   - passes `p0` / the `konrent.t` instance itself as the first constructor argument;
   - `konrent.t extends best.k0`, not `konrent.a0`;
   - therefore `match.A() instanceof konrent.a0 == false`.

2. **Knockout path — `konrent.f0.e(...)`**
   - method signature receives `konrent.a0 a0Var` explicitly;
   - all three constructor sites in the method pass that same `a0Var` as the first constructor argument;
   - therefore `match.A() instanceof konrent.a0 == true`.

3. **Friendly path — `konrent.a.a0(...)`**
   - passes `core.a.f13450b.I()` as the first constructor argument;
   - `best.b.I()` returns `konrent.a` and `konrent.a extends best.k0`, not `konrent.a0`;
   - therefore `match.A() instanceof konrent.a0 == false`.

No other full-constructor invocation exists in the official SMALI corpus. `LegacyMatchConstructionSource` + `LegacyTicketParentClassRule` preserve this construction-source distinction explicitly instead of inferring it from competition type.

## Fingerprints

Java:

- `best/s.java :: full constructor` — `97e9e9d882e2d2ad10a8284c4a79e86ea6f77593a0883599fbaa8a18a9b77150`
- `konrent/f0.java :: e(...)` — `43caf56ceae976a439cd7b00e9f5868b45c8a6bab54c2ff6b0ce049f5a18ec40`
- `konrent/a.java :: a0(...)` — `f245b67abea042878431a718f563b9ae1e9d2ebd83533329f8ce4f9f4097f835`
- `konrent/t.java :: X(...)` — decompiler body unavailable; Java stub fingerprint `e7061d3fe367408d6904d0242ee981ff910b6d029eb52516be341492129ee4ef`, with semantics taken from SMALI below.

SMALI:

- `best/s.smali :: full constructor` — `c8897a2ddd65f141e5195b9fd1056c65a5d4ca861f23cbbf5b38e9e340df3d1f`
- `konrent/f0.smali :: e(...)` — `4e27639e9ae1d930081ed31c172c1f76d4d7c588c04a4b416a5db4f20e4554a0`
- `konrent/a.smali :: a0(...)` — `097427a1a4c81e56871fd8e773f117edcc533fed422ca56d83f68df0baaa5a44`
- `konrent/t.smali :: X(...)` — `4e30fb981d17acb1e7755ec1cbb178ab0f9d8f7933a185e5fa7f103e31ff400b`

## Persistence consequence

The discriminator is now fully characterized, but the current V8 scheduled-match/competition tables do not persist the **construction source**. A future additive runtime shape can store that enum/code when a match is materialized and then derive the boolean deterministically. Existing V8 careers must remain fail-closed rather than backfilling it from `legacyCompetitionType`.

This closes the semantic uncertainty around the third remaining ticket input. Together with the characterized mutable club division `O()` and manager `H`, the remaining work is persistence/materialization and atomic lifecycle integration — not further guessing about ticket formulas.

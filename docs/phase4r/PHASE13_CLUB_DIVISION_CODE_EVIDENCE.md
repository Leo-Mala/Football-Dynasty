# Phase 13 — Club division code (`best.c0.O()`) evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Field and setter

`best.c0.O()` returns raw career field `j` (`f4139j`). `best.c0.s1(int)` is the direct writer:

- when the requested value is greater than `4`, it writes `0`;
- otherwise it writes the requested value unchanged;
- therefore negative values are **not** normalized by the legacy setter.

Modern `LegacyClubDivisionCodeRule.write(...)` preserves this asymmetric guard exactly.

Evidence fingerprints:

- `best/c0.java :: s1(int)` — `de6eac08d99220c271568952caf7aa6bdeb8a2cae369a36d5f96780eb0c72405`
- `best/c0.smali :: s1(I)V` — `c5b3a0e9bc33d1e5c225e7e789f2545191eac174daefdcb1e50ee55ba3ee237e`

## National league assignment

In the `konrent.t` constructor used with a national-country `best.x` parent, the legacy order is:

1. `best.x.X0()` increments the country's raw division counter;
2. `best.x.U0()` is copied into league field `F`;
3. the league is attached to the country;
4. `konrent.t.f1()` iterates every club in its `H` list and calls `club.s1(F)`.

SMALI confirms `X0()` before `U0()` and the subsequent `f1()` call. Therefore a club's `O()` is mutable league-membership state, not immutable source `.ban` `level` data.

`konrent.t.f1()` fingerprints:

- Java — `fcb42b108e0439493855762a2d853801a7477115db22b325292a56f2cf8f43bd`
- SMALI — `c0f93d89d077523529affb8388e44123028f1862f6d175875bae3b56ef429035`

## Promotion, relegation and country pool

`best.x.o0()` performs end-of-cycle movement between its ordered national divisions. It moves relegated/promoted clubs between adjacent `konrent.t.N0()` lists, then calls `f1()` on each division so every moved club receives the destination division's `F` value.

For clubs leaving the last represented division, the method explicitly calls `club.s1(0)` before placing them back in the country's spare-club pool. The special `konrent.t.c1()` path likewise writes `0` to every club before returning them to the parent country pool.

Evidence fingerprints:

- `best/x.java :: o0()` — `c2bc72eb25ffe6befe9d3561a64b388d6d1b1c5653c4c83cdf1a7f4dae869e99`
- `best/x.smali :: o0()V` — `7c27f8704196d0fdb27488258fb205ba1a590f0b2cee5aab12bcbbcd0bcf8b2a`
- `konrent/t.java :: c1()` — `061eea44c6d9b0c17e1dc423ddce862ed28e0b2035c53e9e5cdca946ba469362`
- `konrent/t.smali :: c1()V` — `227e1333e10fe3e843d9b0a9fa89871e3512779eb911a40a19261a4ae8855c29`

## Persistence consequence

Ticket revenue reads `home.O()` both for attendance randomness and for several price-table paths. Because `O()` changes when clubs move between divisions, it cannot be reconstructed from immutable `ClubEntity.level` or from a historical source snapshot after the career advances.

The current Room V8 schema has no lossless durable projection of this field. Existing V8 careers must therefore remain fail-closed for this value: a future additive migration may create nullable/raw state, but it must not synthesize `O()` from source `level` or guess it from the currently scheduled competition.

For newly materialized or explicitly imported career state, the proven writers above define how the raw code can be stored and updated. `LegacyClubDivisionCodeRule` characterizes the value semantics now; persistence is deliberately deferred until the manager/club runtime schema change is designed as one coherent additive migration.

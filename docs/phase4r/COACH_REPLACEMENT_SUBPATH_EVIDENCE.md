# Phase 14 — replacement-manager subpath evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`, package `com.brasfoot.v2020`, versionCode `202632`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

This note records the replacement-manager helpers promoted while the larger `best.c0.y()` host remains fail-closed.

## `best.b.t(best.c0,int)`

SMALI: `best/b.smali`, descriptor `t(Lbest/c0;I)Lbest/f0;`, 120 useful instructions / 20 branches, method-block SHA-256 `e5a323a42a6a8a843794b78e96c7cf55dc04885b9680dc992e339a58f1f2e0fc`.

Candidate requirements are exact: unemployed, non-human, primary or secondary country code equals `club.j0()`, and manager `w()` falls inside the mode range. Ranges are `-1 => 0..5`, `0 => p0..p0`, `1 => p0-1..p0`, `2 => p0-2..p0`. Mode 2 briefly writes `p0+1` but the same legacy block immediately overwrites the upper bound with `p0`; the effective bound is therefore `p0`.

An empty pool consumes no random draw. A non-empty pool draws `nextInt(100)`: values `<50` stable-sort by `v()` descending, `H()` descending, `s()` ascending; values `>=50` use `Collections.shuffle`. Modern code uses the project `RandomSource` and explicit Fisher-Yates bounds, preserving branch/draw structure without claiming equivalence to the legacy implicit seed.

## `best.b.u()`

SMALI descriptor `u()Lbest/f0;`, 30 useful instructions / 4 branches, SHA-256 `4ca3c0e9285b8036b4f6a174250cd8ad23cf3d16aa8ef6ead8aef1c968f9a721`.

Returns the first manager in source order whose club is null and whose human flag is false. No RNG.

## `best.b.b4(best.f0,best.f0)`

SMALI descriptor `b4(Lbest/f0;Lbest/f0;)V`, 9 useful instructions / 0 branches, SHA-256 `181c42f551aae17dea8470797f228dcc520d4a884e913466624658ae066c3914`.

The method captures both current clubs first, executes `first.l(second)`, then `second.l(first)`, then `first.e(secondClub)`, then `second.e(firstClub)`. This is deliberately not represented as two `G(...)` transfers because that would change departure/arrival ordering.

## Remaining blocker

`best.c0.y()` remains blocked. Its final fallback invokes `best.b.B(manager,false)`, whose club-pool construction delegates into `best.x.H0(...)` and can also shuffle intermediate competition/country lists. That nested ordering and RNG must be characterized before the whole replacement-manager resolver is promoted.

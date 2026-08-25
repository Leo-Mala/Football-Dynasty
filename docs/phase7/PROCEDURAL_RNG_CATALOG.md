# Phase 7 — Procedural RNG catalog

Official corpus: `Brasfoot.apk_Decompiler.com.zip`

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

This catalog records only control flow and values directly proven by the Brasfoot 2026/27 Java/SMALI corpus. Obfuscated field labels are retained where sporting meaning is not proven.

## Annual call path

The annual fallback reached from `best.c0.n()` is:

`best.f.e(...) -> best.p.d(target, requestedE, null, 0, null, FALSE) -> best.t.e(false, draft, target)`

The Java decompiler does not recover `best.p.d(...)` or `best.t.e(...)`; SMALI is the controlling evidence.

## `best.p.d(...)` direct RNG sites

The method contains seven direct `new Random().nextInt(...)` sites:

1. `nextInt(100) + 1` — selects legacy `n` bucket and sets legacy `b` true only when the roll is exactly `1`.
2. `nextInt(4) + 16` — writes legacy `c`.
3. `nextInt(100) + 1` — generates legacy `e` by bands `0/1/2/3/4`; the caller-provided `requestedE` overrides the stored result only after this draw, so the draw is never skipped.
4. `nextInt(6)` — conditional special rewrite of legacy `d`; reached only when the raw site-3 roll is exactly `1` and target `f0 >= 18`.
5. `nextInt(200)` — reached only from site 4 when target `d == 29` and selector is `5`.
6. `nextInt(4) + 7` — reached only when generated legacy `d` differs from target `d`; rewrites legacy `n`.
7. `nextInt(2)` — writes legacy `g` after name generation.

The old count of seven is therefore only the direct count. It is not the full procedural draw count.

## Transitive RNG before `best.p.D(...)`

Immediately after legacy `e` is selected, `best.p.d` calls `best.o.z(e)`.

`best.o.z(int)` chooses one pair by `nextInt(pairTable[e].size)`:

- `e=0`: 5 pairs;
- `e=1`: 4 pairs;
- `e=2`: 4 pairs;
- `e=3`: 12 pairs;
- `e=4`: 6 pairs.

The selected pair writes legacy `j` and `l`.

When no explicit name is supplied, `p.d` calls `best.o.i0(d)`. That calls `best.u.c(d)` first and uses `best.o.X0(d)` only if `u.c` returns null/empty.

## `best.u.c(...)` name RNG

With loaded name assets, `best.u.c(int)` performs:

1. `nextInt(names.size)` for the first entry;
2. when `names.size >= 1000`, `nextInt(2)`; if this equals zero, replace the first index with `nextInt(500)`;
3. if the first entry contains one whitespace-delimited word and the surname list has more than two entries, `nextInt(surnames.size)`;
4. if the first entry contains exactly two words, `nextInt(2)` decides whether a short second name may be appended; only when that gate passes and the structural guards hold is `nextInt(names.size)` consumed.

Index zero is rewritten to one after each list draw, exactly as in the Java source.

Corpus inventory confirms 221 `resources/assets/names/*.txt` and 221 `resources/assets/surnames/*.txt` files (442 total, about 496 KB uncompressed). Wiring the integer country index to those files remains a separate integration step; the pure list-selection algorithm is now isolated and testable.

## `best.p.D(c0)` RNG

`D(c0)` consumes:

1. `nextInt(3)` unconditionally after target-band calculation;
2. if legacy `b` is true, another `nextInt(3)` after adding 9;
3. `h()` -> `nextInt(5)`;
4. `g()` -> `nextInt(100)`.

The intermediate `h()/g()` results are overwritten later by `p.d`, but their draws remain part of the observable sequence and must not be removed.

The calculated legacy `f` is clamped in the unusual legacy order: subtract 23; values below 5 become 10; values above 100 become 100.

## `best.p.h()`

`h()` computes `base(c) + nextInt(5) + 1 + n` with bases `16->15`, `17->35`, `18->55`, `19->70`, `20->75`, otherwise `0`. Result is clamped to at least 1; if it exceeds 100, legacy behavior sets it to **95**, not 100.

## `best.p.g()`

`g()` consumes `nextInt(100)+1`: `<=15` keeps `n`, `16..60` uses `n-1`, and `61..100` uses `n+1`, then clamps to `1..10`.

## End of `best.p.d(...)`

After `D(target)`, `p.d` calls `e(target)` (no RNG), then calls `h()` and `g()` again. These final calls overwrite the earlier `o/m` values but consume two additional draws.

For the annual path, the fixed structural path excluding name generation and conditional special-`d` branches therefore has ten draws. `b=true`, special-`d` handling and name generation increase that count.

## `best.t.e(...)` RNG

SMALI confirms five bytecode sites, with conditional exclusivity:

1. `nextInt(5)` — always added to the target-club band before scaling draft `o`;
2. `nextInt(10)` — only when draft legacy `n >= 9`;
3. `nextInt(3)` — only when draft legacy `b` is true; value `1` enables `O0`;
4. `nextInt(200)` — only when draft legacy `b` is false; value `1` enables `O0` and short-circuits site 5;
5. `nextInt(300)` — only when draft legacy `b` is false and site 4 missed; value `1` enables both legacy `M` and `O0`.

After flags, `O0 && t0() < 8` executes `J1(8)`, structurally raising the current value to at least 8.

## Modern boundary

Phase 7 uses `RandomSource` for every reconstructed draw. This preserves a deterministic, persistable modern sequence and exact legacy bounds/branches/order, but does not claim bit-for-bit parity with the APK's many independently seeded `new Random()` instances.

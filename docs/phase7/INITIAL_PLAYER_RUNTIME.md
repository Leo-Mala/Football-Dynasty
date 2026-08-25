# Phase 7 — Initial player runtime and persistence boundary

Official corpus: `Brasfoot.apk_Decompiler.com.zip`

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven new-career path

The static `.ban` player record (`e.g`) does not contain the runtime value returned by `best.o.O()`.

`best.c0(e.t)` builds each active player through `new best.o(e.g, false, club)`. That constructor copies name, country, position, side, star, world-top, legacy hash, status, CR1, CR2 and age, then calls `r1()`. It does **not** call `p1(...)` and therefore does not initialize `best.o.j`, the field returned by `O()`.

During new-career setup, `StartOptions.c()` calls `best.h.r(context)`. That path calls `best.b.z1()`, and `z1()` loops through every active `best.o` and calls `o.q1()`.

`q1()` is therefore the proven initialization site for the runtime value used later by annual selection (`O()>50`, rating bands, value calculations and age-related decline).

## `best.o.q1()` RNG

SMALI confirms this order:

1. map target club band from `R0/O/P0`;
2. map target `f0` through the same 16..25 remap family used elsewhere;
3. consume `nextInt(3)` and add it;
4. only when player legacy `E == 1`, add `8 + nextInt(2)`;
5. only when star **or** world-top is true, add `9 + nextInt(3)`;
6. cap the value at 100;
7. consume `nextInt(30) + 210` and write the initial legacy duration;
8. write the computed value through `p1(...)`;
9. call `p()` and `o()`. These two post-processing methods do not contain RNG.

`LegacyCareerPlayerInitializationRules` reproduces this control flow with `RandomSource`.

## Persistence conclusion

The value returned by `O()` is not factual `.ban` data and must not be added to the canonical `players` import as if it came from the dataset.

It is career-generated runtime state. The legacy Kryo career graph persists it as the non-transient `best.o.j` field, and later routines mutate it (including annual age-related decline).

The modern persistence model therefore needs career-scoped player runtime state before full annual movement/procedural integration can be claimed.

## Architecture constraint

A procedural player created by `best.p.d -> best.t.e` belongs to the career graph. Inserting such a player into the global canonical `players` catalog would leak it into unrelated careers.

The Phase 7 storage design must keep:
- immutable/reference player facts separate from career-generated runtime values;
- procedural players scoped to a career;
- career membership changes scoped to the same career;
- foreign-key cleanup tied to `career_metadata`.

Room remains V2 until that V3 schema is designed, migration-tested and committed with generated schema evidence.

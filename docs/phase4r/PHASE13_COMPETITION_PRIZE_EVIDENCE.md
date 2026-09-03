# Phase 13 — competition prize finance evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Reachable call and calculation path

The recovered knockout/round container `konrent.f0` carries the round prize in field `f13873n`.

`konrent.f0.e(...)` performs, in order:

1. stores raw competition type from argument `i5`;
2. stores raw stage from argument `i2` through `z(i2)`;
3. computes `l(rawCompetitionType, rawStage)`;
4. stores that result through `y(...)` into `f13873n`;
5. creates the round matches.

After the round is decided, `konrent.f0.d(boolean,boolean)` resolves the winner of each pairing. The only finance mutation is:

`prize > 0 && winner.Q0() -> winner.B(prize, 3)`.

The losing club receives no prize mutation. `best.c0.B` credits club cash and, because `Q0()` has already been proven true by the caller, routes category `3` into `best.m.a`. Category `3` is the existing prize-income bucket.

## Exact recovered prize calculation

`konrent.f0.l(type, stage)`:

- type `2`: `Q1[stage + ((Q1.lastIndex) - a0.i0())]` when computed index is `< Q1.size`, otherwise `0`;
- type `4`: row is `a0.b0().p()` when nonnegative, otherwise row `1`; return `S1[row][stage]` only when `stage < S1.size`;
- type `5`: return `T1[stage]` only when `stage < T1.size`;
- type `6`: row is `a0.b0().p()` only when in `0..1`, otherwise row `1`; critically, return `U1[row][stage]` only when `stage < U1.size` (the OUTER array length, `2`), not when stage is within the six-value inner row;
- type `8`: always return `V1[0]`;
- other types: `0`.

Recovered values:

- `Q1 = [10000, 10000, 100000, 200000, 500000, 2000000, 3500000]`;
- `S1[0] = [500000, 1000000, 3000000, 7000000, 0, 0]`;
- `S1[1] = [500000, 1000000, 2500000, 5000000, 0, 0]`;
- `S1[2..5] = [200000, 500000, 1000000, 2000000, 0, 0]`;
- `T1 = [2000000, 5000000, 5000000]`;
- `U1[0] = [100000, 200000, 500000, 1000000, 3000000, 0]`;
- `U1[1] = [100000, 200000, 500000, 1000000, 2000000, 0]`;
- `V1 = [1000000, 500000]`, while this caller always selects element `0`.

The symbolic constants were resolved inside the same official corpus: `kotlin.time.g.f15020a = 1000000`, `com.google.android.gms.common.util.m.f8515d = 5000000`, and `f8517f = 7000000`.

## Preserved quirks

- Type `6` intentionally uses the outer-array length as the stage bound, making stages `>= 2` return zero despite populated inner-row values.
- Type `4` accepts every nonnegative raw `p()` value without an upper clamp; invalid positive values therefore retain the legacy array-index failure boundary rather than being normalized.
- Type `2` checks only `index < length`; a negative computed index retains the legacy array-index failure boundary.
- A zero/negative prize does nothing.
- A winner with `Q0() == false` receives neither cash nor ledger income because the caller skips `B(...)` entirely.
- No RNG, invented bonus, runner-up reward, balance clamp, football-data change or external rule is introduced.

## Evidence fingerprints

Exact extracted method text (UTF-8, including terminal LF) from the official archive:

- `konrent/f0.java :: private int l(int,int)` — `05306ca52b1294d2525207b600ffc63f2614d28df0cfde49477dd7c1db163db9`
- `konrent/f0.smali :: .method private l(II)I` — `4859e579f819f21a66e1b94d7b94e236e017e70a0b7b9f1879b30ac9d4594f62`
- `konrent/f0.java :: public void d(boolean,boolean)` — `68485dc1a45d7e8a1f6ecfd30daeb618a928cca011a29e7e023cb42e9863292d`
- `konrent/f0.smali :: .method public d(ZZ)V` — `cc9c9f38eeac053d0e6d4ba6c8c1d5dc0ebf9a0fe8fe42c8603c38461aede060`
- `konrent/f0.java :: public void e(...)` — `2ecb098c689868defffb58ce8279665e8639bdb87bb9eb2b6a81c983196ddee8`
- `konrent/f0.smali :: .method public e(...)V` — `214768e67ea1915dd1b48ec65b8df1fffea95ea1089b2fdb462635be717c18c3`

Normalized structural fingerprint for `e -> l -> d -> B(category 3)`:  
`e6bf34421c10333aed43f3b3a3506b768fba23163d602c9bd624c00aa5cced49`

## Modern boundary

- pure rule: `LegacyCompetitionPrizeRule`;
- persistence seam: `CareerCompetitionPrizeStore`;
- persistence write: `CareerManagerRuntimeStore.commitFinanceState`;
- Room remains V7; no migration is necessary because cash and `prizeIncome` are already persisted;
- tests characterize the recovered tables, type-6 outer-bound quirk, raw fallback behavior, `Q0()` eligibility, category-3 mutation and Room reopen persistence.

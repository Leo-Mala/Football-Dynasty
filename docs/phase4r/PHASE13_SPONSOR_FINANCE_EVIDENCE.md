# Phase 13 — annual sponsor finance evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`  
SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Reachable call path

`best.b.d()` -> private `best.b.s()` -> each `core.a.f13450b.g1()` club -> `best.c0.p()`.

`best.b.d()` is the recovered `NovoAno` path. Both decompiled Java and SMALI place `s()` in that path. `s()` then calls `c0.p()` for every club in `g1()`.

## Recovered mutation

`best.c0.p()` performs operations in this order:

1. if `club.countryCode != 29 && best.b.J1()`:
   - compute `club.q()` (senior `best.o.m0()` + youth `best.p.u()` salary values);
   - write cash directly as `(long) (cash + q() * 3.2d)`;
2. read raw division code;
3. only for division `0..4`, call `c0.B(best.j0.Y1[division][0], 6)`;
4. `c0.B` always adds that fixed amount to cash;
5. only when the club's legacy ledger flag `Q0()` is true, `c0.B` also calls `best.m.a(amount, 6)`;
6. `best.m.a(..., 6)` increments `f4365e`, exposed by `best.m.q()`;
7. `ActivityFinancas` renders `best.m.q()` in `R.id.in_patroR`, proving category `6` is the sponsor-income bucket.

The `best.b.J1()` field is initialized from `core.a.a().isJogaEstadual()` through `best.b.X2(...)`. Therefore the boolean is preserved as the raw state-championship-enabled flag; no broader football semantic is inferred.

## Fixed sponsor values

`best.j0.Y1[division][0]`:

| raw division | fixed credit |
| ---: | ---: |
| 0 | 3,500,000 |
| 1 | 6,500,000 |
| 2 | 5,000,000 |
| 3 | 3,000,000 |
| 4 | 2,500,000 |

The decompiler symbol used at division 2 resolves to `com.google.android.gms.common.util.m.f8515d = 5,000,000`.

## Preserved quirks

- The payroll-derived `3.2` amount mutates cash directly and is **not** added to the sponsor ledger.
- The payroll bonus executes before division validation. An invalid division can therefore receive the payroll bonus while receiving no fixed sponsor credit.
- Country code `29` suppresses only the payroll-derived branch; a valid division still receives the fixed sponsor credit.
- The fixed credit changes cash even when `Q0()` is false; only ledger recording is suppressed.
- The legacy expression promotes cash/payroll to JVM `double` and then converts back to `long`; the modern rule intentionally keeps the same double-to-long truncation boundary.
- No balance clamp, normalization, invented sponsor contract, RNG, or external sporting data is introduced.

## Evidence fingerprints

Exact extracted method text (UTF-8, including terminal LF) from the official archive:

- `best/c0.java :: public void p()` — `10b3e90b9d49effe2c97fdf4136d8cdb3246fde2cfe9d6e63ee997b6952b82ba`
- `best/c0.smali :: .method public p()V` — `60130e488e4ffc33faeff82bd7809ca6e6e864814baf905c4bdb009a07c5496e`
- `best/b.java :: private void s()` — `b8d90b6afe0e2e02625feac537528f2589e890ea0a3bdeb9dc57157f814e196e`
- `best/b.smali :: .method private s()V` — `f8f78f3654a01cc85bec89a64073c94165673128dc16800a5cc1da6053a6a0a1`
- `best/b.java :: public void d()` — `c0270a777998b4d412ab8ff85cc15291e5e77a1723445a0f2a2aef5d14fbf943`
- `best/b.smali :: .method public d()V` — `4941233af4f4e59370471d73a89fc31244ae8d5b1634e423ba0ddf79a5c0dbaa`
- `best/j0.java :: Y1 declaration line` — `3fbac6b725d51fd646faa727bc280e6d494d2ef04c3aba3fa4b285e1d4e93e6d`

Structural call/mutation fingerprint for the normalized path documented above:  
`6d43b41a88df2f6423efa164c05e0127616f4da10af66cd4794a9247e380f1fe`

## Modern boundary

- pure rule: `LegacySponsorPaymentRule`;
- persistence seam: `CareerSponsorPaymentStore`;
- persistence write: `CareerManagerRuntimeStore.commitFinanceState`;
- Room schema: unchanged (V7); no migration is needed because the existing sponsor ledger and cash fields already represent this proven state;
- tests cover the fixed values, payroll/ledger discrepancy, country-29 branch, invalid-division ordering, ledger-disabled behavior, and Room reopen persistence.

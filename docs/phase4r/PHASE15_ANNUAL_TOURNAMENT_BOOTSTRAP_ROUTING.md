# Fase 15 — `best.a.q()` annual tournament bootstrap dispatch

## Authority

Official corpus: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

This checkpoint was re-opened from the raw archive and read from `smali/best/a.smali`. SMALI is authoritative over decompiled Java.

## Reachable route

The annual `best.a.J(1)` command dispatcher maps command `cw` to `best.a.q()`.

The complete executable body of `best.a.q()` is a thin router:

1. read global `core.a.b` (`best.b`);
2. call `best.b.O0()` and obtain `konrent.b0`;
3. invoke `konrent.b0.V()` exactly once;
4. return.

There is no branch, loop, RNG draw, collection mutation, arithmetic or persistence write owned by `best.a.q()` itself.

## Modern boundary

`LegacyAnnualTournamentBootstrapRoutingRules.dispatch(...)` freezes only that single-call contract. It deliberately does **not** claim that the substantive `konrent.b0.V()` tournament rebuild has been implemented.

This separation matters because `V()` owns the actual tournament state reconstruction and remains subject to its own object/collection ownership audit. The `q()` router must not duplicate any RNG or state mutation that belongs downstream.

## Regression

`LegacyAnnualTournamentBootstrapRoutingRulesTest` proves that the supplied bootstrap boundary is invoked exactly once.

## Status

- `best.a.q()` thin routing: **IMPLEMENTED_AND_TESTED**;
- downstream `konrent.b0.V()` substantive tournament bootstrap: **REACHABLE_NOT_IMPLEMENTED** pending exact modern collection/state mapping;
- no Room/schema change is justified by this router boundary.

# Fase 15 — `best.n.m()` annual router evidence

Corpus authority: `Brasfoot.apk_Decompiler.com.zip`, SHA-256 `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`, package `com.brasfoot.v2020`, versionCode `202632`.

SMALI is authoritative for the executable order below.

## Exact reachable order

`best.b.d()` reaches `best.n.m()` in the annual lifecycle. `best.n.m()` executes in this order:

1. snapshot `best.b.P0()` into a local integer before any maintenance;
2. if `best.b.o0()` exists and is non-empty, attempt `best.b.d4()` inside an exception-swallowing boundary;
3. construct a legacy raw `java.util.Random`, consume exactly one `nextInt(100)`, and call `best.b.g4()` only when the result is strictly greater than `50`;
4. if `best.b.g0()` exists and is non-empty, attempt `best.b.e4()` inside a second exception-swallowing boundary;
5. call `best.b.j2(1)` unconditionally;
6. when the original pre-maintenance `P0()` snapshot was `0`, call `best.b.F2(true)`;
7. read `best.b.V0` and `best.b.E1()` and route exactly as follows:
   - `V0=true`, `E1=true`: `best.b.F()` then start `ActivityFimAno`;
   - `V0=true`, `E1=false`: `best.n.i()` only;
   - `V0=false`, `E1=true`: `best.b.F()` only;
   - `V0=false`, `E1=false`: no final call.

The `nextInt(100)` draw is unconditional: queue absence does not skip it, and the final `V0/E1` route does not influence whether it is consumed.

## Modern characterization boundary

`LegacyAnnualNMRoutingRules` now freezes this control flow with the project `RandomSource`. It does not claim that the APK's fresh time-seeded `new Random()` state is bit-identical to the persisted modern career stream. That distinction follows the project evidence policy: reachable legacy implicit/raw randomness is made explicit and deterministic in modern code without claiming an unprovable seed equivalence.

The boundary intentionally emits only call intents. It does not implement the substantive effects of `d4()`, `g4()`, `e4()`, `j2(1)`, `F2(true)`, `F()` or `best.n.i()`.

`LegacyAnnualNMRoutingRulesTest` freezes:

- exact queue/g4/queue/j2 order;
- the strict `> 50` gate at values `50` and `51`;
- one unconditional random draw even with both queues absent;
- `j2(1)` before `F2(true)` when the original `P0()==0`;
- all four `V0/E1` final routes.

## Re-audit of the apparent `best.f` RNG blocker

The current branch already contains earlier, tested compatibility primitives for the reachable `best.f` chain:

- `LegacyAnnualRandomRules.bestFConstructorGate(...)` for `nextInt(100) > 10`;
- `LegacyAnnualRandomRules.bestFNGate(...)` for the reachable `best.f.n()` predicate;
- `LegacyAnnualRandomRules.shuffleInPlace(...)`, an explicit reverse Fisher-Yates driven by `RandomSource`, documented not to claim parity with the APK implicit shuffle seed;
- `LegacyAnnualSelectionRules` for the characterized constructor, `n`, `q` and fallback selection predicates/ranges;
- `LegacyAnnualJAndBestFDeepTest` and `LegacyAnnualRandomRulesTest` covering relevant draw-count/selection behavior.

Therefore the remaining `g4 → n3 → best.f` gap is not "raw RNG policy unknown". The compatibility policy and pure selection primitives already exist. What remains is the runtime composition that gathers the exact legacy candidate collections in the exact order, invokes those characterized primitives, and then passes the selected target/value into the already implemented annual `T1(target,value,true,false,false)` mutation adapter.

## Remaining substantive callees

This audit also confirms the following bodies still need independent runtime classification before `best.n.m()` can be promoted as a complete annual implementation:

- `best.b.d4()` — expired `components.o2` queue processing and `best.o.U1(club)` mutation;
- `best.b.e4()` — expired `components.y1` queue processing and its payload application;
- `best.b.j2(1)` — competition-list routing through `best.a.x()`/`best.a.J(1)`;
- `best.b.F2(true)` — writes legacy field `M0=true`; semantic/persistence equivalence must be proven before adding durable state;
- `best.b.F()` — multi-pass reset over competition/tournament/player collections, including the SMALI-observed first-`z0()` access quirk;
- `best.n.i()` — false final branch when `V0=true` and `E1=false`.

No schema change is justified by this router characterization alone.

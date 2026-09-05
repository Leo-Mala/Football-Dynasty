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

## `d4()` payload — `components.o2`

The official SMALI resolves the queue payload without relying on names inferred from UI:

- `components.o2` is `Serializable` and contains exactly `best.o player`, `Calendar expiry`, `best.c0 club`;
- its `(best.o,best.c0)` constructor immediately appends itself to `best.b.o0()`;
- the constructor derives a calendar from the active competition calendar and adds exactly `0x13f` (`319`) days;
- `best.b.d4()` scans the queue; for an entry whose expiry is before the current global calendar, it calls `player.U1(club)` only when both references are non-null, then stages the expired entry for removal;
- `best.o.U1(club)` is itself only a swallowed-exception wrapper around `T1(club, 0, false, false, true)`.

This means the final movement mutation is not unknown: its exact T1 flag tuple is now proven. What is still missing is the durable representation and creation lifecycle of the delayed `components.o2` queue. It must not be collapsed into `LegacyPendingPlayerMovement`, whose proven fields describe a different three-scalar pending-movement structure. A V15 schema is therefore not justified until all `components.o2` writers/callers are mapped and a durable modern queue is shown to be necessary.

## `e4()` payload — `components.y1`

`components.y1` is also `Serializable` and contains exactly:

- `best.k a`;
- `Calendar b`;
- an `int[4] c` accumulator array.

Its `a()` payload method loops the four slots in ascending index order. For every slot whose value is strictly positive it calls `best.k.h(index, value)` and then zeros that same slot. `best.k.h(index,value)` adds `value` to the matching slot of its own four-element `b` array when the index is in bounds.

`best.b.e4()` applies `components.y1.a()` only to expired entries, stages those entries, and removes them after the scan. No RNG is consumed in this payload path.

As with `d4()`, the mutation body is now characterized but the queue creation/writer lifecycle and exact modern owner for the four `best.k` counters still need to be proven before persistence is introduced.

## Remaining substantive callees

After this payload audit, the remaining blockers before `best.n.m()` can be promoted as a complete annual implementation are narrower:

- `d4()` — exact T1 mutation is proven, but delayed-queue creation/writers and durable representation remain open;
- `e4()` — exact four-slot application is proven, but delayed-queue creation/writers and modern `best.k` counter owner remain open;
- `best.b.j2(1)` — competition-list routing through `best.a.x()`/`best.a.J(1)`;
- `best.b.F2(true)` — writes legacy field `M0=true`; semantic/persistence equivalence must be proven before adding durable state;
- `best.b.F()` — multi-pass reset over competition/tournament/player collections, including the SMALI-observed first-`z0()` access quirk;
- `best.n.i()` — false final branch when `V0=true` and `E1=false`;
- object-level `g4 → n3 → best.f` composition using the already existing deterministic compatibility primitives.

No schema change is justified by this router/payload characterization alone.

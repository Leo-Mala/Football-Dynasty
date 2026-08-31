# Phase 11 — Tactics → match runtime evidence

Official corpus: `Brasfoot.apk_Decompiler.com.zip`

SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`

## Proven tactic slot consumed by the match engine

The legacy club object `best.c0` owns an integer tactics array `S` initialized as four zeroes.

- `best.c0.x1(int index, int value)` performs exactly `S[index] = value`.
- `best.c0.i0()` returns that same `S` array.
- `DialogTatics.j()` writes the three editable radio groups through `x1(1, ...)`, `x1(2, ...)`, and `x1(3, ...)`.
- `best.s.k(best.s,int,int)` selects the current match side, obtains that side's `best.c0`, calls `i0()`, and reads array element **2**.

The Java decompilation shows `sVar.s0().i0()[2]` for one side and `sVar.t0().i0()[2]` for the other. SMALI confirms the same path: after `Lbest/c0;->i0()[I`, the engine executes `aget` using a register previously loaded with constant `2`.

Therefore the raw legacy match tactic index is exactly:

`best.c0.S[2]`

The modern bridge must not substitute slot 1 or slot 3, and must not pre-normalize the value. `best.s.k(...)` itself performs the legacy fallback `>= 3 -> 0` before indexing its `{30,10,0}` tactic-offset table.

## Reachable halftime mutation

`ActivityIntervalo.k()` passes the currently selected match club object to `DialogTatics.l(best.c0)` and opens `DialogTatics`.

`DialogTatics.j()` then mutates that same `best.c0` through `x1(...)`. There is no copy or alternate tactics object in this path. Subsequent match-engine reads of `i0()[2]` therefore observe the changed slot-2 value.

This proves the reachable chain:

`ActivityIntervalo → DialogTatics → best.c0.x1(2,value) → best.c0.S[2] → best.s.k(...)`

## Modern consequence

- `LegacyTacticsMatchRuntimeRule.MATCH_ENGINE_OPTION_SLOT = 2`.
- `LegacyTacticsMatchRuntimeRule.matchEngineTacticIndex(...)` returns the raw slot-2 value.
- `CareerMatchExecutionCoordinator.executeManagerMatch(...)` supplies the characterized home/away slot-2 values together with the Phase 11 lineup output to the persisted Phase 9/Phase 8 match seam.
- Mid-match UI can commit a new `LegacyTacticsRawState` through the already-characterized `LegacyTacticsDialogRuntimeRule.commit(...)`; reading slot 2 after that commit yields the tactic index observed by the second-half legacy engine path.

No external football rule or inferred tactic meaning is introduced here.

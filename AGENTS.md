# AGENTS.md — Football Dynasty

This file is the project-wide operating contract for coding agents working in this repository. It is intentionally phase-independent. Current phase scope, branch names, PR numbers and certified SHAs must be discovered from GitHub and the current phase documentation at the start of every task.

## 1. Mission

Reconstruct and modernize the legacy football-management game for current Android while preserving proven legacy behavior and bundled football content until the reconstruction/data-freeze phase is formally closed.

Priority order:

1. repository integrity and user instructions;
2. behavioral parity with the official legacy reference;
3. deterministic, testable implementation;
4. safe persistence and career isolation;
5. maintainable modern architecture;
6. performance and UI refinement.

Do not trade a higher-priority item for a lower-priority convenience.

## 2. Start-of-task protocol — never work from stale chat state

Before editing code, always inspect the real repository state:

1. identify the active integration branch and current development branch;
2. record their exact SHAs;
3. inspect the active PR, including base/head, Draft/Ready state, mergeability and unresolved review threads;
4. inspect workflow runs associated with the exact current head;
5. read `docs/DATA_FREEZE.md`, the relevant current-phase docs and any referenced evidence files;
6. compare this state with any handoff/snapshot supplied in chat;
7. if GitHub is newer, preserve the newer work and continue from it.

A chat transcript, handoff prompt or prior report is context, not authority. GitHub is authoritative for repository state.

Do not recreate a branch/PR merely because a previous description is stale. Preserve existing work unless the user explicitly requests replacement.

## 3. Official legacy reference and factual-data freeze

The only approved factual football reference during reconstruction is the official supplied Brasfoot corpus documented by the project. The currently fixed reference is:

- package: `com.brasfoot.v2020`;
- versionCode: `202632`;
- source archive: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

Follow `docs/DATA_FREEZE.md`.

DO NOT use external football sources to change or complete:

- players or identities;
- attributes, ratings or positions;
- squads/rosters;
- clubs;
- competitions or formats;
- sporting rules encoded by the legacy build.

Do not fill unknown values with plausible football knowledge. If evidence is incomplete, keep the concept neutral/unknown and document the boundary.

Never “fix” unusual legacy gameplay merely because it looks wrong. A legacy quirk or bug is part of parity until evidence and project scope explicitly authorize changing it.

## 4. Reverse-engineering evidence rules

Use the legacy material as evidence, not as the target architecture.

Evidence hierarchy for behavior:

1. executable/bytecode behavior and SMALI;
2. decompiled Java when complete and consistent with SMALI;
3. callers/callees, serialized fields and binary/assets evidence;
4. repeatable characterization fixtures/tests;
5. inference only as a documented hypothesis — never as implemented fact.

When Java is truncated, suspicious or contains a decompiler stub, inspect the matching SMALI before implementation. For important behavior, cross-check Java and SMALI even when Java looks readable.

For each migrated gameplay path, identify as applicable:

- inputs and outputs;
- control flow and short-circuit behavior;
- state mutations and ordering;
- RNG sites and bounds;
- list iteration/shuffle ordering;
- persistent/global side effects;
- time/season effects;
- financial/competition effects;
- error/empty/null behavior.

Use neutral legacy-oriented names when sporting semantics are not proven. Rename to domain semantics only after evidence supports it.

## 5. Implementation principles

Parity first, redesign second.

Prefer small pure rules and explicit state over copying large legacy classes. Separate:

- pure simulation/rules;
- Android/UI concerns;
- persistence/repository concerns;
- legacy compatibility/import boundaries.

Do not copy the decompiled application wholesale into production source.

Preserve odd branch ordering, unreachable branches, one-shot rerolls, non-wrapping scans, mutable pool ordering and similar behaviors when they are proven by the legacy implementation.

Do not silently introduce defaults, retries, sorting, normalization or “cleanup” that changes behavior.

Keep canonical/reference football facts separate from career-scoped runtime state. Procedurally generated or career-mutated players/memberships must not leak across careers.

## 6. RNG and determinism

Modern gameplay code must use the project `RandomSource` boundary (or a proven successor abstraction).

Inside modern gameplay/domain code, DO NOT use directly:

- `java.util.Random`;
- `kotlin.random.Random`;
- `ThreadLocalRandom`;
- `Math.random()`.

For every reconstructed RNG path preserve and test:

- draw order;
- exact bound;
- comparison operator/threshold;
- branches that consume no draw;
- draw count;
- shuffle behavior;
- mutable RNG-dependent state.

Replace implicit legacy `Collections.shuffle` with an explicit deterministic shuffle driven by `RandomSource` when migrating that site.

Do not claim bit-for-bit equivalence with the legacy app's implicit seed unless it has actually been proven. The modern requirement is explicit, persistible and reproducible RNG state.

When gameplay state and RNG state are persisted, they must advance atomically: never save a new gameplay result with old RNG state or new RNG state without its corresponding gameplay mutation.

## 7. Persistence / Room

Never assume the current Room version from a chat message. Inspect the schema files and database declaration first.

Rules:

- `fallbackToDestructiveMigration` is forbidden;
- do not bump the schema version unless new persistent state is proven necessary;
- every schema bump requires an explicit non-destructive migration;
- export and commit the generated schema;
- add migration tests from the previous supported version;
- preserve foreign keys, career isolation and cleanup semantics;
- test save/reopen behavior for new career-scoped state;
- avoid DB I/O inside hot simulation loops.

Prefer pure simulation followed by a repository transaction over per-minute/per-draw database writes.

## 8. Tests and characterization

A compiling implementation is not sufficient evidence of parity.

Before or with each gameplay migration, add repeatable characterization tests covering the behavior being claimed. Include boundary cases where relevant:

- threshold edges;
- empty and single-element inputs;
- branch short-circuits;
- RNG bounds and draw counts;
- deterministic repeatability;
- snapshot/restore or save/reopen;
- career isolation;
- rollback/atomicity;
- legacy quirks explicitly preserved.

Never weaken the test suite to make CI pass. Do not remove, skip or relax a failing test unless the test itself is proven incorrect and the correction preserves or strengthens coverage.

Do not raise timeouts as a substitute for fixing a functional or performance failure.

## 9. Required validation discipline

Use the current phase workflow as the authoritative CI gate. Common repository-level checks include:

- exact-head checkout/verification;
- raw-RNG guard;
- `./gradlew help --no-daemon`;
- Room/KSP/schema generation;
- `./gradlew :app:testDebugUnitTest --no-daemon --stacktrace`;
- required JUnit suites executing more than zero tests with zero failures/errors/skips;
- official fixture/corpus integrity gates;
- `./gradlew :app:assembleDebug --no-daemon --stacktrace`;
- migration/destructive-fallback policy checks.

Do not certify a new commit with an older workflow run.

When CI fails:

1. inspect the failing job/step/log;
2. identify the concrete cause;
3. fix the cause, not the gate;
4. run/inspect CI again on the new exact head.

## 10. Git and PR discipline

- Never force-push or rewrite shared history.
- Never overwrite unrelated newer work.
- Do not push directly to the integration branch unless the user explicitly requests that workflow.
- Continue the existing phase branch and PR when they exist; do not create replacements to avoid a failure.
- Keep phase PRs Draft while implementation/certification is incomplete.
- Prefer focused commits with evidence/tests accompanying behavior changes.
- Temporary bootstrap/transport/debug artifacts must not remain in the final diff.

Before merge, freeze a `FINAL_HEAD` and verify again:

- PR head equals `FINAL_HEAD`;
- base is still the intended integration baseline;
- required workflows are green on `FINAL_HEAD`;
- PR is mergeable and conflict-free;
- no material review thread/comment remains unresolved;
- aggregate diff contains no accidental factual-data change, temporary artifact, raw RNG, destructive migration or weakened test.

When the task grants autonomous execution, do not repeatedly ask for merge permission. Merge automatically only after all gates above are satisfied, using an expected-head safeguard when available.

## 11. Reviews and tool availability

Never claim a review/tool approved something unless that review/tool actually ran on the relevant head.

If Codex or another optional reviewer is unavailable, continue with independent review using:

- diff inspection;
- Java↔SMALI comparison;
- tests/fixtures;
- GitHub Actions/logs;
- architecture, persistence, RNG and data-integrity review.

Do not present “Codex approved” when Codex did not run.

## 12. Phase completion and advancement

A phase is complete only when its claimed behavior is implemented or explicitly bounded by evidence, documentation is current, required tests/builds are green on the exact final head, and the PR passes final audit.

After a safe merge, re-read the repository state and project plans before defining the next phase. Derive the next scope from proven open boundaries; do not invent a phase from memory or assumptions.

When the user's task authorizes autonomous progression, create the next phase branch from the certified merge and continue without asking for routine authorization.

## 13. Working style

Investigate → implement → test → inspect failure → fix → recertify.

Do not stop at a status report when the task asks for implementation. Do not promise background work. Keep the user informed during long operations, but continue working until completion or a genuine permissions/safety blocker is reached.

When uncertain about behavior, gather evidence rather than guessing.

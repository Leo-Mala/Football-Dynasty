# AGENTS.md — Football Dynasty

This file is the project-wide operating contract for coding agents working in this repository. It is intentionally phase-independent. Current phase scope, branch names, PR numbers and certified SHAs must be discovered from GitHub and the current phase documentation at the start of every task.

## 1. Mission — rewrite the supplied game, do not redesign it

This project is a technical rewrite of the exact legacy game contained in the official supplied Brasfoot corpus. It is NOT a new football-management game inspired by that corpus.

The reconstructed application must preserve the same proven functional scope and sporting content as the official legacy reference. The goal is to reproduce the legacy game's functions and behavior on a maintainable modern Android implementation.

Priority order:

1. repository integrity and explicit user instructions;
2. functional parity with the official legacy reference;
3. behavioral parity with the official legacy reference;
4. factual sporting-data preservation;
5. deterministic, testable implementation;
6. safe persistence and career isolation;
7. maintainable modern architecture;
8. performance and UI implementation quality without changing scope.

Do not trade a higher-priority item for a lower-priority convenience.

## 2. Functional-scope lock — no new game functions

The official legacy corpus defines the feature set.

DO NOT add gameplay, management systems, rules, screens, competitions, options or user-facing game capabilities merely because they would be useful, modern or expected in another football game.

DO NOT remove, omit or simplify a proven legacy function merely because it is difficult to reconstruct.

A function may be implemented only when at least one of these is true:

- it exists in the official legacy corpus and is being reconstructed;
- it is technical infrastructure required to run that same legacy behavior on modern Android (for example Room, coroutines, AndroidX, Compose, DI, modern storage or compatibility code);
- it is test/diagnostic/CI infrastructure that does not create new game behavior.

Technical modernization must not silently create new gameplay semantics.

If the legacy corpus does not prove that a game function exists, do not add it to production gameplay. Record the uncertainty and investigate the corpus instead.

The target rule is:

`same game functions + same sporting content + same proven behavior -> modern implementation`

not:

`legacy inspiration -> redesigned game`.

## 3. Start-of-task protocol — never work from stale chat state

Before editing code, always inspect the real repository state:

1. identify the active integration branch and current development branch;
2. record their exact SHAs;
3. inspect the active PR, including base/head, Draft/Ready state, mergeability and unresolved review threads;
4. inspect workflow runs associated with the exact current head;
5. read `AGENTS.md`, `docs/DATA_FREEZE.md`, the relevant current-phase docs and any referenced evidence files;
6. compare this state with any handoff/snapshot supplied in chat;
7. if GitHub is newer, preserve the newer work and continue from it.

A chat transcript, handoff prompt or prior report is context, not authority. GitHub is authoritative for repository state.

Do not recreate a branch/PR merely because a previous description is stale. Preserve existing work unless the user explicitly requests replacement.

## 4. Official legacy reference and factual-data freeze

The only approved factual football reference during reconstruction is the official supplied Brasfoot corpus documented by the project. The currently fixed reference is:

- package: `com.brasfoot.v2020`;
- versionCode: `202632`;
- source archive: `Brasfoot.apk_Decompiler.com.zip`;
- SHA-256: `3eb5622ba9b5953a1bcc2c83c16700db86fc41c027989e34b8c00c207f25c465`.

Follow `docs/DATA_FREEZE.md`.

The reconstructed game must preserve the clubs, players, squads, competitions and sporting structures represented by that official corpus.

DO NOT use external football sources to change, complete or modernize:

- players or identities;
- player names;
- attributes, ratings or positions;
- squads/rosters;
- clubs or club identities;
- competitions;
- competition participants;
- competition formats;
- sporting structures or rules encoded by the legacy build.

Do not substitute current real-world information for legacy data.

Do not fill unknown values with plausible football knowledge. If evidence is incomplete, keep the concept neutral/unknown and document the boundary.

Never “fix” unusual legacy gameplay merely because it looks wrong. A legacy quirk or bug is part of parity until evidence and project scope explicitly authorize changing it.

## 5. Reverse-engineering evidence rules

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
- error/empty/null behavior;
- callers that prove the function is actually reachable in the legacy game.

Use neutral legacy-oriented names when sporting semantics are not proven. Rename to domain semantics only after evidence supports it.

Do not implement an apparently useful legacy method as gameplay merely because the method exists: establish its role/call path when that distinction matters.

## 6. Implementation principles

Parity first. Redesign of game behavior is out of scope during reconstruction.

Prefer small pure rules and explicit state over copying large legacy classes. Separate:

- pure simulation/rules;
- Android/UI concerns;
- persistence/repository concerns;
- legacy compatibility/import boundaries.

Do not copy the decompiled application wholesale into production source.

The internal architecture may be modernized aggressively only when the externally relevant game behavior remains equivalent.

Preserve odd branch ordering, unreachable branches, one-shot rerolls, non-wrapping scans, mutable pool ordering and similar behaviors when they are proven by the legacy implementation.

Do not silently introduce defaults, retries, sorting, normalization or “cleanup” that changes behavior.

Do not add a new game mechanic to compensate for an incompletely reconstructed legacy mechanic. Finish the reconstruction instead.

Keep canonical/reference football facts separate from career-scoped runtime state. Procedurally generated or career-mutated players/memberships must not leak across careers.

## 7. RNG and determinism

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

Do not claim bit-for-bit equivalence with the legacy app's implicit seed unless it has actually been proven. The modern requirement is explicit, persistible and reproducible RNG state while preserving the reconstructed probability/control-flow structure.

When gameplay state and RNG state are persisted, they must advance atomically: never save a new gameplay result with old RNG state or new RNG state without its corresponding gameplay mutation.

## 8. Persistence / Room

Never assume the current Room version from a chat message. Inspect the schema files and database declaration first.

Rules:

- `fallbackToDestructiveMigration` is forbidden;
- do not bump the schema version unless new persistent state is proven necessary to represent legacy behavior;
- every schema bump requires an explicit non-destructive migration;
- export and commit the generated schema;
- add migration tests from the previous supported version;
- preserve foreign keys, career isolation and cleanup semantics;
- test save/reopen behavior for new career-scoped state;
- avoid DB I/O inside hot simulation loops.

Modern persistence is an implementation detail. It must represent legacy game state without inventing new gameplay state.

Prefer pure simulation followed by a repository transaction over per-minute/per-draw database writes.

## 9. Tests and characterization

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
- legacy quirks explicitly preserved;
- proof that a reconstructed function does not introduce behavior outside the legacy function's scope.

Never weaken the test suite to make CI pass. Do not remove, skip or relax a failing test unless the test itself is proven incorrect and the correction preserves or strengthens coverage.

Do not raise timeouts as a substitute for fixing a functional or performance failure.

## 10. Required validation discipline

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

Where practical, phase gates should also guard against accidental factual-data changes and against unauthorized feature-scope expansion.

Do not certify a new commit with an older workflow run.

When CI fails:

1. inspect the failing job/step/log;
2. identify the concrete cause;
3. fix the cause, not the gate;
4. run/inspect CI again on the new exact head.

## 11. Git and PR discipline

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
- aggregate diff contains no accidental factual-data change, unauthorized game function, temporary artifact, raw RNG, destructive migration or weakened test.

When the task grants autonomous execution, do not repeatedly ask for merge permission. Merge automatically only after all gates above are satisfied, using an expected-head safeguard when available.

## 12. Reviews and tool availability

Never claim a review/tool approved something unless that review/tool actually ran on the relevant head.

If Codex or another optional reviewer is unavailable, continue with independent review using:

- diff inspection;
- Java↔SMALI comparison;
- tests/fixtures;
- GitHub Actions/logs;
- architecture, persistence, RNG and data-integrity review;
- functional-scope review against the official corpus.

Do not present “Codex approved” when Codex did not run.

## 13. Phase completion and advancement

A phase is complete only when its claimed legacy behavior is reconstructed or explicitly bounded by evidence, documentation is current, required tests/builds are green on the exact final head, and the PR passes final audit.

A phase must not be considered complete if a proven legacy function in that phase's scope was replaced by a simplified or newly invented substitute.

After a safe merge, re-read the repository state and project plans before defining the next phase. Derive the next scope from proven open legacy boundaries; do not invent a phase or new game feature from memory or assumptions.

When the user's task authorizes autonomous progression, create the next phase branch from the certified merge and continue without asking for routine authorization.

## 14. Working style

Investigate → prove legacy behavior → implement → characterize/test → inspect failure → fix → recertify.

Do not stop at a status report when the task asks for implementation. Do not promise background work. Keep the user informed during long operations, but continue working until completion or a genuine permissions/safety blocker is reached.

When uncertain about behavior, gather evidence rather than guessing.

The final question for every gameplay change is: “Is this function and behavior present in the official supplied game?” If the answer is not proven, do not add it.

## 15. Mandatory manual test build before final Release

The final Release stage has an explicit user-validation gate and must not begin immediately after automated certification.

Before creating the final production Release, release tag, or publishing the definitive production APK/artifact:

1. finish implementation and the normal automated certification required by the roadmap up to the pre-Release boundary;
2. generate an installable Android test build from the exact intended pre-Release candidate head;
3. make that test build available to the user for manual installation and gameplay validation;
4. record the exact candidate SHA/build identity supplied for manual testing;
5. wait for the user's explicit manual-test result before proceeding with the final Release stage;
6. if the user reports bugs, keep Release blocked, correct them through the normal branch/PR/test discipline, generate a new test build, and repeat manual validation;
7. only after the user explicitly approves the manually tested candidate may the project proceed to the final Release/tag/publication workflow.

The manual test build is a pre-Release validation artifact, not the official Release. Supplying it must not create a production tag, production Release, or bypass any remaining roadmap gate.

This manual-validation requirement is mandatory even when all automated CI, unit, instrumentation, migration, integrity, performance, or other certification gates are already green.

## 16. Shared Codex budget and model escalation

The repository owner actively develops three projects with the same Codex allowance:

- `Leo-Mala/Football-Dynasty`: target up to 35% of the shared weekly Codex budget;
- `Leo-Mala/Chat-Pro-Football`: target up to 30%;
- `Leo-Mala/Chat-Basket-Manager-`: target up to 20%;
- keep at least 15% unassigned as a shared reserve for blockers, regressions, CI failures and manual-test fixes.

These percentages are planning targets, not permission to spend quota merely because it is available. This repository must stay within its target whenever practical so the other two projects can continue through the same week. ChatGPT Work is not part of this plan and must not be assumed as an available budget or execution path.

### Codex usage rules

- Use Codex only when it materially reduces engineering time or improves coverage. Do not spend Codex quota on routine HEAD/status checks, PR metadata, simple GitHub operations, trivial one-file edits or work that direct GitHub tools can complete safely.
- Prefer `Luna` for high-volume and repetitive work such as repository search, Java↔SMALI call-site mapping, inventory, log triage, documentation, straightforward test generation and mechanical comparisons.
- Escalate to `Terra` for normal implementation, debugging, persistence/Room work, non-trivial tests, CI root-cause analysis and integrated code changes when Luna is not sufficient.
- Use `Sol` only for genuinely difficult blockers, architecture-sensitive problems, subtle parity/RNG/concurrency/persistence failures, or cases where the lower tier has proved inadequate. Do not make Sol the default.
- Use the lowest reasoning/effort level that can safely complete the task. Reserve maximum/high-cost modes for blocked or demonstrably complex work.
- Batch related investigation and implementation work so the same repository context and legacy evidence are reused. Do not launch duplicate agents to repeat an investigation that is already certified or still valid for the exact current HEAD/corpus.
- Reuse valid test, audit and CI evidence when repository policy permits it. Do not rerun expensive Codex work merely to restate status.

### Shared-quota guardrails

When reliable Codex usage information is available, manage the shared weekly allowance conservatively:

- below 50% consumed: normal economical use;
- 50-70% consumed: favor Luna and batch work more aggressively;
- 70-85% consumed: use Codex only for tasks that materially advance a project or remove a real blocker;
- above 85% consumed: preserve the remainder for critical regressions, CI blockers and APK/manual-test fixes until the allowance resets.

If reliable usage information is unavailable, never invent a percentage. Default to the conservative policy: Luna first, no duplicate agents, batch related work, and escalate only when justified.

Do not assume extra paid credits, automatic top-ups or an additional per-project quota. Repository correctness, legacy fidelity, required tests, explicit user instructions, exact-head discipline and existing project safeguards always take priority over quota savings; never weaken a gate or implementation merely to reduce Codex consumption.

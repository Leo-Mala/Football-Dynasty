package com.leomala.footballdynasty.domain.manager

/**
 * Exact control/list ordering of the annual junior lifecycle around `best.p.c(c0)` and
 * `best.t.e(TRUE, p, c0)`.
 *
 * Authoritative evidence is the official SMALI corpus. `best.b.q()` iterates the club's existing
 * `a0()` list without mutating that list in-place. TRUE promotion stages the current draft in F1,
 * exposes the materialized player to the club immediately through `c0.f(o)`, stages the player in
 * D1, and `best.p.c()` may then generate a replacement into E1 before the next original draft is
 * visited. F1/E1 are applied only after the current club iteration; D1 is appended to the global
 * player list only after every club has been processed.
 *
 * These buffers are transient lifecycle implementation details, not durable save state. A modern
 * Room boundary may therefore commit the equivalent post-lifecycle state atomically without
 * inventing persisted L1/J1 roster kinds, provided it preserves this operation/RNG order.
 */
object LegacyJuniorAnnualLifecycleRules {
    enum class Step {
        INCREMENT_AGE,
        MATERIALIZE_PLAYER,
        STAGE_DRAFT_REMOVAL,
        EXPOSE_PLAYER_TO_CLUB,
        STAGE_PLAYER_FOR_GLOBAL_APPEND,
        GENERATE_REPLACEMENT_IMMEDIATELY,
        REFRESH_CURRENT_DRAFT_IMMEDIATELY,
        APPLY_STAGED_DRAFT_REMOVALS_AFTER_CLUB_ITERATION,
        APPLY_STAGED_REPLACEMENTS_AFTER_CLUB_ITERATION,
        APPEND_STAGED_PLAYERS_TO_GLOBAL_AFTER_ALL_CLUBS,
    }

    /** Steps executed while visiting one draft in the original club-junior iteration. */
    fun immediatePlan(action: LegacyJuniorRuntimeRules.AnnualAction): List<Step> = when (action) {
        LegacyJuniorRuntimeRules.AnnualAction.NONE -> listOf(
            Step.INCREMENT_AGE,
        )
        LegacyJuniorRuntimeRules.AnnualAction.REFRESH_DRAFT -> listOf(
            Step.INCREMENT_AGE,
            Step.REFRESH_CURRENT_DRAFT_IMMEDIATELY,
        )
        LegacyJuniorRuntimeRules.AnnualAction.PROMOTE -> listOf(
            Step.INCREMENT_AGE,
            Step.MATERIALIZE_PLAYER,
            Step.STAGE_DRAFT_REMOVAL,
            Step.EXPOSE_PLAYER_TO_CLUB,
            Step.STAGE_PLAYER_FOR_GLOBAL_APPEND,
        )
        LegacyJuniorRuntimeRules.AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT -> listOf(
            Step.INCREMENT_AGE,
            Step.MATERIALIZE_PLAYER,
            Step.STAGE_DRAFT_REMOVAL,
            Step.EXPOSE_PLAYER_TO_CLUB,
            Step.STAGE_PLAYER_FOR_GLOBAL_APPEND,
            Step.GENERATE_REPLACEMENT_IMMEDIATELY,
        )
    }

    /** `best.b.q()` applies these buffers after the current club's original iterator is exhausted. */
    val afterClubIterationPlan: List<Step> = listOf(
        Step.APPLY_STAGED_DRAFT_REMOVALS_AFTER_CLUB_ITERATION,
        Step.APPLY_STAGED_REPLACEMENTS_AFTER_CLUB_ITERATION,
    )

    /** `best.b.q()` performs the global D1 append only after all clubs have completed. */
    val afterAllClubsPlan: List<Step> = listOf(
        Step.APPEND_STAGED_PLAYERS_TO_GLOBAL_AFTER_ALL_CLUBS,
    )
}

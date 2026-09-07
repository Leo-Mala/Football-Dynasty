package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyJuniorAnnualLifecycleRulesTest {
    @Test
    fun `annual promote stages lists around immediate club exposure in exact smali order`() {
        assertEquals(
            listOf(
                LegacyJuniorAnnualLifecycleRules.Step.INCREMENT_AGE,
                LegacyJuniorAnnualLifecycleRules.Step.MATERIALIZE_PLAYER,
                LegacyJuniorAnnualLifecycleRules.Step.STAGE_DRAFT_REMOVAL,
                LegacyJuniorAnnualLifecycleRules.Step.EXPOSE_PLAYER_TO_CLUB,
                LegacyJuniorAnnualLifecycleRules.Step.STAGE_PLAYER_FOR_GLOBAL_APPEND,
            ),
            LegacyJuniorAnnualLifecycleRules.immediatePlan(
                LegacyJuniorRuntimeRules.AnnualAction.PROMOTE,
            ),
        )
    }

    @Test
    fun `annual replacement generation remains immediate but insertion remains deferred`() {
        val plan = LegacyJuniorAnnualLifecycleRules.immediatePlan(
            LegacyJuniorRuntimeRules.AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT,
        )

        assertEquals(
            listOf(
                LegacyJuniorAnnualLifecycleRules.Step.INCREMENT_AGE,
                LegacyJuniorAnnualLifecycleRules.Step.MATERIALIZE_PLAYER,
                LegacyJuniorAnnualLifecycleRules.Step.STAGE_DRAFT_REMOVAL,
                LegacyJuniorAnnualLifecycleRules.Step.EXPOSE_PLAYER_TO_CLUB,
                LegacyJuniorAnnualLifecycleRules.Step.STAGE_PLAYER_FOR_GLOBAL_APPEND,
                LegacyJuniorAnnualLifecycleRules.Step.GENERATE_REPLACEMENT_IMMEDIATELY,
            ),
            plan,
        )
        assertEquals(
            listOf(
                LegacyJuniorAnnualLifecycleRules.Step.APPLY_STAGED_DRAFT_REMOVALS_AFTER_CLUB_ITERATION,
                LegacyJuniorAnnualLifecycleRules.Step.APPLY_STAGED_REPLACEMENTS_AFTER_CLUB_ITERATION,
            ),
            LegacyJuniorAnnualLifecycleRules.afterClubIterationPlan,
        )
        assertEquals(
            listOf(
                LegacyJuniorAnnualLifecycleRules.Step.APPEND_STAGED_PLAYERS_TO_GLOBAL_AFTER_ALL_CLUBS,
            ),
            LegacyJuniorAnnualLifecycleRules.afterAllClubsPlan,
        )
    }

    @Test
    fun `annual refresh mutates current draft immediately without staging list effects`() {
        assertEquals(
            listOf(
                LegacyJuniorAnnualLifecycleRules.Step.INCREMENT_AGE,
                LegacyJuniorAnnualLifecycleRules.Step.REFRESH_CURRENT_DRAFT_IMMEDIATELY,
            ),
            LegacyJuniorAnnualLifecycleRules.immediatePlan(
                LegacyJuniorRuntimeRules.AnnualAction.REFRESH_DRAFT,
            ),
        )
    }

    @Test
    fun `annual no-op still preserves unconditional age increment`() {
        assertEquals(
            listOf(LegacyJuniorAnnualLifecycleRules.Step.INCREMENT_AGE),
            LegacyJuniorAnnualLifecycleRules.immediatePlan(
                LegacyJuniorRuntimeRules.AnnualAction.NONE,
            ),
        )
    }
}

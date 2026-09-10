package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyLineupEligibilityKnownInputsRuleTest {
    @Test
    fun `M0 rejection resolves before every later unknown owner`() {
        assertEquals(
            false,
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = true,
                competitionRestrictionActive = null,
                excludedByCompetitionV0 = null,
                lineupModeFlag = null,
                hasClub = null,
                clubActiveQ0 = null,
            )
        )
    }

    @Test
    fun `mode true bypasses club and contract clock exactly like K0`() {
        assertEquals(
            true,
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = false,
                excludedByCompetitionV0 = null,
                lineupModeFlag = true,
                hasClub = null,
                clubActiveQ0 = null,
            )
        )
        assertEquals(
            false,
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = true,
                excludedByCompetitionV0 = true,
                lineupModeFlag = true,
                hasClub = null,
                clubActiveQ0 = null,
            )
        )
    }

    @Test
    fun `proven no club or Q0 false bypasses contract clock when mode is false`() {
        assertEquals(
            true,
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = false,
                excludedByCompetitionV0 = null,
                lineupModeFlag = false,
                hasClub = false,
                clubActiveQ0 = null,
            )
        )
        assertEquals(
            true,
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = false,
                excludedByCompetitionV0 = null,
                lineupModeFlag = false,
                hasClub = true,
                clubActiveQ0 = false,
            )
        )
    }

    @Test
    fun `Q0 true keeps result unresolved because exact contract clock is reached`() {
        assertNull(
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = false,
                excludedByCompetitionV0 = null,
                lineupModeFlag = false,
                hasClub = true,
                clubActiveQ0 = true,
            )
        )
    }

    @Test
    fun `unresolved persisted owners never masquerade as legacy null values`() {
        assertNull(
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = false,
                excludedByCompetitionV0 = null,
                lineupModeFlag = false,
                hasClub = null,
                clubActiveQ0 = null,
            )
        )
        assertNull(
            LegacyLineupEligibilityKnownInputsRule.resolveWithoutCareerClock(
                blockedByM0 = false,
                competitionRestrictionActive = true,
                excludedByCompetitionV0 = null,
                lineupModeFlag = true,
                hasClub = true,
                clubActiveQ0 = false,
            )
        )
    }
}

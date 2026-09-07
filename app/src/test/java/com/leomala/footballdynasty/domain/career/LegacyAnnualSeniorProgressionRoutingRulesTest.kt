package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualSeniorProgressionRoutingRulesTest {
    @Test
    fun `player without current club returns without progression or M clear`() {
        assertEquals(
            emptyList<LegacyAnnualSeniorProgressionRoutingRules.Step>(),
            LegacyAnnualSeniorProgressionRoutingRules.steps(
                hasCurrentClub = false,
                age = 21,
            ),
        )
    }

    @Test
    fun `age below thirty two grows before M clear`() {
        assertEquals(
            listOf(
                LegacyAnnualSeniorProgressionRoutingRules.Step.APPLY_GROWTH,
                LegacyAnnualSeniorProgressionRoutingRules.Step.CLEAR_LEGACY_M,
            ),
            LegacyAnnualSeniorProgressionRoutingRules.steps(
                hasCurrentClub = true,
                age = 31,
            ),
        )
    }

    @Test
    fun `age thirty two is first decline age and still clears M afterwards`() {
        assertEquals(
            listOf(
                LegacyAnnualSeniorProgressionRoutingRules.Step.APPLY_DECLINE,
                LegacyAnnualSeniorProgressionRoutingRules.Step.CLEAR_LEGACY_M,
            ),
            LegacyAnnualSeniorProgressionRoutingRules.steps(
                hasCurrentClub = true,
                age = 32,
            ),
        )
    }
}

package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyTicketParentClassRuleTest {
    @Test
    fun `league parent is not a0`() {
        assertFalse(
            LegacyTicketParentClassRule.parentCompetitionIsA0(
                LegacyMatchConstructionSource.LEAGUE_T
            )
        )
    }

    @Test
    fun `knockout parent is a0`() {
        assertTrue(
            LegacyTicketParentClassRule.parentCompetitionIsA0(
                LegacyMatchConstructionSource.KNOCKOUT_F0
            )
        )
    }

    @Test
    fun `friendly parent is not a0`() {
        assertFalse(
            LegacyTicketParentClassRule.parentCompetitionIsA0(
                LegacyMatchConstructionSource.FRIENDLY_A
            )
        )
    }
}

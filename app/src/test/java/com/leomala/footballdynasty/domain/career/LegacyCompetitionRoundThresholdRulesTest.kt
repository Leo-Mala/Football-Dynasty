package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyCompetitionRoundThresholdRulesTest {
    @Test
    fun `non-group competition follows double round schedule before threshold opens`() {
        assertEquals(0, LegacyCompetitionRoundThresholdRules.resolve(20, 0, 19))
        assertEquals(10, LegacyCompetitionRoundThresholdRules.resolve(20, 0, 20))
    }

    @Test
    fun `group calculation preserves integer division before legacy round calls`() {
        // 18 / 4 is integer 4 in SMALI: 4 * 2 - 1 = 7; 7 / 2 = 3.
        assertEquals(0, LegacyCompetitionRoundThresholdRules.resolve(18, 4, 3))
        assertEquals(2, LegacyCompetitionRoundThresholdRules.resolve(18, 4, 4))
    }

    @Test
    fun `returned round also preserves integer division for odd values`() {
        assertEquals(2, LegacyCompetitionRoundThresholdRules.resolve(18, 4, 5))
    }
}

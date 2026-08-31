package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPlayerSubroleCodeRuleTest {
    @Test
    fun goalkeeperAndCentreBackAlwaysWriteZero() {
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(0, 13, 13, 2))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(2, 8, 8, 2))
    }

    @Test
    fun sideBackBranchPreservesPrimaryCodePrecedence() {
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(1, 13, 7))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(1, 6, 10))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(1, 7, 13))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(1, 10, 13))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(1, 5, 13))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(1, 5, 7))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(1, 8, 0))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(1, 5, 0))
    }

    @Test
    fun midfieldBranchMatchesLegacyDefaultAndDefensiveCodes() {
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(3, 11, 7))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(3, 7, 11))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(3, 5, 11))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(3, 5, 10))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(3, 5, 5))
    }

    @Test
    fun attackerBranchPreservesThreeLegacySubroleCodes() {
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(4, 7, 0))
        assertEquals(0, LegacyPlayerSubroleCodeRule.resolve(4, 10, 0))
        assertEquals(2, LegacyPlayerSubroleCodeRule.resolve(4, 8, 0))
        assertEquals(2, LegacyPlayerSubroleCodeRule.resolve(4, 13, 0))
        assertEquals(2, LegacyPlayerSubroleCodeRule.resolve(4, 6, 0))
        assertEquals(1, LegacyPlayerSubroleCodeRule.resolve(4, 5, 0))
    }

    @Test
    fun unknownPositionReturnsWithoutOverwritingPreviousValue() {
        assertEquals(2, LegacyPlayerSubroleCodeRule.resolve(99, 7, 10, currentSubroleCode = 2))
    }
}

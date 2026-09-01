package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyCoachRawHRuleTest {
    @Test
    fun `constructor and employment reset H to eighty`() {
        assertEquals(80, LegacyCoachRawHRule.initialValue())
        assertEquals(80, LegacyCoachRawHRule.afterEmployment())
    }

    @Test
    fun `match deltas preserve home away result and large margin branches`() {
        assertEquals(50, LegacyCoachRawHRule.afterMatch(50, 1, 1, 1, managerIsAway = false))
        assertEquals(52, LegacyCoachRawHRule.afterMatch(50, 1, 1, 1, managerIsAway = true))

        assertEquals(53, LegacyCoachRawHRule.afterMatch(50, 1, 2, 1, managerIsAway = false))
        assertEquals(55, LegacyCoachRawHRule.afterMatch(50, 1, 2, 1, managerIsAway = true))
        assertEquals(56, LegacyCoachRawHRule.afterMatch(50, 1, 4, 1, managerIsAway = false))
        assertEquals(62, LegacyCoachRawHRule.afterMatch(50, 1, 4, 1, managerIsAway = true))

        assertEquals(45, LegacyCoachRawHRule.afterMatch(50, 1, 1, 2, managerIsAway = false))
        assertEquals(47, LegacyCoachRawHRule.afterMatch(50, 1, 1, 2, managerIsAway = true))
        assertEquals(40, LegacyCoachRawHRule.afterMatch(50, 1, 1, 4, managerIsAway = false))
        assertEquals(45, LegacyCoachRawHRule.afterMatch(50, 1, 1, 4, managerIsAway = true))
    }

    @Test
    fun `match caller gate excludes type seven and missing competition`() {
        assertEquals(17, LegacyCoachRawHRule.afterMatch(17, 7, 5, 0, managerIsAway = false))
        assertEquals(17, LegacyCoachRawHRule.afterMatch(17, null, 5, 0, managerIsAway = false))
        assertEquals(23, LegacyCoachRawHRule.afterMatch(20, 8, 1, 0, managerIsAway = false))
    }

    @Test
    fun `each H write clamps exactly like legacy h int`() {
        assertEquals(100, LegacyCoachRawHRule.afterMatch(98, 1, 4, 0, managerIsAway = true))
        assertEquals(0, LegacyCoachRawHRule.afterMatch(3, 1, 0, 4, managerIsAway = false))
        assertEquals(100, LegacyCoachRawHRule.afterAnnualRecovery(70))
        assertEquals(60, LegacyCoachRawHRule.afterAnnualRecovery(10))
    }

    @Test
    fun `main team refresh applies only the proven thirty floor`() {
        assertEquals(30, LegacyCoachRawHRule.afterMainTeamRefresh(12, legacyFloorEnabled = true))
        assertEquals(30, LegacyCoachRawHRule.afterMainTeamRefresh(30, legacyFloorEnabled = true))
        assertEquals(12, LegacyCoachRawHRule.afterMainTeamRefresh(12, legacyFloorEnabled = false))
        assertEquals(77, LegacyCoachRawHRule.afterMainTeamRefresh(77, legacyFloorEnabled = true))
    }
}

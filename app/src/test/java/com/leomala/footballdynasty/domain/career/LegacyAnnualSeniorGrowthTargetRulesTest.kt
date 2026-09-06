package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualSeniorGrowthTargetRulesTest {
    private fun calculate(
        r0: Boolean = true,
        o: Int = 1,
        p0: Int = 0,
        j: Int = 9,
        j0: Int = 0,
    ) = LegacyAnnualSeniorGrowthTargetRules.calculate(
        LegacyAnnualSeniorGrowthTargetRules.ClubState(
            legacyR0 = r0,
            legacyO = o,
            legacyP0 = p0,
            legacyJ = j,
            legacyJ0 = j0,
        ),
    )

    @Test
    fun `R0 baseline target preserves O matrix`() {
        assertEquals(30, calculate(o = 0).baselineTarget)
        assertEquals(100, calculate(o = 1).baselineTarget)
        assertEquals(60, calculate(o = 2).baselineTarget)
        assertEquals(40, calculate(o = 3).baselineTarget)
        assertEquals(30, calculate(o = 4).baselineTarget)
        assertEquals(20, calculate(o = 99).baselineTarget)
    }

    @Test
    fun `non R0 baseline target preserves p0 matrix`() {
        assertEquals(100, calculate(r0 = false, p0 = 5).baselineTarget)
        assertEquals(100, calculate(r0 = false, p0 = 4).baselineTarget)
        assertEquals(75, calculate(r0 = false, p0 = 3).baselineTarget)
        assertEquals(40, calculate(r0 = false, p0 = 2).baselineTarget)
        assertEquals(30, calculate(r0 = false, p0 = 1).baselineTarget)
        assertEquals(20, calculate(r0 = false, p0 = 0).baselineTarget)
    }

    @Test
    fun `J zero cap preserves all special j0 bands`() {
        assertEquals(95, calculate(j = 0, j0 = 3).clubSpecificCap)
        assertEquals(95, calculate(j = 0, j0 = 97).clubSpecificCap)
        assertEquals(90, calculate(j = 0, j0 = 154).clubSpecificCap)
        assertEquals(80, calculate(j = 0, j0 = 162).clubSpecificCap)
        assertEquals(70, calculate(j = 0, j0 = 999).clubSpecificCap)
    }

    @Test
    fun `J one cap preserves executable j0 branches`() {
        assertEquals(90, calculate(j = 1, j0 = 29).clubSpecificCap)
        assertEquals(90, calculate(j = 1, j0 = 11).clubSpecificCap)
        assertEquals(80, calculate(j = 1, j0 = 195).clubSpecificCap)
        assertEquals(80, calculate(j = 1, j0 = 46).clubSpecificCap)
        assertEquals(80, calculate(j = 1, j0 = 42).clubSpecificCap)
        assertEquals(70, calculate(j = 1, j0 = 151).clubSpecificCap)
        assertEquals(70, calculate(j = 1, j0 = 150).clubSpecificCap)
        assertEquals(60, calculate(j = 1, j0 = 999).clubSpecificCap)
    }

    @Test
    fun `J two and three caps preserve exact special sets`() {
        assertEquals(75, calculate(j = 2, j0 = 10).clubSpecificCap)
        assertEquals(70, calculate(j = 2, j0 = 141).clubSpecificCap)
        assertEquals(60, calculate(j = 2, j0 = 999).clubSpecificCap)

        assertEquals(75, calculate(j = 3, j0 = 107).clubSpecificCap)
        assertEquals(70, calculate(j = 3, j0 = 98).clubSpecificCap)
        assertEquals(60, calculate(j = 3, j0 = 999).clubSpecificCap)
    }

    @Test
    fun `J four five and default caps preserve exact branches`() {
        assertEquals(80, calculate(j = 4, j0 = 131).clubSpecificCap)
        assertEquals(70, calculate(j = 4, j0 = 68).clubSpecificCap)
        assertEquals(65, calculate(j = 4, j0 = 51).clubSpecificCap)
        assertEquals(55, calculate(j = 4, j0 = 999).clubSpecificCap)
        assertEquals(60, calculate(j = 5, j0 = 143).clubSpecificCap)
        assertEquals(45, calculate(j = 5, j0 = 999).clubSpecificCap)
        assertEquals(100, calculate(j = 9, j0 = 999).clubSpecificCap)
    }

    @Test
    fun `final target is minimum of baseline and club specific cap`() {
        val baselineWins = calculate(r0 = true, o = 0, j = 0, j0 = 3)
        assertEquals(30, baselineWins.baselineTarget)
        assertEquals(95, baselineWins.clubSpecificCap)
        assertEquals(30, baselineWins.cappedTarget)

        val clubCapWins = calculate(r0 = true, o = 1, j = 5, j0 = 999)
        assertEquals(100, clubCapWins.baselineTarget)
        assertEquals(45, clubCapWins.clubSpecificCap)
        assertEquals(45, clubCapWins.cappedTarget)
    }
}

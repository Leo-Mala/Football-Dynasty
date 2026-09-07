package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualSeniorProgressionRulesTest {
    @Test
    fun `no club preserves M N overall and consumes no rng`() {
        val random = SeededRandomSource(7L)
        val result = LegacyAnnualSeniorProgressionRules.apply(
            club = club(hasCurrentClub = false),
            player = player(age = 24, overall = 71, legacyN = 0.75, legacyMFlag = true, legacyD0 = 80),
            random = random,
        )

        assertEquals(71, result.overall)
        assertEquals(0.75, result.legacyN, 0.0)
        assertTrue(result.legacyMFlag)
        assertEquals(0L, random.draws)
    }

    @Test
    fun `growth composes accumulation target high d0 draw finalization then clears M`() {
        val random = SeededRandomSource(17L)
        val result = LegacyAnnualSeniorProgressionRules.apply(
            club = club(hasCurrentClub = true),
            player = player(age = 20, overall = 50, legacyN = 0.95, legacyMFlag = true, legacyD0 = 60),
            random = random,
        )

        assertEquals(51, result.overall)
        assertTrue(result.legacyN < 1.0)
        assertFalse(result.legacyMFlag)
        assertEquals(1L, random.draws)
    }

    @Test
    fun `decline uses retained N without rng and clears M after path`() {
        val random = SeededRandomSource(23L)
        val result = LegacyAnnualSeniorProgressionRules.apply(
            club = club(hasCurrentClub = true),
            player = player(age = 35, overall = 70, legacyN = 0.99, legacyMFlag = true, legacyD0 = 99),
            random = random,
        )

        assertEquals(69, result.overall)
        assertTrue(result.legacyN >= 0.0)
        assertFalse(result.legacyMFlag)
        assertEquals(0L, random.draws)
    }

    private fun club(hasCurrentClub: Boolean) = LegacyAnnualSeniorProgressionRules.ClubState(
        hasCurrentClub = hasCurrentClub,
        legacyF0 = 20,
        legacyR0 = true,
        legacyP0 = 4,
        legacyJ = 6,
        legacyJ0 = 0,
        legacyO = 1,
    )

    private fun player(
        age: Int,
        overall: Int,
        legacyN: Double,
        legacyMFlag: Boolean,
        legacyD0: Int,
    ) = LegacyAnnualSeniorProgressionRules.PlayerState(
        age = age,
        overall = overall,
        legacyN = legacyN,
        legacyMFlag = legacyMFlag,
        legacyD0 = legacyD0,
        legacyM = 0,
        legacyStar = false,
        legacyWorldTop = false,
    )
}

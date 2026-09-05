package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualSeniorDeclineRulesTest {
    @Test
    fun `club R0 false derives legacy tier from P0`() {
        val base = LegacyAnnualSeniorDeclineRules.Input(
            age = 40,
            overall = 80,
            legacyN = 0.9,
            clubO = 99,
            clubF0 = 0,
            clubR0 = false,
            clubP0 = 4,
        )

        val tierOne = LegacyAnnualSeniorDeclineRules.apply(base)
        assertEquals(79, tierOne.overall)
        assertEquals(0.17, tierOne.legacyN, 1e-12)

        val tierTwoFloor = LegacyAnnualSeniorDeclineRules.apply(
            base.copy(overall = 24, legacyN = 0.9, clubP0 = 3),
        )
        assertEquals(24, tierTwoFloor.overall)
        assertEquals(1.044, tierTwoFloor.legacyN, 1e-12)
    }

    @Test
    fun `club F0 twenty subtracts two years before weighting`() {
        val result = LegacyAnnualSeniorDeclineRules.apply(
            LegacyAnnualSeniorDeclineRules.Input(
                age = 33,
                overall = 60,
                legacyN = 0.0,
                clubO = 1,
                clubF0 = 20,
                clubR0 = true,
                clubP0 = 0,
            ),
        )

        assertEquals(60, result.overall)
        assertEquals(0.0, result.legacyN, 0.0)
    }

    @Test
    fun `overall bands preserve exact decline multipliers`() {
        val common = LegacyAnnualSeniorDeclineRules.Input(
            age = 36,
            overall = 50,
            legacyN = 0.0,
            clubO = 9,
            clubF0 = 0,
            clubR0 = true,
            clubP0 = 0,
        )

        assertEquals(0.08, LegacyAnnualSeniorDeclineRules.apply(common).legacyN, 1e-12)
        assertEquals(0.12, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 51)).legacyN, 1e-12)
        assertEquals(0.15, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 71)).legacyN, 1e-12)
        assertEquals(0.0, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 101)).legacyN, 0.0)
    }

    @Test
    fun `strict threshold does not decline at exactly one`() {
        val result = LegacyAnnualSeniorDeclineRules.apply(
            LegacyAnnualSeniorDeclineRules.Input(
                age = 36,
                overall = 50,
                legacyN = 0.92,
                clubO = 3,
                clubF0 = 0,
                clubR0 = true,
                clubP0 = 0,
            ),
        )

        assertEquals(50, result.overall)
        assertEquals(1.0, result.legacyN, 1e-12)
    }

    @Test
    fun `tier floor blocks decline without consuming accumulated N`() {
        val result = LegacyAnnualSeniorDeclineRules.apply(
            LegacyAnnualSeniorDeclineRules.Input(
                age = 40,
                overall = 35,
                legacyN = 0.9,
                clubO = 1,
                clubF0 = 0,
                clubR0 = true,
                clubP0 = 0,
            ),
        )

        assertEquals(35, result.overall)
        assertEquals(1.044, result.legacyN, 1e-12)
    }

    @Test
    fun `decline consumes exactly one accumulated point per annual call`() {
        val result = LegacyAnnualSeniorDeclineRules.apply(
            LegacyAnnualSeniorDeclineRules.Input(
                age = 48,
                overall = 90,
                legacyN = 2.0,
                clubO = 1,
                clubF0 = 0,
                clubR0 = true,
                clubP0 = 0,
            ),
        )

        assertEquals(89, result.overall)
        assertEquals(1.51, result.legacyN, 1e-12)
    }

    @Test
    fun `final overall clamp is unconditional`() {
        val result = LegacyAnnualSeniorDeclineRules.apply(
            LegacyAnnualSeniorDeclineRules.Input(
                age = 40,
                overall = 0,
                legacyN = 0.5,
                clubO = 1,
                clubF0 = 0,
                clubR0 = true,
                clubP0 = 0,
            ),
        )

        assertEquals(1, result.overall)
        assertEquals(0.5, result.legacyN, 0.0)
    }
}

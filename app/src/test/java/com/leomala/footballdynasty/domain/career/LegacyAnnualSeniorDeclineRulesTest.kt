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

        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 79, legacyN = 0.17),
            LegacyAnnualSeniorDeclineRules.apply(base),
        )

        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 24, legacyN = 1.02),
            LegacyAnnualSeniorDeclineRules.apply(
                base.copy(overall = 24, legacyN = 0.9, clubP0 = 3),
            ),
        )
    }

    @Test
    fun `club F0 twenty subtracts two years before weighting`() {
        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 60, legacyN = 0.0),
            LegacyAnnualSeniorDeclineRules.apply(
                LegacyAnnualSeniorDeclineRules.Input(
                    age = 33,
                    overall = 60,
                    legacyN = 0.0,
                    clubO = 1,
                    clubF0 = 20,
                    clubR0 = true,
                    clubP0 = 0,
                ),
            ),
        )
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

        assertEquals(0.08, LegacyAnnualSeniorDeclineRules.apply(common).legacyN, 0.0)
        assertEquals(0.12, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 51)).legacyN, 0.0)
        assertEquals(0.15, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 71)).legacyN, 0.0)
        assertEquals(0.0, LegacyAnnualSeniorDeclineRules.apply(common.copy(overall = 101)).legacyN, 0.0)
    }

    @Test
    fun `strict threshold does not decline at exactly one`() {
        val input = LegacyAnnualSeniorDeclineRules.Input(
            age = 36,
            overall = 50,
            legacyN = 0.92,
            clubO = 3,
            clubF0 = 0,
            clubR0 = true,
            clubP0 = 0,
        )

        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 50, legacyN = 1.0),
            LegacyAnnualSeniorDeclineRules.apply(input),
        )
    }

    @Test
    fun `tier floor blocks decline without consuming accumulated N`() {
        val input = LegacyAnnualSeniorDeclineRules.Input(
            age = 40,
            overall = 35,
            legacyN = 0.9,
            clubO = 1,
            clubF0 = 0,
            clubR0 = true,
            clubP0 = 0,
        )

        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 35, legacyN = 1.044),
            LegacyAnnualSeniorDeclineRules.apply(input),
        )
    }

    @Test
    fun `decline consumes exactly one accumulated point per annual call`() {
        val input = LegacyAnnualSeniorDeclineRules.Input(
            age = 48,
            overall = 90,
            legacyN = 2.0,
            clubO = 1,
            clubF0 = 0,
            clubR0 = true,
            clubP0 = 0,
        )

        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 89, legacyN = 1.51),
            LegacyAnnualSeniorDeclineRules.apply(input),
        )
    }

    @Test
    fun `final overall clamp is unconditional`() {
        assertEquals(
            LegacyAnnualSeniorDeclineRules.Result(overall = 1, legacyN = 0.5),
            LegacyAnnualSeniorDeclineRules.apply(
                LegacyAnnualSeniorDeclineRules.Input(
                    age = 40,
                    overall = 0,
                    legacyN = 0.5,
                    clubO = 1,
                    clubF0 = 0,
                    clubR0 = true,
                    clubP0 = 0,
                ),
            ),
        )
    }
}

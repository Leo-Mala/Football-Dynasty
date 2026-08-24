package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPlayerValueRulesTest {
    @Test
    fun `value applies legacy club flag position status and age factors in order`() {
        val result = LegacyPlayerValueRules.calculate(
            LegacyPlayerValueRules.Input(
                countryGroup = 0,
                clubLevel = 22,
                position = 4,
                status = 1,
                age = 18,
                overall = 80,
                star = true,
                worldTop = true,
                legacyCreatedYear = 0,
                currentYear = 2026,
                legacyHash = 8,
            )
        )

        assertEquals(18, result.normalizedAge)
        assertEquals(153_446_400, result.marketValue)
    }

    @Test
    fun `newly created player applies three percent projection then legacy hash`() {
        val result = LegacyPlayerValueRules.calculate(
            LegacyPlayerValueRules.Input(
                countryGroup = 1,
                clubLevel = 10,
                position = 3,
                status = 0,
                age = 15,
                overall = 50,
                star = false,
                worldTop = false,
                legacyCreatedYear = 2026,
                currentYear = 2026,
                legacyHash = 5,
            )
        )

        assertEquals(16, result.normalizedAge)
        assertEquals(1_197_000, result.marketValue)
    }

    @Test
    fun `negative senior age adjustment falls back to legacy minimum factor`() {
        val result = LegacyPlayerValueRules.calculate(
            LegacyPlayerValueRules.Input(
                countryGroup = 1,
                clubLevel = 10,
                position = 3,
                status = 0,
                age = 60,
                overall = 50,
                star = false,
                worldTop = false,
                legacyCreatedYear = 0,
                currentYear = 2026,
                legacyHash = 5,
            )
        )

        assertEquals(60, result.normalizedAge)
        assertEquals(600_000, result.marketValue)
    }
}

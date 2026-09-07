package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualNEmploymentRoutingRulesTest {
    @Test
    fun `legacy N1 false short circuits without calls`() {
        val calls = LegacyAnnualNEmploymentRoutingRules.plan(
            legacyN1 = false,
            employedFlag = true,
            entries = listOf(
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = false),
            ),
        )

        assertTrue(calls.isEmpty())
    }

    @Test
    fun `cS requires K true and legacy y null preserving source order`() {
        val calls = LegacyAnnualNEmploymentRoutingRules.plan(
            legacyN1 = true,
            employedFlag = false,
            entries = listOf(
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = false),
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = true),
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = false, hasLegacyY = false),
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = false),
            ),
        )

        assertEquals(listOf(0, 3), calls.map { it.sourceIndex })
        assertTrue(calls.all { !it.legacyArgument && it.overwritesLegacyG })
    }

    @Test
    fun `cSempregado ignores legacy y but still requires K true`() {
        val calls = LegacyAnnualNEmploymentRoutingRules.plan(
            legacyN1 = true,
            employedFlag = true,
            entries = listOf(
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = true),
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = false, hasLegacyY = false),
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = false),
            ),
        )

        assertEquals(listOf(0, 2), calls.map { it.sourceIndex })
    }

    @Test
    fun `each eligible call overwrites legacy g so last source entry wins final assignment`() {
        val calls = LegacyAnnualNEmploymentRoutingRules.plan(
            legacyN1 = true,
            employedFlag = true,
            entries = List(4) {
                LegacyAnnualNEmploymentRoutingRules.Entry(legacyK = true, hasLegacyY = false)
            },
        )

        assertEquals(listOf(0, 1, 2, 3), calls.map { it.sourceIndex })
        assertEquals(3, calls.last().sourceIndex)
        assertTrue(calls.all { it.overwritesLegacyG })
    }
}

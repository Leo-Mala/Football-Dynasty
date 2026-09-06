package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyAnnualNEmploymentTransientRulesTest {
    @Test
    fun `null or empty legacy g takes fallback without scanning K`() {
        assertEquals(
            LegacyAnnualNEmploymentTransientRules.KResult(
                route = LegacyAnnualNEmploymentTransientRules.KRoute.FALLBACK,
                firstLegacyKIndex = null,
            ),
            LegacyAnnualNEmploymentTransientRules.resolveK(
                legacyGSize = null,
                legacyKFlags = listOf(true),
            ),
        )
        assertEquals(
            LegacyAnnualNEmploymentTransientRules.KRoute.FALLBACK,
            LegacyAnnualNEmploymentTransientRules.resolveK(
                legacyGSize = 0,
                legacyKFlags = listOf(true),
            ).route,
        )
    }

    @Test
    fun `non empty legacy g takes invitation and records first K true source index`() {
        val result =
            LegacyAnnualNEmploymentTransientRules.resolveK(
                legacyGSize = 2,
                legacyKFlags = listOf(false, false, true, true),
            )

        assertEquals(LegacyAnnualNEmploymentTransientRules.KRoute.LEGACY_G_INVITATION, result.route)
        assertEquals(2, result.firstLegacyKIndex)
    }

    @Test
    fun `non empty legacy g still takes invitation when no K entry exists`() {
        val result =
            LegacyAnnualNEmploymentTransientRules.resolveK(
                legacyGSize = 1,
                legacyKFlags = listOf(false, false),
            )

        assertEquals(LegacyAnnualNEmploymentTransientRules.KRoute.LEGACY_G_INVITATION, result.route)
        assertNull(result.firstLegacyKIndex)
    }

    @Test
    fun `j clears legacy g strictly after b m and before j2 zero`() {
        assertEquals(
            listOf(
                LegacyAnnualNEmploymentTransientRules.JLifecycleAction.CALL_B_M,
                LegacyAnnualNEmploymentTransientRules.JLifecycleAction.CLEAR_LEGACY_G,
                LegacyAnnualNEmploymentTransientRules.JLifecycleAction.CALL_B_J2_ZERO,
            ),
            LegacyAnnualNEmploymentTransientRules.planJLifecycle(),
        )
    }
}

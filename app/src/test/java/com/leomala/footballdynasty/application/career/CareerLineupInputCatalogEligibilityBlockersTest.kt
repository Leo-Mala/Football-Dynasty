package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore.MatchPreparationBlocker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerLineupInputCatalogEligibilityBlockersTest {
    private val unresolved = MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED
    private val runtime = MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED

    @Test
    fun `all known K0 projections remove only aggregate eligibility blocker`() {
        val resolved = CareerLineupInputCatalogStore.resolveLineupEligibilityBlockers(
            blockers = linkedSetOf(unresolved, runtime),
            eligibilityProjections = listOf(true, false, true),
        )

        assertFalse(unresolved in resolved)
        assertTrue(runtime in resolved)
    }

    @Test
    fun `unknown K0 projection keeps eligibility fail closed`() {
        val resolved = CareerLineupInputCatalogStore.resolveLineupEligibilityBlockers(
            blockers = linkedSetOf(unresolved, runtime),
            eligibilityProjections = listOf(true, null, false),
        )

        assertTrue(unresolved in resolved)
        assertTrue(runtime in resolved)
    }

    @Test
    fun `empty prepared roster cannot resolve aggregate eligibility`() {
        val resolved = CareerLineupInputCatalogStore.resolveLineupEligibilityBlockers(
            blockers = linkedSetOf(unresolved, runtime),
            eligibilityProjections = emptyList(),
        )

        assertTrue(unresolved in resolved)
        assertTrue(runtime in resolved)
    }
}

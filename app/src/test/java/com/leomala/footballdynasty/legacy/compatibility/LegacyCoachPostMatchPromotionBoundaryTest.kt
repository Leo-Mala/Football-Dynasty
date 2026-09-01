package com.leomala.footballdynasty.legacy.compatibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LegacyCoachPostMatchPromotionBoundaryTest {
    @Test
    fun `locks exact reachable post-match evidence surface`() {
        assertEquals("best.s.f()", LegacyCoachPostMatchPromotionBoundary.callerMethod)
        assertEquals("best.f0.i(best.s)", LegacyCoachPostMatchPromotionBoundary.homeManagerMethod)
        assertEquals("best.f0.j(best.s)", LegacyCoachPostMatchPromotionBoundary.pairedManagerMethod)
        assertEquals(
            linkedSetOf(1, 2, 3, 4, 5, 6, 8),
            LegacyCoachPostMatchPromotionBoundary.characterizedHCompetitionTypes,
        )
        assertTrue(LegacyCoachPostMatchPromotionBoundary.hProjectionCharacterized)
    }

    @Test
    fun `keeps production persistence fail closed until complete i j lifecycle is characterized`() {
        assertFalse(LegacyCoachPostMatchPromotionBoundary.completeLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.productionPersistenceAllowed())
    }
}

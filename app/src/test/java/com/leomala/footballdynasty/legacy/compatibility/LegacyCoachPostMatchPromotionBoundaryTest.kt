package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCoachPostMatchPromotionBoundaryTest {
    @Test
    fun `locks exact reachable post-match caller order`() {
        assertEquals("best.s.f()", LegacyCoachPostMatchPromotionBoundary.callerMethod)
        assertEquals("best.f0.i(best.s)", LegacyCoachPostMatchPromotionBoundary.postMatchAdjustmentMethod)
        assertEquals("best.f0.j(best.s)", LegacyCoachPostMatchPromotionBoundary.postMatchStatisticsMethod)
        assertEquals(
            listOf("best.f0.j(best.s)", "best.f0.i(best.s)"),
            LegacyCoachPostMatchPromotionBoundary.characterizedCallerOrder,
        )
        assertEquals(
            linkedSetOf(1, 2, 3, 4, 5, 6, 8),
            LegacyCoachPostMatchPromotionBoundary.characterizedHCompetitionTypes,
        )
        assertTrue(LegacyCoachPostMatchPromotionBoundary.hProjectionCharacterized)
    }

    @Test
    fun `requires recovered j i Java smali evidence in exact caller order before lifecycle promotion`() {
        assertEquals(
            LegacyCoachPostMatchPromotionBoundary.characterizedCallerOrder,
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map {
                "${it.legacyClassName}.${it.methodSignature}"
            },
        )
        assertEquals(
            listOf("j(best.s)", "i(best.s)"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.methodSignature },
        )
        assertEquals(
            listOf("j(Lbest/s;)V", "i(Lbest/s;)V"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.smaliMethodSignature },
        )
        assertEquals(
            listOf("best/f0.smali", "best/f0.smali"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.smaliFileName },
        )
        assertTrue(LegacyCoachPostMatchPromotionBoundary.recoveredManagerMethodEvidenceComplete)
    }

    @Test
    fun `keeps production persistence fail closed after structural recovery until semantics are promoted`() {
        assertFalse(LegacyCoachPostMatchPromotionBoundary.semanticLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.completeLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.productionPersistenceAllowed())
    }
}

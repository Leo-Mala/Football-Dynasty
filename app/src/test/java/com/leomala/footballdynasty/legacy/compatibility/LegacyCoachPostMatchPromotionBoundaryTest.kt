package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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
    fun `requires exact recovered i j Java smali evidence before lifecycle promotion`() {
        assertEquals(
            listOf("i(best.s)", "j(best.s)"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.methodSignature },
        )
        assertEquals(
            listOf("i(Lbest/s;)V", "j(Lbest/s;)V"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.smaliMethodSignature },
        )
        assertEquals(
            listOf("best/f0.smali", "best/f0.smali"),
            LegacyCoachPostMatchPromotionBoundary.requiredRecoveredManagerMethods.map { it.smaliFileName },
        )
        assertFalse(LegacyCoachPostMatchPromotionBoundary.recoveredManagerMethodEvidenceComplete)
    }

    @Test
    fun `keeps production persistence fail closed until structural and semantic i j evidence is complete`() {
        assertFalse(LegacyCoachPostMatchPromotionBoundary.semanticLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.completeLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.productionPersistenceAllowed())
    }
}

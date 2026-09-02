package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class LegacyCoachPostMatchSemanticEvidenceTest {
    @Test
    fun `locks proven mutation families without claiming full field ordering`() {
        val adjustment =
            assertNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.i(best.s)"),
            ).let { LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.i(best.s)")!! }
        val statistics =
            assertNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.j(best.s)"),
            ).let { LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.j(best.s)")!! }

        assertEquals(
            linkedSetOf(
                LegacyCoachPostMatchMutationFamily.RAW_G,
                LegacyCoachPostMatchMutationFamily.RAW_H,
            ),
            adjustment.mutationFamilies,
        )
        assertEquals(
            linkedSetOf(
                LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
            ),
            statistics.mutationFamilies,
        )
        assertFalse(adjustment.completeFieldOrderingRecovered)
        assertFalse(statistics.completeFieldOrderingRecovered)
    }

    @Test
    fun `semantic promotion remains fail closed until every required method is complete`() {
        assertFalse(
            LegacyCoachPostMatchSemanticEvidence.completeFor(
                listOf("best.f0.j(best.s)", "best.f0.i(best.s)"),
            ),
        )
        assertFalse(LegacyCoachPostMatchPromotionBoundary.semanticLifecycleCharacterized)
        assertFalse(LegacyCoachPostMatchPromotionBoundary.productionPersistenceAllowed())
    }

    @Test
    fun `unknown or empty required method sets cannot accidentally promote persistence`() {
        assertFalse(LegacyCoachPostMatchSemanticEvidence.completeFor(emptyList()))
        assertFalse(
            LegacyCoachPostMatchSemanticEvidence.completeFor(
                listOf("best.f0.unknown(best.s)"),
            ),
        )
    }
}

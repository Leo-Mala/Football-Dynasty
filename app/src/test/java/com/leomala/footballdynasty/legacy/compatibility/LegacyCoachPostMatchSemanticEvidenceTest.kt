package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCoachPostMatchSemanticEvidenceTest {
    @Test
    fun `locks proven mutation families without claiming full field ordering`() {
        val adjustment =
            requireNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.i(best.s)"),
            )
        val statistics =
            requireNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.j(best.s)"),
            )

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
        assertFalse(adjustment.semanticallyComplete)
        assertFalse(statistics.semanticallyComplete)
    }

    @Test
    fun `records H as characterized while G and j mutation families remain unresolved`() {
        val adjustment =
            requireNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.i(best.s)"),
            )
        val statistics =
            requireNotNull(
                LegacyCoachPostMatchSemanticEvidence.findExact("best.f0.j(best.s)"),
            )

        assertEquals(
            linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_H),
            adjustment.fullyCharacterizedMutationFamilies,
        )
        assertEquals(
            linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_G),
            adjustment.unresolvedMutationFamilies,
        )
        assertEquals(
            linkedSetOf(
                LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
            ),
            statistics.unresolvedMutationFamilies,
        )
        assertTrue(
            adjustment.mutationFamilyCharacterized(
                LegacyCoachPostMatchMutationFamily.RAW_H,
            ),
        )
        assertFalse(
            adjustment.mutationFamilyCharacterized(
                LegacyCoachPostMatchMutationFamily.RAW_G,
            ),
        )
        assertTrue(statistics.fullyCharacterizedMutationFamilies.isEmpty())
    }

    @Test
    fun `recovery remainder follows exact caller order and keeps unknown methods explicit`() {
        val unresolved =
            LegacyCoachPostMatchSemanticEvidence.unresolvedFor(
                listOf(
                    "best.f0.j(best.s)",
                    "best.f0.i(best.s)",
                    "best.f0.unknown(best.s)",
                ),
            )

        assertEquals(
            listOf(
                "best.f0.j(best.s)",
                "best.f0.i(best.s)",
                "best.f0.unknown(best.s)",
            ),
            unresolved.keys.toList(),
        )
        assertEquals(
            linkedSetOf(
                LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
            ),
            unresolved["best.f0.j(best.s)"],
        )
        assertEquals(
            linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_G),
            unresolved["best.f0.i(best.s)"],
        )
        assertTrue(unresolved.containsKey("best.f0.unknown(best.s)"))
        assertNull(unresolved["best.f0.unknown(best.s)"])
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot characterize a mutation family outside the proven method surface`() {
        LegacyCoachPostMatchMethodSemanticEvidence(
            legacyMethod = "best.f0.i(best.s)",
            mutationFamilies = linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_H),
            fullyCharacterizedMutationFamilies =
                linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_G),
            completeFieldOrderingRecovered = false,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reachable method cannot become complete with an empty proven mutation surface`() {
        LegacyCoachPostMatchMethodSemanticEvidence(
            legacyMethod = "best.f0.i(best.s)",
            mutationFamilies = emptySet(),
            fullyCharacterizedMutationFamilies = emptySet(),
            completeFieldOrderingRecovered = true,
        )
    }

    @Test
    fun `semantic catalog method identities remain unique`() {
        assertEquals(
            LegacyCoachPostMatchSemanticEvidence.methods.size,
            LegacyCoachPostMatchSemanticEvidence.methods.map { it.legacyMethod }.toSet().size,
        )
    }

    @Test
    fun `ordering alone cannot promote a partially characterized method`() {
        val partial =
            LegacyCoachPostMatchMethodSemanticEvidence(
                legacyMethod = "best.f0.i(best.s)",
                mutationFamilies =
                    linkedSetOf(
                        LegacyCoachPostMatchMutationFamily.RAW_G,
                        LegacyCoachPostMatchMutationFamily.RAW_H,
                    ),
                fullyCharacterizedMutationFamilies =
                    linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_H),
                completeFieldOrderingRecovered = true,
            )

        assertFalse(partial.semanticallyComplete)
        assertEquals(
            linkedSetOf(LegacyCoachPostMatchMutationFamily.RAW_G),
            partial.unresolvedMutationFamilies,
        )
    }

    @Test
    fun `semantic completeness requires all mutation families and complete ordering`() {
        val complete =
            LegacyCoachPostMatchMethodSemanticEvidence(
                legacyMethod = "best.f0.i(best.s)",
                mutationFamilies =
                    linkedSetOf(
                        LegacyCoachPostMatchMutationFamily.RAW_G,
                        LegacyCoachPostMatchMutationFamily.RAW_H,
                    ),
                fullyCharacterizedMutationFamilies =
                    linkedSetOf(
                        LegacyCoachPostMatchMutationFamily.RAW_G,
                        LegacyCoachPostMatchMutationFamily.RAW_H,
                    ),
                completeFieldOrderingRecovered = true,
            )

        assertTrue(complete.unresolvedMutationFamilies.isEmpty())
        assertTrue(complete.semanticallyComplete)
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

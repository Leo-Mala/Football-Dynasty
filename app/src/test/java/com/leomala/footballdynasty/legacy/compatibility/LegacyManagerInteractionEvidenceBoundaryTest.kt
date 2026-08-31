package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerInteractionEvidenceBoundaryTest {
    @Test
    fun everyReachableManagerInteractionHasItsRecoveredHostMethodBound() {
        assertEquals(
            LegacyManagerInteractionEvidenceCatalog.confirmed,
            LegacyManagerInteractionEvidenceBoundary.recoveredHostMethods.keys,
        )

        LegacyManagerInteractionEvidenceCatalog.confirmed.forEach { interaction ->
            val recovered = requireNotNull(
                LegacyManagerInteractionEvidenceBoundary.recoveredHostMethodFor(interaction),
            )
            assertEquals(interaction.legacyClassName, recovered.legacyClassName)
        }
    }

    @Test
    fun recoveredHostBodiesDoNotUnlockUncharacterizedManagerSemantics() {
        assertEquals(
            LegacyManagerInteractionEvidenceCatalog.confirmed,
            LegacyManagerInteractionEvidenceBoundary.semanticRuntimeBlockedInteractions,
        )
        LegacyManagerInteractionEvidenceCatalog.confirmed.forEach { interaction ->
            assertTrue(
                LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(interaction),
            )
        }
    }

    @Test
    fun exactRecoveredMethodsMatchTheVersionedSmaliInventory() {
        assertEquals(
            "a(a.p,a.ac,int)",
            LegacyManagerInteractionEvidenceBoundary
                .recoveredHostMethodFor(LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL)
                ?.methodSignature,
        )
        assertEquals(
            "onCreate(Bundle)",
            LegacyManagerInteractionEvidenceBoundary
                .recoveredHostMethodFor(LegacyManagerInteractionEvidence.PLAYER_CONTRACT)
                ?.methodSignature,
        )
        assertEquals(
            "a(a.p,a.ac,int)",
            LegacyManagerInteractionEvidenceBoundary
                .recoveredHostMethodFor(LegacyManagerInteractionEvidence.TEAM_PROPOSAL)
                ?.methodSignature,
        )
        assertEquals(
            "onStart()",
            LegacyManagerInteractionEvidenceBoundary
                .recoveredHostMethodFor(LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER)
                ?.methodSignature,
        )
    }
}

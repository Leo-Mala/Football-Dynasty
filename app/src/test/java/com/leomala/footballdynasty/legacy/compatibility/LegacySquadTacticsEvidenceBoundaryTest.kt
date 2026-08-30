package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySquadTacticsEvidenceBoundaryTest {
    @Test
    fun onlyRecoveredLineupAndTacticsMethodsEnterThePhase11Boundary() {
        assertEquals(
            setOf(
                "DialogTatics" to "onCreate(Bundle)",
                "ActivityEscala" to "gL()",
                "ActivitySavedTatics" to "sa()",
            ),
            LegacySquadTacticsEvidenceBoundary.recoveredMethodsAwaitingSemanticCharacterization
                .map { evidence -> evidence.legacyClassName to evidence.methodSignature }
                .toSet(),
        )
    }

    @Test
    fun semanticTargetsMatchTheProvenLegacySurfacesAndRecoveredMethods() {
        assertEquals(
            listOf(
                LegacySquadTacticsEvidenceBoundary.SemanticTarget(
                    "ActivityEscala",
                    "gL()",
                    "lineup",
                ),
                LegacySquadTacticsEvidenceBoundary.SemanticTarget(
                    "DialogTatics",
                    "onCreate(Bundle)",
                    "tactics",
                ),
                LegacySquadTacticsEvidenceBoundary.SemanticTarget(
                    "ActivitySavedTatics",
                    "sa()",
                    "saved-tactics",
                ),
            ),
            LegacySquadTacticsEvidenceBoundary.requiredSemanticTargets,
        )
        assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveRecoveredBodies())
    }

    @Test
    fun recoveredBodiesStayBlockedUntilGameplaySemanticsAreCharacterized() {
        assertTrue(
            LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked(
                "DialogTatics",
                "onCreate(Bundle)",
            ),
        )
        assertTrue(
            LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked(
                "ActivityEscala",
                "gL()",
            ),
        )
        assertTrue(
            LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked(
                "ActivitySavedTatics",
                "sa()",
            ),
        )
    }

    @Test
    fun unrelatedRecoveredManagerMethodsDoNotUnlockPhase11Semantics() {
        assertFalse(
            LegacySquadTacticsEvidenceBoundary.isRecoveredPhase11Method(
                "ActivityEstadio",
                "onCreate(Bundle)",
            ),
        )
        assertFalse(
            LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked(
                "DialogTatics",
                "unknown()",
            ),
        )
    }
}

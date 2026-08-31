package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    fun semanticTargetsCarryOnlyRecoveredStructuralEvidence() {
        val targets = LegacySquadTacticsEvidenceBoundary.requiredSemanticTargets

        assertEquals(3, targets.size)
        assertEquals(
            listOf("ActivityEscala", "DialogTatics", "ActivitySavedTatics"),
            targets.map { it.legacyClassName },
        )
        assertTrue(
            targets.all { target ->
                target.characterizationState ==
                    LegacySquadTacticsEvidenceBoundary.CharacterizationState.RECOVERED_BODY_ONLY
            },
        )
        assertTrue(targets.all(LegacySquadTacticsEvidenceBoundary::recoveryMetadataMatchesInventory))
        assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveRecoveredBodies())
        assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveConsistentSurfaceEvidence())
        assertFalse(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsAreSemanticallyCharacterized())
    }

    @Test
    fun activityMapSurfaceEvidenceIsLockedWithoutInventingTacticalSemantics() {
        val lineup = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscala", "gL()"),
        )
        val tactics = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)"),
        )
        val saved = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "sa()"),
        )

        assertEquals("lineup", lineup.surfaceRole)
        assertEquals("activity_escala", lineup.observedLayoutName)
        assertFalse(lineup.surfaceIsDynamicallyConstructed)

        assertEquals("tactics", tactics.surfaceRole)
        assertNull(tactics.observedLayoutName)
        assertTrue(tactics.surfaceIsDynamicallyConstructed)

        assertEquals("saved-tactics", saved.surfaceRole)
        assertEquals("activity_savedtatics", saved.observedLayoutName)
        assertFalse(saved.surfaceIsDynamicallyConstructed)
    }

    @Test
    fun recoveredMetadataMatchesTheVersionedSmaliInventoryExactly() {
        val lineup = LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscala", "gL()")
        val tactics = LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)")
        val saved = LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "sa()")

        assertNotNull(lineup)
        assertNotNull(tactics)
        assertNotNull(saved)

        assertEquals("ActivityEscala.smali", lineup!!.smaliFileName)
        assertEquals(223, lineup.instructionCount)
        assertEquals(22, lineup.branchCount)

        assertEquals("DialogTatics.smali", tactics!!.smaliFileName)
        assertEquals(172, tactics.instructionCount)
        assertEquals(20, tactics.branchCount)

        assertEquals("ActivitySavedTatics.smali", saved!!.smaliFileName)
        assertEquals(115, saved.instructionCount)
        assertEquals(9, saved.branchCount)
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

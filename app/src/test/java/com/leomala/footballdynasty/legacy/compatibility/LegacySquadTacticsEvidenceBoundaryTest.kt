package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySquadTacticsEvidenceBoundaryTest {
    @Test
    fun provenSquadPrimitivesStayNarrowerThanLineupAndTacticsSemantics() {
        assertEquals(
            setOf(
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.SENIOR_ROSTER_MEMBERSHIP,
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.SOURCE_ORDER_PRESERVATION,
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.OPAQUE_POSITION_CODE,
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.OPAQUE_STATUS_CODE,
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.OPAQUE_SIDE_CODE,
                LegacySquadTacticsEvidenceBoundary.ProvenSquadPrimitive.OPAQUE_TRAIT_FIELDS,
            ),
            LegacySquadTacticsEvidenceBoundary.provenSquadPrimitives,
        )
        assertFalse(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsAreSemanticallyCharacterized())
    }

    @Test
    fun historicalLineupNamesAreRejectedByTheOfficialBoundary() {
        assertNull(LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscala", "gL()"))
        assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscala", "gL()"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao", "B()"))
    }

    @Test
    fun onlyCurrentOfficialLineupAndTacticsMethodsEnterThePhase11Boundary() {
        assertEquals(
            setOf(
                "DialogTatics" to "onCreate(Bundle)",
                "ActivityEscalacao" to "B()",
                "ActivitySavedTatics" to "g()",
            ),
            LegacySquadTacticsEvidenceBoundary.recoveredMethodsAwaitingSemanticCharacterization
                .map { evidence -> evidence.legacyClassName to evidence.methodSignature }
                .toSet(),
        )
    }

    @Test
    fun semanticTargetsCarryCurrentOfficialStructuralEvidence() {
        val targets = LegacySquadTacticsEvidenceBoundary.requiredSemanticTargets

        assertEquals(3, targets.size)
        assertEquals(
            listOf("ActivityEscalacao", "DialogTatics", "ActivitySavedTatics"),
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
    fun officialSurfaceEvidenceIsLockedWithoutInventingTacticalSemantics() {
        val lineup = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscalacao", "B()"),
        )
        val tactics = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)"),
        )
        val saved = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "g()"),
        )

        assertEquals("lineup", lineup.surfaceRole)
        assertEquals("activity_escala", lineup.observedLayoutName)
        assertFalse(lineup.surfaceIsDynamicallyConstructed)
        assertEquals("y()V", lineup.smaliMethodSignature)

        assertEquals("tactics", tactics.surfaceRole)
        assertNull(tactics.observedLayoutName)
        assertTrue(tactics.surfaceIsDynamicallyConstructed)

        assertEquals("saved-tactics", saved.surfaceRole)
        assertEquals("activity_savedtatics", saved.observedLayoutName)
        assertFalse(saved.surfaceIsDynamicallyConstructed)
    }

    @Test
    fun recoveredMetadataMatchesTheOfficialPhase4rCorpusExactly() {
        val lineup = LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscalacao", "B()")
        val tactics = LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)")
        val saved = LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "g()")

        assertNotNull(lineup)
        assertNotNull(tactics)
        assertNotNull(saved)

        assertEquals("ActivityEscalacao.smali", lineup!!.smaliFileName)
        assertEquals("y()V", lineup.smaliMethodSignature)
        assertEquals(212, lineup.instructionCount)
        assertEquals(22, lineup.branchCount)

        assertEquals("DialogTatics.smali", tactics!!.smaliFileName)
        assertEquals(171, tactics.instructionCount)
        assertEquals(19, tactics.branchCount)

        assertEquals("ActivitySavedTatics.smali", saved!!.smaliFileName)
        assertEquals(103, saved.instructionCount)
        assertEquals(8, saved.branchCount)
    }

    @Test
    fun recoveredBodiesStayBlockedUntilGameplaySemanticsAreCharacterized() {
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("DialogTatics", "onCreate(Bundle)"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao", "B()"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivitySavedTatics", "g()"))
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

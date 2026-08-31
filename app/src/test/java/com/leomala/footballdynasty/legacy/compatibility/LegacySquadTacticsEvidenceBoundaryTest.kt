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
    }

    @Test
    fun readableSwapSubpathsAreCharacterizedWithoutUnlockingTheLargeLineupBody() {
        assertEquals(
            setOf(
                LegacyCharacterizedLineupRuntimePath.BENCH_REORDER_U,
                LegacyCharacterizedLineupRuntimePath.STARTER_BENCH_SWAP_V,
                LegacyCharacterizedLineupRuntimePath.STARTER_REORDER_W,
            ),
            LegacySquadTacticsEvidenceBoundary.characterizedLineupRuntimePaths,
        )
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao", "B()"))
    }

    @Test
    fun subroleCandidateSelectionAndSavedTacticCreationAreCharacterized() {
        assertEquals(
            setOf(
                LegacyCharacterizedTacticsRuntimePath.PLAYER_SUBROLE_DERIVATION_R1,
                LegacyCharacterizedTacticsRuntimePath.ACTION_CANDIDATE_SELECTION_E,
                LegacyCharacterizedTacticsRuntimePath.SAVED_TACTIC_CREATE_G,
            ),
            LegacySquadTacticsEvidenceBoundary.characterizedTacticsRuntimePaths,
        )
        LegacySquadTacticsEvidenceBoundary.characterizedTacticsRuntimePaths.forEach { path ->
            assertTrue(LegacySquadTacticsEvidenceBoundary.isCharacterizedTacticsRuntimePath(path))
        }
    }

    @Test
    fun savedTacticsGIsFullyCharacterizedWhileLargeLineupAndTacticsHostsRemainBlocked() {
        assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivitySavedTatics", "g()"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao", "B()"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("DialogTatics", "onCreate(Bundle)"))
        assertFalse(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsAreSemanticallyCharacterized())
    }

    @Test
    fun historicalLineupNamesAreRejectedByTheOfficialBoundary() {
        assertNull(LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscala", "gL()"))
        assertFalse(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscala", "gL()"))
        assertTrue(LegacySquadTacticsEvidenceBoundary.isSemanticRuntimeBlocked("ActivityEscalacao", "B()"))
    }

    @Test
    fun onlyUncharacterizedCurrentOfficialHostsRemainInAwaitingSet() {
        assertEquals(
            setOf(
                "DialogTatics" to "onCreate(Bundle)",
                "ActivityEscalacao" to "B()",
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
        assertTrue(targets.all(LegacySquadTacticsEvidenceBoundary::recoveryMetadataMatchesInventory))
        assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveRecoveredBodies())
        assertTrue(LegacySquadTacticsEvidenceBoundary.allRequiredTargetsHaveConsistentSurfaceEvidence())
        assertEquals(
            LegacySquadTacticsEvidenceBoundary.CharacterizationState.SEMANTICS_CHARACTERIZED,
            requireNotNull(LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "g()")).characterizationState,
        )
    }

    @Test
    fun officialSurfaceEvidenceIsLockedWithoutInventingRemainingTacticalSemantics() {
        val lineup = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscalacao", "B()"),
        )
        val tactics = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)"),
        )
        val saved = requireNotNull(
            LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "g()"),
        )

        assertEquals("activity_escala", lineup.observedLayoutName)
        assertEquals("y()V", lineup.smaliMethodSignature)
        assertNull(tactics.observedLayoutName)
        assertTrue(tactics.surfaceIsDynamicallyConstructed)
        assertEquals("activity_savedtatics", saved.observedLayoutName)
    }

    @Test
    fun recoveredMetadataMatchesTheOfficialPhase4rCorpusExactly() {
        val lineup = LegacySquadTacticsEvidenceBoundary.findTarget("ActivityEscalacao", "B()")
        val tactics = LegacySquadTacticsEvidenceBoundary.findTarget("DialogTatics", "onCreate(Bundle)")
        val saved = LegacySquadTacticsEvidenceBoundary.findTarget("ActivitySavedTatics", "g()")

        assertNotNull(lineup)
        assertNotNull(tactics)
        assertNotNull(saved)
        assertEquals(212, lineup!!.instructionCount)
        assertEquals(22, lineup.branchCount)
        assertEquals(171, tactics!!.instructionCount)
        assertEquals(19, tactics.branchCount)
        assertEquals(103, saved!!.instructionCount)
        assertEquals(8, saved.branchCount)
    }
}

package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerInteractionEvidenceBoundaryTest {
    @Test
    fun everyReachableManagerInteractionHasItsCurrentOfficialHostMethodBound() {
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
    fun onlySemanticallyCharacterizedInteractionsAreUnlockedFromTheBoundary() {
        val characterized = setOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
            LegacyManagerInteractionEvidence.PLAYER_SALE,
        )
        assertEquals(characterized, LegacyManagerInteractionEvidenceBoundary.semanticRuntimeCharacterizedInteractions)
        characterized.forEach { interaction ->
            assertTrue(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeCharacterized(interaction))
            assertFalse(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(interaction))
        }

        val stillBlocked = LegacyManagerInteractionEvidenceCatalog.confirmed - characterized
        assertEquals(stillBlocked, LegacyManagerInteractionEvidenceBoundary.semanticRuntimeBlockedInteractions)
        stillBlocked.forEach { interaction ->
            assertFalse(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeCharacterized(interaction))
            assertTrue(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(interaction))
        }
    }

    @Test
    fun characterizedPlayerDialogSubpathsUnlockSaleButNotRetirement() {
        val characterizedPaths = setOf(
            LegacyCharacterizedPlayerDialogRuntimePath.CONTRACT_RENEWAL,
            LegacyCharacterizedPlayerDialogRuntimePath.LOAN_MANAGEMENT,
            LegacyCharacterizedPlayerDialogRuntimePath.PLAYER_SALE,
        )
        assertEquals(characterizedPaths, LegacyManagerInteractionEvidenceBoundary.characterizedPlayerDialogRuntimePaths)
        characterizedPaths.forEach { path ->
            assertTrue(LegacyManagerInteractionEvidenceBoundary.isCharacterizedPlayerDialogRuntimePath(path))
        }
        assertFalse(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyManagerInteractionEvidence.PLAYER_SALE))
        assertTrue(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyManagerInteractionEvidence.PLAYER_RETIREMENT))
        assertTrue(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyManagerInteractionEvidence.TEAM_PROPOSAL))
        assertTrue(LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER))
    }

    @Test
    fun exactSaleChainIsLockedSeparatelyFromTheDialogHost() {
        assertEquals(11, LegacyManagerInteractionEvidenceBoundary.characterizedPlayerSaleMethods.size)
        assertEquals(
            listOf(
                72 to 7,
                29 to 0,
                18 to 0,
                45 to 7,
                17 to 0,
                9 to 1,
                621 to 86,
                84 to 20,
                321 to 42,
                83 to 9,
                77 to 16,
            ),
            LegacyManagerInteractionEvidenceBoundary.characterizedPlayerSaleMethods.map {
                it.instructionCount to it.branchCount
            },
        )
    }

    @Test
    fun exactRecoveredMethodsMatchTheOfficialPhase4rCorpus() {
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            methodSignature = "t(best.o,best.c0,int)",
            smaliMethodSignature = "t(Lbest/o;Lbest/c0;I)I",
            instructionCount = 136,
            branchCount = 14,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
            methodSignature = "onCreate(Bundle)",
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
            instructionCount = 530,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_SALE,
            methodSignature = "onCreate(Bundle)",
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
            instructionCount = 530,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_RETIREMENT,
            methodSignature = "onCreate(Bundle)",
            smaliMethodSignature = "onCreate(Landroid/os/Bundle;)V",
            instructionCount = 530,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.TEAM_PROPOSAL,
            methodSignature = "s(best.o,best.c0,int)",
            smaliMethodSignature = "s(Lbest/o;Lbest/c0;I)I",
            instructionCount = 133,
            branchCount = 14,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER,
            methodSignature = "onStart()",
            smaliMethodSignature = "onStart()V",
            instructionCount = 93,
            branchCount = 15,
        )
    }

    private fun assertRecovered(
        interaction: LegacyManagerInteractionEvidence,
        methodSignature: String,
        smaliMethodSignature: String,
        instructionCount: Int,
        branchCount: Int,
    ) {
        val recovered = requireNotNull(
            LegacyManagerInteractionEvidenceBoundary.recoveredHostMethodFor(interaction),
        )
        assertEquals(methodSignature, recovered.methodSignature)
        assertEquals(smaliMethodSignature, recovered.smaliMethodSignature)
        assertEquals(instructionCount, recovered.instructionCount)
        assertEquals(branchCount, recovered.branchCount)
    }
}

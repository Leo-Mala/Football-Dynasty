package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun onlySemanticallyCharacterizedInteractionsAreUnlockedFromTheBoundary() {
        val characterized = setOf(
            LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
        )
        assertEquals(
            characterized,
            LegacyManagerInteractionEvidenceBoundary.semanticRuntimeCharacterizedInteractions,
        )
        characterized.forEach { interaction ->
            assertTrue(
                LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeCharacterized(interaction),
            )
            assertFalse(
                LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(interaction),
            )
        }

        val stillBlocked = LegacyManagerInteractionEvidenceCatalog.confirmed - characterized
        assertEquals(
            stillBlocked,
            LegacyManagerInteractionEvidenceBoundary.semanticRuntimeBlockedInteractions,
        )
        stillBlocked.forEach { interaction ->
            assertFalse(
                LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeCharacterized(interaction),
            )
            assertTrue(
                LegacyManagerInteractionEvidenceBoundary.isSemanticRuntimeBlocked(interaction),
            )
        }
    }

    @Test
    fun exactRecoveredMethodsMatchTheVersionedSmaliInventory() {
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_SEARCH_PROPOSAL,
            methodSignature = "a(a.p,a.ac,int)",
            smaliFileName = "ActivityProcura.smali",
            instructionCount = 2,
            branchCount = 0,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_CONTRACT,
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogIgrokInfo.smali",
            instructionCount = 554,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_SALE,
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogIgrokInfo.smali",
            instructionCount = 554,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.PLAYER_RETIREMENT,
            methodSignature = "onCreate(Bundle)",
            smaliFileName = "DialogIgrokInfo.smali",
            instructionCount = 554,
            branchCount = 28,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.TEAM_PROPOSAL,
            methodSignature = "a(a.p,a.ac,int)",
            smaliFileName = "ActivityTimes.smali",
            instructionCount = 2,
            branchCount = 0,
        )
        assertRecovered(
            interaction = LegacyManagerInteractionEvidence.CAREER_CLUB_OFFER,
            methodSignature = "onStart()",
            smaliFileName = "ActivityMainTeam.smali",
            instructionCount = 97,
            branchCount = 15,
        )
    }

    private fun assertRecovered(
        interaction: LegacyManagerInteractionEvidence,
        methodSignature: String,
        smaliFileName: String,
        instructionCount: Int,
        branchCount: Int,
    ) {
        val recovered = requireNotNull(
            LegacyManagerInteractionEvidenceBoundary.recoveredHostMethodFor(interaction),
        )
        assertEquals(methodSignature, recovered.methodSignature)
        assertEquals(smaliFileName, recovered.smaliFileName)
        assertEquals(instructionCount, recovered.instructionCount)
        assertEquals(branchCount, recovered.branchCount)
    }
}

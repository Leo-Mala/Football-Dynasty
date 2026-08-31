package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerProgressionEvidenceBoundaryTest {
    @Test
    fun onlySerializedCoachIdentityFieldsArePromotedToProvenState() {
        assertEquals(linkedSetOf("coach", "coachCountry"), LegacyCareerProgressionEvidenceBoundary.provenSerializedCoachFields)
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coach"))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coachCountry"))
        assertFalse(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("reputation"))
    }

    @Test
    fun careerHostsStillUseOfficialPhase4rEvidence() {
        val selection = requireNotNull(LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(LegacyManagerCareerSurface.CLUB_SELECTION))
        assertEquals("i(String)", selection.methodSignature)
        assertEquals(38, selection.instructionCount)
        assertEquals(9, selection.branchCount)
        val hub = requireNotNull(LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(LegacyManagerCareerSurface.CAREER_CLUB_HUB))
        assertEquals("onStart()", hub.methodSignature)
        assertEquals(93, hub.instructionCount)
        assertEquals(15, hub.branchCount)
    }

    @Test
    fun replacementPoolFallbackAndSwapAreCharacterizedWithoutUnlockingWholeResolver() {
        assertEquals(
            setOf(
                LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
                LegacyCharacterizedCareerRuntimePath.REPLACEMENT_CANDIDATE_POOL_T,
                LegacyCharacterizedCareerRuntimePath.REPLACEMENT_UNEMPLOYED_FALLBACK_U,
                LegacyCharacterizedCareerRuntimePath.MANAGER_SWAP_B4,
            ),
            LegacyCareerProgressionEvidenceBoundary.characterizedCareerRuntimePaths,
        )
        assertEquals(
            setOf(
                "best.b" to "t(best.c0,int)",
                "best.b" to "u()",
                "best.b" to "b4(best.f0,best.f0)",
            ),
            LegacyCareerProgressionEvidenceBoundary.characterizedReplacementSubmethods
                .map { it.legacyClassName to it.methodSignature }
                .toSet(),
        )
        val byName = LegacyCareerProgressionEvidenceBoundary.characterizedReplacementSubmethods.associateBy { it.methodSignature }
        assertEquals(120 to 20, requireNotNull(byName["t(best.c0,int)"]).let { it.instructionCount to it.branchCount })
        assertEquals(30 to 4, requireNotNull(byName["u()"]).let { it.instructionCount to it.branchCount })
        assertEquals(9 to 0, requireNotNull(byName["b4(best.f0,best.f0)"]).let { it.instructionCount to it.branchCount })
        assertEquals("y()", LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.methodSignature)
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyCareerProgressionSurfaceEvidence.CLUB_INVITATION))
    }

    @Test
    fun directEmploymentTransitionRemainsCharacterized() {
        assertEquals(3, LegacyCareerProgressionEvidenceBoundary.characterizedEmploymentMethods.size)
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E))
    }

    @Test
    fun historicalClubSelectionMethodNameIsNotPromoted() {
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "E(String)"))
    }

    @Test
    fun remainingCareerSurfacesStayFailClosed() {
        assertEquals(LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed, LegacyCareerProgressionEvidenceBoundary.semanticRuntimeBlockedSurfaces)
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyCareerProgressionSurfaceEvidence.DISMISSALS))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE))
    }
}

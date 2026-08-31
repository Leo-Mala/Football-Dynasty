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
    fun replacementYearEndAndContinuationHostsAreCharacterized() {
        assertEquals(
            setOf(
                LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
                LegacyCharacterizedCareerRuntimePath.REPLACEMENT_CANDIDATE_POOL_T,
                LegacyCharacterizedCareerRuntimePath.REPLACEMENT_UNEMPLOYED_FALLBACK_U,
                LegacyCharacterizedCareerRuntimePath.MANAGER_SWAP_B4,
                LegacyCharacterizedCareerRuntimePath.DISMISSAL_GATE_L,
                LegacyCharacterizedCareerRuntimePath.END_OF_YEAR_DISPATCH_M,
                LegacyCharacterizedCareerRuntimePath.CAREER_CONTINUATION_I,
                LegacyCharacterizedCareerRuntimePath.PENDING_MATCH_LAUNCH_H,
                LegacyCharacterizedCareerRuntimePath.POST_SEASON_RESULTS_J,
                LegacyCharacterizedCareerRuntimePath.INVITATION_DISPATCH_K,
            ),
            LegacyCareerProgressionEvidenceBoundary.characterizedCareerRuntimePaths,
        )
        assertEquals(
            setOf("best.b" to "t(best.c0,int)", "best.b" to "u()", "best.b" to "b4(best.f0,best.f0)"),
            LegacyCareerProgressionEvidenceBoundary.characterizedReplacementSubmethods.map { it.legacyClassName to it.methodSignature }.toSet(),
        )
        assertEquals("y()", LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.methodSignature)
    }

    @Test
    fun officialYearEndMethodsHaveExactFingerprintsAndPaths() {
        assertEquals(setOf("best.n" to "l()", "best.n" to "m()"), LegacyCareerProgressionEvidenceBoundary.characterizedYearEndMethods.map { it.legacyClassName to it.methodSignature }.toSet())
        val byName = LegacyCareerProgressionEvidenceBoundary.characterizedYearEndMethods.associateBy { it.methodSignature }
        assertEquals(81 to 16, requireNotNull(byName["l()"]).let { it.instructionCount to it.branchCount })
        assertEquals(65 to 11, requireNotNull(byName["m()"]).let { it.instructionCount to it.branchCount })
    }

    @Test
    fun officialContinuationMethodsHaveExactFingerprintsAndPaths() {
        assertEquals(
            setOf("best.n" to "i()", "best.n" to "h()", "best.n" to "j()", "best.n" to "k()"),
            LegacyCareerProgressionEvidenceBoundary.characterizedContinuationMethods.map { it.legacyClassName to it.methodSignature }.toSet(),
        )
        val byName = LegacyCareerProgressionEvidenceBoundary.characterizedContinuationMethods.associateBy { it.methodSignature }
        assertEquals(78 to 16, requireNotNull(byName["i()"]).let { it.instructionCount to it.branchCount })
        assertEquals(37 to 3, requireNotNull(byName["h()"]).let { it.instructionCount to it.branchCount })
        assertEquals(27 to 3, requireNotNull(byName["j()"]).let { it.instructionCount to it.branchCount })
        assertEquals(117 to 20, requireNotNull(byName["k()"]).let { it.instructionCount to it.branchCount })
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(LegacyCharacterizedCareerRuntimePath.CAREER_CONTINUATION_I))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(LegacyCharacterizedCareerRuntimePath.PENDING_MATCH_LAUNCH_H))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(LegacyCharacterizedCareerRuntimePath.POST_SEASON_RESULTS_J))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(LegacyCharacterizedCareerRuntimePath.INVITATION_DISPATCH_K))
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

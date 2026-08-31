package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerProgressionEvidenceBoundaryTest {
    @Test
    fun onlySerializedCoachIdentityFieldsArePromotedToProvenState() {
        assertEquals(
            linkedSetOf("coach", "coachCountry"),
            LegacyCareerProgressionEvidenceBoundary.provenSerializedCoachFields,
        )
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coach"))
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coachCountry"))
        assertFalse(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("reputation"))
        assertFalse(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("objectives"))
    }

    @Test
    fun clubSelectionAndCareerClubHubUseOfficialPhase4rMethodEvidence() {
        val clubSelection = requireNotNull(
            LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(
                LegacyManagerCareerSurface.CLUB_SELECTION,
            ),
        )
        assertEquals("ActivityEscolhaTimes", clubSelection.legacyClassName)
        assertEquals("i(String)", clubSelection.methodSignature)
        assertEquals("i(Ljava/lang/String;)Z", clubSelection.smaliMethodSignature)
        assertEquals(38, clubSelection.instructionCount)
        assertEquals(9, clubSelection.branchCount)

        val careerClubHub = requireNotNull(
            LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(
                LegacyManagerCareerSurface.CAREER_CLUB_HUB,
            ),
        )
        assertEquals("ActivityMainTeam", careerClubHub.legacyClassName)
        assertEquals("onStart()", careerClubHub.methodSignature)
        assertEquals("onStart()V", careerClubHub.smaliMethodSignature)
        assertEquals(93, careerClubHub.instructionCount)
        assertEquals(15, careerClubHub.branchCount)
    }

    @Test
    fun directManagerEmploymentTransitionIsCharacterizedWithoutUnlockingReplacementSelection() {
        assertEquals(
            setOf(
                LegacyCharacterizedCareerRuntimePath.MANAGER_NAME_VALIDATION,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_ACCEPTANCE_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_INVITATION_CANCEL_DISPATCH,
                LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
            ),
            LegacyCareerProgressionEvidenceBoundary.characterizedCareerRuntimePaths,
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isCharacterizedCareerRuntimePath(
                LegacyCharacterizedCareerRuntimePath.CLUB_MANAGER_TRANSFER_G_L_E,
            ),
        )
        assertEquals(
            setOf(
                "best.b" to "G(best.c0,best.f0,best.f0)",
                "best.f0" to "l(best.f0)",
                "best.f0" to "e(best.c0)",
            ),
            LegacyCareerProgressionEvidenceBoundary.characterizedEmploymentMethods
                .map { it.legacyClassName to it.methodSignature }
                .toSet(),
        )
        assertEquals("best.c0", LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.legacyClassName)
        assertEquals("y()", LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.methodSignature)
        assertEquals(103, LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.instructionCount)
        assertEquals(22, LegacyCareerProgressionEvidenceBoundary.recoveredReplacementManagerResolver.branchCount)
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(
                LegacyCareerProgressionSurfaceEvidence.CLUB_INVITATION,
            ),
        )
    }

    @Test
    fun historicalClubSelectionMethodNameIsNotPromoted() {
        assertNull(LegacyManagerRecoveredMethodEvidence.findExact("ActivityEscolhaTimes", "E(String)"))
        assertEquals(
            "i(String)",
            requireNotNull(
                LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(
                    LegacyManagerCareerSurface.CLUB_SELECTION,
                ),
            ).methodSignature,
        )
    }

    @Test
    fun recoveredCareerBodiesDoNotUnlockRemainingCareerSurfaces() {
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.hasRecoveredCareerHostBody(
                LegacyManagerCareerSurface.CLUB_SELECTION,
            ),
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.hasRecoveredCareerHostBody(
                LegacyManagerCareerSurface.CAREER_CLUB_HUB,
            ),
        )
        assertEquals(
            LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed,
            LegacyCareerProgressionEvidenceBoundary.semanticRuntimeBlockedSurfaces,
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(
                LegacyCareerProgressionSurfaceEvidence.DISMISSALS,
            ),
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(
                LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE,
            ),
        )
    }
}

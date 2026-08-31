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

        val recoveredSurfaces = setOf(
            LegacyManagerCareerSurface.CLUB_SELECTION,
            LegacyManagerCareerSurface.CAREER_CLUB_HUB,
        )
        val expectedAwaitingRecovery = LegacyManagerCareerSurfaces.confirmed - recoveredSurfaces
        assertEquals(
            expectedAwaitingRecovery,
            LegacyCareerProgressionEvidenceBoundary.reachableCareerSurfacesWithoutRecoveredHostBody,
        )
        expectedAwaitingRecovery.forEach { surface ->
            assertTrue(
                LegacyCareerProgressionEvidenceBoundary
                    .isReachableCareerSurfaceAwaitingRecoveredHostBody(surface),
            )
            assertNull(LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(surface))
        }
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
    fun recoveredCareerBodiesDoNotUnlockCareerSemantics() {
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
        LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed.forEach { surface ->
            assertTrue(LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(surface))
        }
    }

    @Test
    fun provenCoachIdentityDoesNotUnlockCoachProfileCareerMutations() {
        assertTrue(LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coach"))
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(
                LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE,
            ),
        )
    }
}

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
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coach"),
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coachCountry"),
        )
        assertFalse(
            LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("reputation"),
        )
        assertFalse(
            LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("objectives"),
        )
    }

    @Test
    fun onlyCareerClubHubHasRecoveredMethodLevelHostEvidence() {
        val recovered = requireNotNull(
            LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(
                LegacyManagerCareerSurface.CAREER_CLUB_HUB,
            ),
        )

        assertEquals("ActivityMainTeam", recovered.legacyClassName)
        assertEquals("onStart()", recovered.methodSignature)
        assertEquals("ActivityMainTeam.smali", recovered.smaliFileName)
        assertEquals(97, recovered.instructionCount)
        assertEquals(15, recovered.branchCount)
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.hasRecoveredCareerHostBody(
                LegacyManagerCareerSurface.CAREER_CLUB_HUB,
            ),
        )

        val expectedAwaitingRecovery =
            LegacyManagerCareerSurfaces.confirmed - LegacyManagerCareerSurface.CAREER_CLUB_HUB
        assertEquals(
            expectedAwaitingRecovery,
            LegacyCareerProgressionEvidenceBoundary.reachableCareerSurfacesWithoutRecoveredHostBody,
        )
        expectedAwaitingRecovery.forEach { surface ->
            assertTrue(
                LegacyCareerProgressionEvidenceBoundary
                    .isReachableCareerSurfaceAwaitingRecoveredHostBody(surface),
            )
            assertNull(
                LegacyCareerProgressionEvidenceBoundary.recoveredCareerHostMethodFor(surface),
            )
        }
    }

    @Test
    fun recoveredCareerClubHubBodyDoesNotUnlockCareerSemantics() {
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
            assertTrue(
                LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(surface),
            )
        }
    }

    @Test
    fun provenCoachIdentityDoesNotUnlockCoachProfileCareerMutations() {
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isProvenSerializedCoachField("coach"),
        )
        assertTrue(
            LegacyCareerProgressionEvidenceBoundary.isSemanticRuntimeBlocked(
                LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE,
            ),
        )
    }
}

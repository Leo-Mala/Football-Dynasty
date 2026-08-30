package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun reachableCareerSurfacesRemainBlockedFromInventedRuntimeSemantics() {
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

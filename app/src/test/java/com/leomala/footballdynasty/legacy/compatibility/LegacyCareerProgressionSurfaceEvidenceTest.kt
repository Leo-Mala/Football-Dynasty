package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerProgressionSurfaceEvidenceTest {
    @Test
    fun confirmedCatalogMatchesOnlyVersionedCareerAndCoachSurfaces() {
        assertEquals(
            linkedSetOf(
                LegacyCareerProgressionSurfaceEvidence.COACH_PROFILE,
                LegacyCareerProgressionSurfaceEvidence.CLUB_INVITATION,
                LegacyCareerProgressionSurfaceEvidence.NATIONAL_TEAM_INVITATION,
                LegacyCareerProgressionSurfaceEvidence.DISMISSALS,
                LegacyCareerProgressionSurfaceEvidence.COACH_RANKING,
                LegacyCareerProgressionSurfaceEvidence.COACH_HALL,
            ),
            LegacyCareerProgressionSurfaceEvidenceCatalog.confirmed,
        )
    }

    @Test
    fun resolvesOnlyExactActivityAndLayoutPairs() {
        assertEquals(
            LegacyCareerProgressionSurfaceEvidence.CLUB_INVITATION,
            LegacyCareerProgressionSurfaceEvidenceCatalog.fromExactSource(
                "ActivityConvite",
                "activity_convite",
            ),
        )
        assertEquals(
            LegacyCareerProgressionSurfaceEvidence.DISMISSALS,
            LegacyCareerProgressionSurfaceEvidenceCatalog.fromExactSource(
                "DialogDemissoes",
                "dialog_demissoes",
            ),
        )
        assertEquals(
            LegacyCareerProgressionSurfaceEvidence.COACH_HALL,
            LegacyCareerProgressionSurfaceEvidenceCatalog.fromExactSource(
                "ActivityHallTecnicos",
                "activity_bola_ouro",
            ),
        )
    }

    @Test
    fun identicalOrPlausibleNamesDoNotCreateUnprovenCareerBehavior() {
        assertTrue(
            LegacyCareerProgressionSurfaceEvidenceCatalog.isConfirmedExactSource(
                "ActivityTecnico",
                "activity_tecnico",
            ),
        )
        assertFalse(
            LegacyCareerProgressionSurfaceEvidenceCatalog.isConfirmedExactSource(
                "ActivityTecnico",
                "activity_ranking_tec",
            ),
        )
        assertNull(
            LegacyCareerProgressionSurfaceEvidenceCatalog.fromExactSource(
                "ActivityConvite",
                "dialog_oferta",
            ),
        )
        assertNull(
            LegacyCareerProgressionSurfaceEvidenceCatalog.fromExactSource(
                "ActivityDemissao",
                "dialog_demissoes",
            ),
        )
    }
}

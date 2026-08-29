package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerCareerSurfacesTest {
    @Test
    fun confirmedCatalogMatchesOnlyProvenManagerFacingLegacyActivities() {
        assertEquals(
            linkedSetOf(
                LegacyManagerCareerSurface.COACH,
                LegacyManagerCareerSurface.CLUB_INVITATION,
                LegacyManagerCareerSurface.DISMISSALS,
                LegacyManagerCareerSurface.COACH_RANKING,
                LegacyManagerCareerSurface.COACH_HALL,
            ),
            LegacyManagerCareerSurfaces.confirmed,
        )
    }

    @Test
    fun resolvesExactLegacyClassNamesWithoutAliasesOrFallbacks() {
        assertEquals(
            LegacyManagerCareerSurface.COACH,
            LegacyManagerCareerSurfaces.fromLegacyClassName("ActivityTecnico"),
        )
        assertEquals(
            LegacyManagerCareerSurface.CLUB_INVITATION,
            LegacyManagerCareerSurfaces.fromLegacyClassName("ActivityConvite"),
        )
        assertEquals(
            LegacyManagerCareerSurface.DISMISSALS,
            LegacyManagerCareerSurfaces.fromLegacyClassName("DialogDemissoes"),
        )
        assertEquals(
            LegacyManagerCareerSurface.COACH_RANKING,
            LegacyManagerCareerSurfaces.fromLegacyClassName("ActivtyRankingTecnicos"),
        )
        assertEquals(
            LegacyManagerCareerSurface.COACH_HALL,
            LegacyManagerCareerSurfaces.fromLegacyClassName("ActivityHallTecnicos"),
        )
    }

    @Test
    fun rejectsPlausibleButUnprovenNames() {
        assertFalse(LegacyManagerCareerSurfaces.isConfirmedLegacyClassName("ActivityManager"))
        assertFalse(LegacyManagerCareerSurfaces.isConfirmedLegacyClassName("ActivityEmpregos"))
        assertNull(LegacyManagerCareerSurfaces.fromLegacyClassName("ActivityRankingTecnicos"))
    }

    @Test
    fun keepsTheDecompilerObservedRankingClassSpellingExact() {
        assertTrue(LegacyManagerCareerSurfaces.isConfirmedLegacyClassName("ActivtyRankingTecnicos"))
        assertFalse(LegacyManagerCareerSurfaces.isConfirmedLegacyClassName("ActivityRankingTecnicos"))
    }
}

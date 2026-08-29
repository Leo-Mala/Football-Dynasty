package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyManagerSystemSurfacesTest {
    @Test
    fun confirmedCatalogMatchesOnlyVersionedManagerLoopSurfaces() {
        assertEquals(
            linkedSetOf(
                LegacyManagerSystemSurface.LINEUP,
                LegacyManagerSystemSurface.TACTICS,
                LegacyManagerSystemSurface.SAVED_TACTICS,
                LegacyManagerSystemSurface.PLAYER_SEARCH,
                LegacyManagerSystemSurface.PLAYER_INFO,
                LegacyManagerSystemSurface.FINANCES,
                LegacyManagerSystemSurface.STADIUM,
            ),
            LegacyManagerSystemSurfaces.confirmed,
        )
    }

    @Test
    fun resolvesExactLegacyClassNamesWithoutAliasesOrFallbacks() {
        assertEquals(
            LegacyManagerSystemSurface.LINEUP,
            LegacyManagerSystemSurfaces.fromLegacyClassName("ActivityEscala"),
        )
        assertEquals(
            LegacyManagerSystemSurface.TACTICS,
            LegacyManagerSystemSurfaces.fromLegacyClassName("DialogTatics"),
        )
        assertEquals(
            LegacyManagerSystemSurface.SAVED_TACTICS,
            LegacyManagerSystemSurfaces.fromLegacyClassName("ActivitySavedTatics"),
        )
        assertEquals(
            LegacyManagerSystemSurface.PLAYER_SEARCH,
            LegacyManagerSystemSurfaces.fromLegacyClassName("ActivityProcura"),
        )
        assertEquals(
            LegacyManagerSystemSurface.PLAYER_INFO,
            LegacyManagerSystemSurfaces.fromLegacyClassName("DialogIgrokInfo"),
        )
        assertEquals(
            LegacyManagerSystemSurface.FINANCES,
            LegacyManagerSystemSurfaces.fromLegacyClassName("ActivityFinancas"),
        )
        assertEquals(
            LegacyManagerSystemSurface.STADIUM,
            LegacyManagerSystemSurfaces.fromLegacyClassName("ActivityEstadio"),
        )
    }

    @Test
    fun rejectsPlausibleButUnprovenAliasesAndWildcardHelpers() {
        assertFalse(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("ActivityTactics"))
        assertFalse(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("ActivityFinance"))
        assertFalse(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("RcTransfer"))
        assertNull(LegacyManagerSystemSurfaces.fromLegacyClassName("Rc*"))
    }

    @Test
    fun keepsDecompilerObservedTaticsSpellingExact() {
        assertTrue(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("DialogTatics"))
        assertTrue(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("ActivitySavedTatics"))
        assertFalse(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("DialogTactics"))
        assertFalse(LegacyManagerSystemSurfaces.isConfirmedLegacyClassName("ActivitySavedTactics"))
    }
}

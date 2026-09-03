package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySchemaCatalogTest {
    @Test
    fun coreLegacyIdentitiesRemainStable() {
        assertEquals(
            listOf("a.p", "a.ac", "a.t", "d.q", "a.b"),
            LegacySchemaCatalog.core.map { it.type },
        )
        assertTrue("posicao" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("nRebaixados" in LegacySchemaCatalog.competition.confirmedFields)
    }

    @Test
    fun playerCommercialCatalogUsesTheSingleProvenFieldBoundary() {
        assertEquals(
            linkedSetOf(
                "anoIn",
                "aposentado",
                "energiaBase",
                "forca",
                "pais",
                "posicao",
                "status",
            ) + LegacyCareerPlayerCommercialFields.confirmedNames,
            LegacySchemaCatalog.player.confirmedFields,
        )
        assertTrue(LegacyCareerPlayerCommercialFields.SALARY in LegacySchemaCatalog.player.confirmedFields)
        assertTrue(LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN in LegacySchemaCatalog.player.confirmedFields)
    }

    @Test
    fun clubCommercialCatalogUsesTheSingleProvenFieldBoundary() {
        assertEquals(
            linkedSetOf("xp") + LegacyCareerClubCommercialFields.confirmedNames,
            LegacySchemaCatalog.club.confirmedFields,
        )
        assertTrue(LegacyCareerClubCommercialFields.INVESTMENT in LegacySchemaCatalog.club.confirmedFields)
        assertTrue(LegacyCareerClubCommercialFields.SPONSOR in LegacySchemaCatalog.club.confirmedFields)
    }
}

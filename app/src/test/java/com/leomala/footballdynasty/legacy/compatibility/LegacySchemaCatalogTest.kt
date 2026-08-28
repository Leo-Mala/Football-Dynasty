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
    fun provenPlayerCommercialFieldsRemainCatalogued() {
        assertTrue("salario" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("rcClause" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("rcRenewYear" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("rcConvYear" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("pendSaleClub" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("pendSaleValue" in LegacySchemaCatalog.player.confirmedFields)
        assertTrue("pendIsLoan" in LegacySchemaCatalog.player.confirmedFields)
    }
}

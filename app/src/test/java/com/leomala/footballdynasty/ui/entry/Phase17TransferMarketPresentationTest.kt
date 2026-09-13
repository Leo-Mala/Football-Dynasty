package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerTransferSearchCatalogStore
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase17TransferMarketPresentationTest {
    @Test
    fun `presentation keeps persisted market values literal without inventing currency`() {
        val presentation = CareerTransferSearchCatalogStore.PlayerRow(
            playerId = "player-9",
            name = "Player Nine",
            clubId = "club-b",
            clubName = "Club B",
            position = 3,
            age = 24,
            overall = 82,
            marketValue = 12_345,
            sourceOrdinal = 7,
        ).toPhase17TransferMarketPresentation()

        assertEquals("player-9", presentation.playerId)
        assertEquals("Player Nine", presentation.name)
        assertEquals("Clube: Club B • Posição 3 • Idade 24 • Força 82", presentation.detailLabel)
        assertEquals("Valor de mercado persistido: 12345", presentation.marketValueLabel)
    }
}

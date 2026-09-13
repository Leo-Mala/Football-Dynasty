package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerContractCatalogStore
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase17ContractPresentationTest {
    @Test
    fun `presentation keeps persisted contract values literal`() {
        val presentation = row(salaryCode = 321).toPhase17ContractPresentation()

        assertEquals("player-7", presentation.playerId)
        assertEquals("Player Seven", presentation.name)
        assertEquals("Posição 3 • Ordem persistida 6", presentation.detailLabel)
        assertEquals("Término contratual persistido (ms): 123456", presentation.contractEndLabel)
        assertEquals("Código salarial persistido: 321", presentation.salaryLabel)
    }

    @Test
    fun `missing commercial salary stays explicitly unavailable`() {
        val presentation = row(salaryCode = null).toPhase17ContractPresentation()

        assertEquals("Código salarial persistido indisponível.", presentation.salaryLabel)
    }

    private fun row(salaryCode: Int?) = CareerContractCatalogStore.ContractRow(
        playerId = "player-7",
        name = "Player Seven",
        position = 3,
        sourceOrdinal = 6,
        contractEndEpochMillis = 123_456L,
        salaryCode = salaryCode,
    )
}

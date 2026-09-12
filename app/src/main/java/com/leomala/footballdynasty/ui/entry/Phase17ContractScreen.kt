package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerContractCatalogStore

/** Read-only owner of persisted senior-contract rows; a missing provider fails closed. */
internal val LocalCareerContractCatalogStore =
    staticCompositionLocalOf<CareerContractCatalogStore?> { null }

internal data class Phase17ContractRowPresentation(
    val playerId: String,
    val name: String,
    val detailLabel: String,
    val contractEndLabel: String,
    val salaryLabel: String,
)

/**
 * Keeps the presentation deliberately literal: salary is a persisted legacy code and no currency,
 * payment cadence or commercial meaning is inferred from an opaque field.
 */
internal fun CareerContractCatalogStore.ContractRow.toPhase17ContractPresentation() =
    Phase17ContractRowPresentation(
        playerId = playerId,
        name = name,
        detailLabel = "Posição $position • Ordem persistida $sourceOrdinal",
        contractEndLabel = "Término contratual persistido (ms): $contractEndEpochMillis",
        salaryLabel = salaryCode?.let { "Código salarial persistido: $it" }
            ?: "Código salarial persistido indisponível.",
    )

/**
 * Phase 17 read-only contract surface backed only by the persisted managed senior squad.
 * Contract renewal remains intentionally absent until every accepted-renewal side effect has a
 * proved atomic persistence owner.
 */
@Composable
internal fun Phase17ContractScreen(
    contracts: CareerContractCatalogStore.ContractList,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar ao treinador")
        }
        Text(
            text = "Contratos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${contracts.clubId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        if (contracts.rows.isEmpty()) {
            Text(
                text = "Nenhum contrato persistido encontrado para o elenco principal.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contracts.rows, key = { it.playerId }) { row ->
                val presentation = row.toPhase17ContractPresentation()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = presentation.name, style = MaterialTheme.typography.titleMedium)
                    Text(text = presentation.detailLabel, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = presentation.contractEndLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        text = presentation.salaryLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

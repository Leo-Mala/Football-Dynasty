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
import com.leomala.footballdynasty.application.career.CareerTransferSearchCatalogStore

/** Read-only owner of the persisted transfer-search catalog; a missing provider fails closed. */
internal val LocalCareerTransferSearchCatalogStore =
    staticCompositionLocalOf<CareerTransferSearchCatalogStore?> { null }

internal data class Phase17TransferMarketRowPresentation(
    val playerId: String,
    val name: String,
    val detailLabel: String,
    val marketValueLabel: String,
)

internal fun CareerTransferSearchCatalogStore.PlayerRow.toPhase17TransferMarketPresentation() =
    Phase17TransferMarketRowPresentation(
        playerId = playerId,
        name = name,
        detailLabel = "Clube: $clubName • Posição $position • Idade $age • Força $overall",
        marketValueLabel = "Valor de mercado persistido: $marketValue",
    )

/**
 * Phase 17 read-only player-search surface.
 *
 * The UI deliberately does not label these rows as transfer-listed and exposes no purchase/loan
 * command until the complete commercial mutation can be composed and persisted atomically.
 */
@Composable
internal fun Phase17TransferMarketScreen(
    catalog: CareerTransferSearchCatalogStore.SearchCatalog,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar ao treinador")
        }
        Text(
            text = "Mercado — busca de jogadores",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Jogadores seniores persistidos de outros clubes. A presença aqui não significa que o jogador esteja listado para transferência.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        if (catalog.rows.isEmpty()) {
            Text(
                text = "Nenhum jogador sênior de outro clube está disponível no estado persistido desta carreira.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(catalog.rows, key = { it.playerId }) { row ->
                val presentation = row.toPhase17TransferMarketPresentation()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = presentation.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = presentation.detailLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = presentation.marketValueLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

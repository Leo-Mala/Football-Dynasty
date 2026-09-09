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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore

/**
 * Read-only Phase 17 presentation of persisted lineup inputs.
 *
 * It deliberately does not claim that a formation, starting XI or tactic has been selected.
 */
@Composable
fun Phase17LineupInputsScreen(
    inputs: CareerLineupInputCatalogStore.LineupInputs,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar à central")
        }
        Text(
            text = "Escalação — dados persistidos",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Clube: ${inputs.clubId}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            text = "Jogadores disponíveis ao runtime de escalação. Nenhuma formação ou tática é alterada nesta tela.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )

        if (inputs.players.isEmpty()) {
            Text(
                text = "Nenhum jogador persistido disponível para escalação.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(inputs.players, key = { it.playerId }) { player ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Posição ${player.positionCode} • Força ${player.skill} • Energia ${player.energy}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (player.star) {
                        Text(
                            text = "Destaque persistido",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

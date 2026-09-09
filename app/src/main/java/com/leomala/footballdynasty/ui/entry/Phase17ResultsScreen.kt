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
import com.leomala.footballdynasty.application.career.CareerResultCatalogStore

/** Read-only owner of persisted results; a missing provider keeps the surface disabled. */
internal val LocalCareerResultCatalogStore =
    staticCompositionLocalOf<CareerResultCatalogStore?> { null }

internal data class Phase17ResultRowPresentation(
    val matchId: String,
    val dayLabel: String,
    val matchupLabel: String,
    val scoreLabel: String,
    val competitionLabels: List<String>,
)

internal fun CareerResultCatalogStore.ResultRow.toPhase17ResultPresentation() =
    Phase17ResultRowPresentation(
        matchId = matchId,
        dayLabel = "Dia ${dayIndex + 1}",
        matchupLabel = "$homeClubName x $awayClubName",
        scoreLabel = "$homeGoals x $awayGoals",
        competitionLabels = competitionLinks.map { link ->
            "${link.competitionId} • Rodada ${link.roundNumber}"
        },
    )

/** Phase 17 read-only surface for scores already committed in the persisted career. */
@Composable
internal fun Phase17ResultsScreen(
    catalog: CareerResultCatalogStore.ResultCatalog,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Button(onClick = onBack) {
            Text("Voltar ao treinador")
        }
        Text(
            text = "Resultados",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Somente partidas já processadas e com placar persistido nesta carreira.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        if (catalog.rows.isEmpty()) {
            Text(
                text = "Nenhum resultado persistido encontrado.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(catalog.rows, key = { it.matchId }) { row ->
                val presentation = row.toPhase17ResultPresentation()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = presentation.dayLabel, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = presentation.matchupLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                    Text(
                        text = presentation.scoreLabel,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    presentation.competitionLabels.forEach { competitionLabel ->
                        Text(
                            text = competitionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

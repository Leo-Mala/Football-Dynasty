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
 * Phase 17 presentation of the persisted lineup inputs and the fail-closed
 * preparation state for the next managed match.
 *
 * The screen still does not mutate formation/tactics or execute a match. It
 * makes the remaining evidence owners visible instead of silently substituting
 * modern defaults or values copied from characterization tests.
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

        MatchPreparationSummary(
            preparation = inputs.matchPreparation,
            modifier = Modifier.padding(top = 14.dp),
        )

        Text(
            text = "Nenhuma formação, titularidade ou tática é alterada nesta tela enquanto os owners legados abaixo não estiverem resolvidos.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
        )
        if (inputs.players.isEmpty()) {
            Text(
                text = "Nenhum jogador sênior persistido encontrado para o clube gerenciado.",
                style = MaterialTheme.typography.bodyLarge,
            )
            return
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(inputs.players, key = { it.playerId }) { player ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = player.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Posição ${player.positionCode} • Lado ${player.sideCode} • Subfunção ${player.subroleCode}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Força ${player.skill} • Energia ${player.energy} • Estrela ${if (player.star) "sim" else "não"}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    player.excludedByCompetitionV0ForPreparedMatch?.let { excluded ->
                        Text(
                            text = if (excluded) {
                                "Disciplina da competição: indisponível por V0"
                            } else {
                                "Disciplina da competição: liberado por V0"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchPreparationSummary(
    preparation: CareerLineupInputCatalogStore.MatchPreparation,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Preparação da próxima partida",
            style = MaterialTheme.typography.titleLarge,
        )
        val matchId = preparation.matchId
        if (matchId == null) {
            val message = when {
                CareerLineupInputCatalogStore.MatchPreparationBlocker.NO_PLAYABLE_MATCH in preparation.blockers ->
                    "Nenhuma próxima partida jogável persistida."
                CareerLineupInputCatalogStore.MatchPreparationBlocker.MANAGED_CLUB_NOT_ON_NEXT_PLAYABLE_DAY in preparation.blockers ->
                    "O próximo dia jogável não contém partida do clube gerenciado."
                else -> "Preparação da partida indisponível."
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            Text(
                text = "Partida: $matchId • Dia ${(preparation.nextPlayableDayIndex ?: 0) + 1}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = "${preparation.homeClubId} x ${preparation.awayClubId} • Clube gerenciado: ${if (preparation.managedSide == CareerLineupInputCatalogStore.ManagedMatchSide.HOME) "casa" else "fora"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = "Elencos seniores persistidos: casa ${preparation.homeSeniorRosterCount ?: 0} • fora ${preparation.awaySeniorRosterCount ?: 0}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            when (preparation.competitionRestrictionActive) {
                true -> Text(
                    text = "Restrição disciplinar ${preparation.competitionId}: ${if (preparation.competitionDisciplineOwnerResolved == true) "estado V19 resolvido" else "estado V19 incompleto"}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                false -> Text(
                    text = "Restrição disciplinar V0: não ativa para esta partida.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
                null -> Unit
            }
        }

        if (preparation.blockers.isNotEmpty()) {
            Text(
                text = "Jogar permanece bloqueado até resolver:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
            preparation.blockers.forEach { blocker ->
                Text(
                    text = "• ${blocker.presentationLabel()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        } else {
            Text(
                text = "Preparação validada para o command boundary de partida.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

private fun CareerLineupInputCatalogStore.MatchPreparationBlocker.presentationLabel(): String = when (this) {
    CareerLineupInputCatalogStore.MatchPreparationBlocker.NO_PLAYABLE_MATCH ->
        "próxima partida jogável persistida"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.MANAGED_CLUB_NOT_ON_NEXT_PLAYABLE_DAY ->
        "partida do clube gerenciado no próximo dia jogável"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.HOME_SENIOR_ROSTER_EMPTY ->
        "elenco sênior persistido do mandante"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.AWAY_SENIOR_ROSTER_EMPTY ->
        "elenco sênior persistido do visitante"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_ELIGIBILITY_OWNER_UNRESOLVED ->
        "owner legado dos predicados de elegibilidade da escalação"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.TACTICS_STATE_OWNER_UNRESOLVED ->
        "owner persistido do estado de tática"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.SUBSTITUTION_BUDGET_OWNER_UNRESOLVED ->
        "owner legado da quantidade de substituições"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.LEGACY_MODE_FLAG_OWNER_UNRESOLVED ->
        "owner do legacy mode flag da partida"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED ->
        "composição produtiva do runtime de partida até o executor atômico"
}

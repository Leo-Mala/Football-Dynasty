package com.leomala.footballdynasty.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore
import com.leomala.footballdynasty.application.career.CareerManagedLineupFormationPlanner
import com.leomala.footballdynasty.application.career.CareerManagedLineupSelection
import com.leomala.footballdynasty.application.career.CareerManagedLineupSelectionPlanner

/**
 * Phase 17 presentation of the persisted lineup inputs and the fail-closed
 * preparation state for the next managed match.
 *
 * Formation is an explicit manager-owned UI input. Confirmation produces a transient selection
 * bound to the current career/match/club/side and never persists a fabricated legacy saved-lineup
 * owner. The selection is invalidated automatically when the prepared match changes.
 */
@Composable
fun Phase17LineupInputsScreen(
    inputs: CareerLineupInputCatalogStore.LineupInputs,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFormationIndex by remember(inputs.careerId, inputs.matchPreparation.matchId) {
        mutableStateOf<Int?>(null)
    }
    var confirmedSelection by remember(inputs.careerId, inputs.matchPreparation.matchId) {
        mutableStateOf<CareerManagedLineupSelection?>(null)
    }
    val formationResolution = remember(inputs, selectedFormationIndex) {
        CareerManagedLineupFormationPlanner.prepare(
            inputs = inputs,
            formationIndex = selectedFormationIndex,
        )
    }

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

        if (inputs.matchPreparation.matchId != null && inputs.players.isNotEmpty()) {
            Text(
                text = "Formação para a próxima partida",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = "Escolha explícita do treinador; nenhum índice é assumido automaticamente.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    items = CareerManagedLineupFormationPlanner.availableFormationIndices,
                    key = { it },
                ) { formationIndex ->
                    Button(
                        onClick = {
                            selectedFormationIndex = formationIndex
                            confirmedSelection = null
                        }
                    ) {
                        Text("${formationIndex + 1}")
                    }
                }
            }
            FormationSelectionSummary(
                resolution = formationResolution,
                modifier = Modifier.padding(top = 8.dp),
            )

            if (formationResolution.prepared != null) {
                Button(
                    onClick = {
                        val formationIndex = requireNotNull(selectedFormationIndex)
                        confirmedSelection = CareerManagedLineupSelectionPlanner.confirm(
                            inputs = inputs,
                            formationIndex = formationIndex,
                        )
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text("Confirmar escalação")
                }
            }

            confirmedSelection
                ?.takeIf { CareerManagedLineupSelectionPlanner.isCurrent(it, inputs) }
                ?.let { selection ->
                    Text(
                        text = "Escalação confirmada para ${selection.matchId}: formação ${selection.formationIndex + 1}, ${selection.lineup.clubStarters.size} titulares.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
        }

        Text(
            text = "A escolha acima prepara titulares e banco pela regra legada certificada, mas não altera tática nem persiste um falso save de escalação.",
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
private fun FormationSelectionSummary(
    resolution: CareerManagedLineupFormationPlanner.Resolution,
    modifier: Modifier = Modifier,
) {
    val prepared = resolution.prepared
    val message = when {
        prepared != null -> {
            val starterCount = prepared.state.starters.count { it.player != null }
            "Formação ${prepared.formationIndex + 1} preparada: $starterCount/11 titulares e ${prepared.state.bench.size} opções no banco bruto legado."
        }
        resolution.blocker == CareerManagedLineupFormationPlanner.Blocker.FORMATION_NOT_SELECTED ->
            "Selecione uma das 11 formações recuperadas para continuar a preparação."
        resolution.blocker == CareerManagedLineupFormationPlanner.Blocker.INVALID_FORMATION ->
            "Formação inválida; nenhum fallback foi aplicado."
        resolution.blocker == CareerManagedLineupFormationPlanner.Blocker.LINEUP_ELIGIBILITY_UNRESOLVED ->
            "A formação continua bloqueada porque há elegibilidade K0 sem owner resolvido."
        else ->
            "A formação depende de uma próxima partida do clube gerenciado."
    }
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier,
    )
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
            val lineupModeLabel = when (preparation.lineupModeFlag) {
                true -> "ativo"
                false -> "inativo"
                null -> "não materializado"
            }
            Text(
                text = "ActivityMainTeam.D: $lineupModeLabel",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
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
    CareerLineupInputCatalogStore.MatchPreparationBlocker.LINEUP_MODE_FLAG_OWNER_UNRESOLVED ->
        "owner persistido de ActivityMainTeam.D"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.TACTICS_STATE_OWNER_UNRESOLVED ->
        "owner persistido do estado de tática"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.SUBSTITUTION_BUDGET_OWNER_UNRESOLVED ->
        "owner legado da quantidade de substituições"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.LEGACY_MODE_FLAG_OWNER_UNRESOLVED ->
        "owner do best.c0.Q0() usado no runtime da partida"
    CareerLineupInputCatalogStore.MatchPreparationBlocker.MATCH_RUNTIME_COMPOSITION_UNWIRED ->
        "composição produtiva do runtime de partida até o executor atômico"
}

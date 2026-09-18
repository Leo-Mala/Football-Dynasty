package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerCalendarCatalogStore

/**
 * Presentation-only projection of one persisted calendar row.
 *
 * It does not infer gameplay state: pending/result and score visibility come
 * exclusively from the certified persisted calendar projection.
 */
internal data class Phase17MatchPresentation(
    val title: String,
    val dayLabel: String,
    val matchupLabel: String,
    val scoreLabel: String?,
    val stateLabel: String,
    val competitionLabels: List<String>,
    val openActionLabel: String,
)

internal fun CareerCalendarCatalogStore.MatchRow.toPhase17MatchPresentation(): Phase17MatchPresentation {
    val persistedScore = if (processed && homeGoals != null && awayGoals != null) {
        "$homeGoals x $awayGoals"
    } else {
        null
    }

    return Phase17MatchPresentation(
        title = if (processed) "Resultado" else "Partida",
        dayLabel = "Dia ${dayIndex + 1}",
        matchupLabel = "$homeClubName x $awayClubName",
        scoreLabel = persistedScore,
        stateLabel = when {
            !processed -> "Partida ainda não processada."
            persistedScore != null -> "Partida processada."
            else -> "Resultado persistido sem placar completo."
        },
        competitionLabels = competitionLinks.map { link ->
            "${link.competitionId} • Rodada ${link.roundNumber}"
        },
        openActionLabel = if (processed) "Ver resultado" else "Abrir partida",
    )
}

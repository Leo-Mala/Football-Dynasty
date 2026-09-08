package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerCalendarCatalogStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Phase17MatchPresentationTest {
    @Test
    fun `processed match exposes only persisted result data`() {
        val presentation = matchRow(
            processed = true,
            homeGoals = 2,
            awayGoals = 1,
        ).toPhase17MatchPresentation()

        assertEquals("Resultado", presentation.title)
        assertEquals("Dia 4", presentation.dayLabel)
        assertEquals("Clube A x Clube B", presentation.matchupLabel)
        assertEquals("2 x 1", presentation.scoreLabel)
        assertEquals("Partida processada.", presentation.stateLabel)
        assertEquals(listOf("competition-a • Rodada 5"), presentation.competitionLabels)
        assertEquals("Ver resultado", presentation.openActionLabel)
    }

    @Test
    fun `pending match does not synthesize a score`() {
        val presentation = matchRow(
            processed = false,
            homeGoals = null,
            awayGoals = null,
        ).toPhase17MatchPresentation()

        assertEquals("Partida", presentation.title)
        assertNull(presentation.scoreLabel)
        assertEquals("Partida ainda não processada.", presentation.stateLabel)
        assertEquals("Abrir partida", presentation.openActionLabel)
    }

    @Test
    fun `processed row with incomplete persisted score fails closed in presentation`() {
        val presentation = matchRow(
            processed = true,
            homeGoals = 3,
            awayGoals = null,
        ).toPhase17MatchPresentation()

        assertEquals("Resultado", presentation.title)
        assertNull(presentation.scoreLabel)
        assertEquals("Resultado persistido sem placar completo.", presentation.stateLabel)
    }

    private fun matchRow(
        processed: Boolean,
        homeGoals: Int?,
        awayGoals: Int?,
    ) = CareerCalendarCatalogStore.MatchRow(
        matchId = "match-a",
        dayIndex = 3,
        eventTypeCode = 0,
        homeClubId = "club-a",
        homeClubName = "Clube A",
        awayClubId = "club-b",
        awayClubName = "Clube B",
        processed = processed,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
        competitionLinks = listOf(
            CareerCalendarCatalogStore.CompetitionLink(
                competitionId = "competition-a",
                roundNumber = 5,
                fixtureOrdinal = 0,
            )
        ),
    )
}

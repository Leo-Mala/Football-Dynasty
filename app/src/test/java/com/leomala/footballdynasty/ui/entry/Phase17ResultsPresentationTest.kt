package com.leomala.footballdynasty.ui.entry

import com.leomala.footballdynasty.application.career.CareerCalendarCatalogStore
import com.leomala.footballdynasty.application.career.CareerResultCatalogStore
import org.junit.Assert.assertEquals
import org.junit.Test

class Phase17ResultsPresentationTest {
    @Test
    fun `result presentation uses only persisted score and competition links`() {
        val row = CareerResultCatalogStore.ResultRow(
            matchId = "match-a",
            dayIndex = 4,
            eventTypeCode = 0,
            homeClubId = "club-a",
            homeClubName = "Clube A",
            awayClubId = "club-b",
            awayClubName = "Clube B",
            homeGoals = 2,
            awayGoals = 1,
            competitionLinks = listOf(
                CareerCalendarCatalogStore.CompetitionLink(
                    competitionId = "competition-a",
                    roundNumber = 3,
                    fixtureOrdinal = 0,
                )
            ),
        )

        val presentation = row.toPhase17ResultPresentation()

        assertEquals("Dia 5", presentation.dayLabel)
        assertEquals("Clube A x Clube B", presentation.matchupLabel)
        assertEquals("2 x 1", presentation.scoreLabel)
        assertEquals(listOf("competition-a • Rodada 3"), presentation.competitionLabels)
    }
}

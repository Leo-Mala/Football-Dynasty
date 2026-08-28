package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorSquadPlayerViewTest {
    @Test
    fun preservesProvenSeniorFieldsWithoutInterpretation() {
        val senior = Player("p1", "club-a", RosterKind.SENIOR, "Alpha", -1, 0, -2, 7, -3, -4, 0, true, false)
        val club = Club(
            id = "club-a",
            sourceFileRef = "fixture.ban",
            name = "Fixture Club",
            country = 0,
            state = 0,
            level = 0,
            stadium = "",
            capacity = 0,
            reputation = 0,
            players = listOf(
                senior.copy(id = "j1", rosterKind = RosterKind.JUNIOR),
                senior,
                senior.copy(id = "f1", clubId = "club-b"),
            ),
        )

        assertEquals(
            listOf(LegacySeniorSquadPlayerView("p1", "Alpha", -1, 0, -2, 7, -3, -4, 0, true, false)),
            LegacySeniorSquadPlayerViews.from(club),
        )
    }
}

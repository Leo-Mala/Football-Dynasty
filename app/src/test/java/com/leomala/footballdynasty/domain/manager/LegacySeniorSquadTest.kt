package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorSquadTest {
    @Test
    fun `projects only club senior players and preserves source order`() {
        val seniorA = player("senior-a", "club-a", RosterKind.SENIOR)
        val junior = player("junior-a", "club-a", RosterKind.JUNIOR)
        val foreignSenior = player("foreign", "club-b", RosterKind.SENIOR)
        val seniorB = player("senior-b", "club-a", RosterKind.SENIOR)
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
            players = listOf(seniorA, junior, foreignSenior, seniorB),
        )

        assertEquals(listOf(seniorA, seniorB), LegacySeniorSquad.players(club))
    }

    private fun player(id: String, clubId: String, rosterKind: RosterKind) = Player(
        id = id,
        clubId = clubId,
        rosterKind = rosterKind,
        name = id,
        age = 20,
        country = 0,
        position = 0,
        status = 0,
        side = 0,
        cr1 = 0,
        cr2 = 0,
        star = false,
        worldTop = false,
    )
}

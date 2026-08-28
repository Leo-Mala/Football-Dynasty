package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorSquadPositionsTest {
    @Test
    fun `groups senior squad by raw legacy position code preserving source order`() {
        val p1 = player("p1", "club-a", RosterKind.SENIOR, 2)
        val junior = player("j1", "club-a", RosterKind.JUNIOR, 2)
        val p2 = player("p2", "club-a", RosterKind.SENIOR, 1)
        val foreign = player("f1", "club-b", RosterKind.SENIOR, 2)
        val p3 = player("p3", "club-a", RosterKind.SENIOR, 2)
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
            players = listOf(p1, junior, p2, foreign, p3),
        )

        assertEquals(
            linkedMapOf(
                2 to listOf(p1, p3),
                1 to listOf(p2),
            ),
            LegacySeniorSquadPositions.playersByPosition(club),
        )
    }

    private fun player(
        id: String,
        clubId: String,
        rosterKind: RosterKind,
        position: Int,
    ) = Player(
        id = id,
        clubId = clubId,
        rosterKind = rosterKind,
        name = id,
        age = 20,
        country = 0,
        position = position,
        status = 0,
        side = 0,
        cr1 = 0,
        cr2 = 0,
        star = false,
        worldTop = false,
    )
}

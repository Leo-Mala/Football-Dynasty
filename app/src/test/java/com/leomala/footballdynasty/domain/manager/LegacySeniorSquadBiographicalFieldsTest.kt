package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorSquadBiographicalFieldsTest {
    @Test
    fun `preserves raw senior biographical fields and source order without interpretation`() {
        val p1 = player("p1", "club-a", RosterKind.SENIOR, "Alpha", -7, 0)
        val junior = player("j1", "club-a", RosterKind.JUNIOR, "Junior", 99, 88)
        val foreign = player("f1", "club-b", RosterKind.SENIOR, "Foreign", 77, 66)
        val p2 = player("p2", "club-a", RosterKind.SENIOR, "Beta", 12, 34)
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
            players = listOf(p1, junior, foreign, p2),
        )

        assertEquals(
            listOf(
                LegacySeniorPlayerBiographicalFields("p1", "Alpha", -7, 0),
                LegacySeniorPlayerBiographicalFields("p2", "Beta", 12, 34),
            ),
            LegacySeniorSquadBiographicalFields.from(club),
        )
    }

    private fun player(
        id: String,
        clubId: String,
        rosterKind: RosterKind,
        name: String,
        age: Int,
        country: Int,
    ) = Player(
        id = id,
        clubId = clubId,
        rosterKind = rosterKind,
        name = name,
        age = age,
        country = country,
        position = 0,
        status = 0,
        side = 0,
        cr1 = 0,
        cr2 = 0,
        star = false,
        worldTop = false,
    )
}

package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacySeniorSquadTraitsTest {
    @Test
    fun `preserves raw senior trait fields and source order without interpretation`() {
        val p1 = player("p1", "club-a", RosterKind.SENIOR, -7, 0, true, false)
        val junior = player("j1", "club-a", RosterKind.JUNIOR, 99, 88, true, true)
        val foreign = player("f1", "club-b", RosterKind.SENIOR, 77, 66, false, true)
        val p2 = player("p2", "club-a", RosterKind.SENIOR, 12, 34, false, true)
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
                LegacySeniorPlayerTraits("p1", -7, 0, true, false),
                LegacySeniorPlayerTraits("p2", 12, 34, false, true),
            ),
            LegacySeniorSquadTraits.from(club),
        )
    }

    private fun player(
        id: String,
        clubId: String,
        rosterKind: RosterKind,
        cr1: Int,
        cr2: Int,
        star: Boolean,
        worldTop: Boolean,
    ) = Player(
        id = id,
        clubId = clubId,
        rosterKind = rosterKind,
        name = id,
        age = 20,
        country = 0,
        position = 0,
        status = 0,
        side = 0,
        cr1 = cr1,
        cr2 = cr2,
        star = star,
        worldTop = worldTop,
    )
}

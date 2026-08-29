package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyJuniorSourceSelectorsTest {
    @Test
    fun `raw junior selectors preserve exact collection order and provenance`() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior", position = 9, status = 8, side = 7, legacyHash = 100)),
                juniors = listOf(
                    player("Junior A", position = 2, status = 3, side = 4, legacyHash = 201),
                    player("Junior B", position = 5, status = 6, side = 4, legacyHash = 202),
                    player("Junior C", position = 2, status = 6, side = 1, legacyHash = 203),
                ),
            ),
        )

        assertEquals(
            listOf(0, 2),
            squads.juniorRefsByPositionCode(2).map { it.sourceIndex },
        )
        assertEquals(
            listOf(1, 2),
            squads.juniorRefsByStatusCode(6).map { it.sourceIndex },
        )
        assertEquals(
            listOf(0, 1),
            squads.juniorRefsBySideCode(4).map { it.sourceIndex },
        )
        assertTrue(
            (
                squads.juniorRefsByPositionCode(2) +
                    squads.juniorRefsByStatusCode(6) +
                    squads.juniorRefsBySideCode(4)
                ).all { ref ->
                ref.sourceFileRef == "teams/exact.ban" &&
                    ref.rosterKind == LegacySourceRosterKind.JUNIOR
            },
        )
    }

    @Test
    fun `raw junior selectors never fall back to senior collection or unknown codes`() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior", position = 2, status = 3, side = 4, legacyHash = 100)),
                juniors = listOf(player("Junior", position = 5, status = 6, side = 7, legacyHash = 200)),
            ),
        )

        assertTrue(squads.juniorRefsByPositionCode(2).isEmpty())
        assertTrue(squads.juniorRefsByStatusCode(3).isEmpty())
        assertTrue(squads.juniorRefsBySideCode(4).isEmpty())
        assertTrue(squads.juniorRefsByPositionCode(Int.MIN_VALUE).isEmpty())
        assertTrue(squads.juniorRefsByStatusCode(Int.MAX_VALUE).isEmpty())
        assertTrue(squads.juniorRefsBySideCode(Int.MIN_VALUE).isEmpty())
    }

    private fun player(
        name: String,
        position: Int,
        status: Int,
        side: Int,
        legacyHash: Int,
    ): LegacyPlayerSnapshot = LegacyPlayerSnapshot(
        name = name,
        age = 17,
        country = 1,
        position = position,
        status = status,
        side = side,
        cr1 = 5,
        cr2 = 6,
        star = false,
        worldTop = false,
        legacyAid = 7,
        legacySid = 8,
        legacyTid = 9,
        legacyHash = legacyHash,
    )

    private fun team(
        players: List<LegacyPlayerSnapshot>,
        juniors: List<LegacyPlayerSnapshot>,
    ): LegacyTeamSnapshot = LegacyTeamSnapshot(
        name = "Exact",
        fileRef = "teams/exact.ban",
        country = 1,
        state = 2,
        level = 3,
        stadium = "Stadium",
        capacity = 10000,
        reputation = 4,
        players = players,
        juniors = juniors,
    )
}

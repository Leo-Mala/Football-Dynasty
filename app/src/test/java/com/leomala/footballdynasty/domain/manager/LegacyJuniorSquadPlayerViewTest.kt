package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyJuniorSquadPlayerViewTest {
    @Test
    fun preservesJuniorCollectionOrderAndOpaqueSourceFields() {
        val first = player("Junior A", legacyHash = 401)
        val second = player("Junior B", legacyHash = 402)
        val team = team(
            players = listOf(player("Senior", legacyHash = 999)),
            juniors = listOf(first, second),
        )

        val result = LegacySourceJuniorSquadPlayerViews.from(team)

        assertEquals(listOf("Junior A", "Junior B"), result.map { it.name })
        assertEquals(listOf(401, 402), result.map { it.legacyHash })
        assertEquals(listOf(201, 201), result.map { it.legacyAid })
        assertEquals(listOf(202, 202), result.map { it.legacySid })
        assertEquals(listOf(203, 203), result.map { it.legacyTid })
    }

    @Test
    fun doesNotMergeSeniorPlayersIntoJuniorProjection() {
        val team = team(
            players = listOf(player("Senior", legacyHash = 999)),
            juniors = listOf(player("Junior", legacyHash = 401)),
        )

        val result = LegacySourceJuniorSquadPlayerViews.from(team)

        assertEquals(listOf("Junior"), result.map { it.name })
        assertEquals(listOf(401), result.map { it.legacyHash })
    }

    private fun player(name: String, legacyHash: Int): LegacyPlayerSnapshot = LegacyPlayerSnapshot(
        name = name,
        age = 17,
        country = 1,
        position = 2,
        status = 3,
        side = 4,
        cr1 = 5,
        cr2 = 6,
        star = false,
        worldTop = false,
        legacyAid = 201,
        legacySid = 202,
        legacyTid = 203,
        legacyHash = legacyHash,
    )

    private fun team(
        players: List<LegacyPlayerSnapshot>,
        juniors: List<LegacyPlayerSnapshot>,
    ): LegacyTeamSnapshot = LegacyTeamSnapshot(
        name = "Team",
        fileRef = "teams/team.ban",
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

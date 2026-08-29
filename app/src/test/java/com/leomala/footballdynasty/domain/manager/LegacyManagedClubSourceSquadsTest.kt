package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagedClubSourceSquadsTest {
    @Test
    fun preservesExactSourceRefAndSeparateCollectionOrder() {
        val team = team(
            players = listOf(player("Senior A", 101), player("Senior B", 102)),
            juniors = listOf(player("Junior A", 201), player("Junior B", 202)),
        )

        val result = LegacyManagedClubSourceSquadProjection.from(team)

        assertEquals("teams/exact.ban", result.sourceFileRef)
        assertEquals(listOf("Senior A", "Senior B"), result.senior.map { it.name })
        assertEquals(listOf(101, 102), result.senior.map { it.legacyHash })
        assertEquals(listOf("Junior A", "Junior B"), result.juniors.map { it.name })
        assertEquals(listOf(201, 202), result.juniors.map { it.legacyHash })
    }

    @Test
    fun keepsSeniorAndJuniorCollectionsDisjoint() {
        val team = team(
            players = listOf(player("Senior", 301)),
            juniors = listOf(player("Junior", 401)),
        )

        val result = LegacyManagedClubSourceSquadProjection.from(team)

        assertEquals(listOf("Senior"), result.senior.map { it.name })
        assertEquals(listOf("Junior"), result.juniors.map { it.name })
    }

    private fun player(name: String, legacyHash: Int): LegacyPlayerSnapshot = LegacyPlayerSnapshot(
        name = name,
        age = 18,
        country = 1,
        position = 2,
        status = 3,
        side = 4,
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

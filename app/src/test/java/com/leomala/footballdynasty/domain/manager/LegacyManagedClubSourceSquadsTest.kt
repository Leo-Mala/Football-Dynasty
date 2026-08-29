package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

    @Test
    fun createsExactCollectionAndIndexReferencesWithoutFactMatching() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Same", 501), player("Same", 502)),
                juniors = listOf(player("Same", 601)),
            ),
        )

        assertEquals(
            listOf(
                LegacySourcePlayerRef(LegacySourceRosterKind.SENIOR, 0),
                LegacySourcePlayerRef(LegacySourceRosterKind.SENIOR, 1),
            ),
            result.seniorRefs(),
        )
        assertEquals(
            listOf(LegacySourcePlayerRef(LegacySourceRosterKind.JUNIOR, 0)),
            result.juniorRefs(),
        )
        assertEquals(502, result.seniorPlayer(result.seniorRefs()[1])?.legacyHash)
        assertEquals(601, result.juniorPlayer(result.juniorRefs()[0])?.legacyHash)
    }

    @Test
    fun neverFallsBackAcrossCollectionsOrOutsideExactSourceIndex() {
        val result = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Shared", 701)),
                juniors = listOf(player("Shared", 801)),
            ),
        )

        assertNull(result.seniorPlayer(LegacySourcePlayerRef(LegacySourceRosterKind.JUNIOR, 0)))
        assertNull(result.juniorPlayer(LegacySourcePlayerRef(LegacySourceRosterKind.SENIOR, 0)))
        assertNull(result.seniorPlayer(LegacySourcePlayerRef(LegacySourceRosterKind.SENIOR, 1)))
        assertNull(result.juniorPlayer(LegacySourcePlayerRef(LegacySourceRosterKind.JUNIOR, 1)))
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

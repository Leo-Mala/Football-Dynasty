package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyRawSourcePlayerCodeSelectorTest {
    @Test
    fun `senior raw code selection is conjunctive and preserves source identity and order`() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(
                    player("Senior A", position = 2, status = 6, side = 4, legacyHash = 101),
                    player("Senior B", position = 2, status = 3, side = 4, legacyHash = 102),
                    player("Senior C", position = 2, status = 6, side = 4, legacyHash = 103),
                    player("Senior D", position = 5, status = 6, side = 4, legacyHash = 104),
                ),
                juniors = listOf(
                    player("Junior Same Codes", position = 2, status = 6, side = 4, legacyHash = 201),
                ),
            ),
        )

        val refs = squads.seniorRefsMatchingRawCodes(
            LegacyRawSourcePlayerCodes(positionCode = 2, statusCode = 6, sideCode = 4),
        )

        assertEquals(listOf(0, 2), refs.map { it.sourceIndex })
        assertTrue(refs.all { it.sourceFileRef == "teams/exact.ban" })
        assertTrue(refs.all { it.rosterKind == LegacySourceRosterKind.SENIOR })
    }

    @Test
    fun `junior raw code selection stays isolated and supports omitted raw fields`() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(
                    player("Senior Same Codes", position = 9, status = 6, side = 4, legacyHash = 101),
                ),
                juniors = listOf(
                    player("Junior A", position = 2, status = 6, side = 4, legacyHash = 201),
                    player("Junior B", position = 5, status = 6, side = 1, legacyHash = 202),
                    player("Junior C", position = 7, status = 3, side = 4, legacyHash = 203),
                ),
            ),
        )

        val refs = squads.juniorRefsMatchingRawCodes(
            LegacyRawSourcePlayerCodes(statusCode = 6),
        )

        assertEquals(listOf(0, 1), refs.map { it.sourceIndex })
        assertTrue(refs.all { it.rosterKind == LegacySourceRosterKind.JUNIOR })
        assertEquals(
            squads.juniorRefs(),
            squads.juniorRefsMatchingRawCodes(LegacyRawSourcePlayerCodes()),
        )
    }

    @Test
    fun `raw code selection returns empty when no exact conjunction exists`() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior", position = 2, status = 6, side = 4, legacyHash = 101)),
                juniors = listOf(player("Junior", position = 2, status = 6, side = 4, legacyHash = 201)),
            ),
        )
        val impossible = LegacyRawSourcePlayerCodes(positionCode = 2, statusCode = 6, sideCode = 99)

        assertTrue(squads.seniorRefsMatchingRawCodes(impossible).isEmpty())
        assertTrue(squads.juniorRefsMatchingRawCodes(impossible).isEmpty())
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

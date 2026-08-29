package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyResolvedSourcePlayerTest {
    @Test
    fun resolvesExactSeniorAndJuniorReferencesWithoutCollapsingCollectionType() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Shared", 101)),
                juniors = listOf(player("Shared", 201)),
            ),
        )

        val senior = squads.player(squads.seniorRefs().single())
        val junior = squads.player(squads.juniorRefs().single())

        assertTrue(senior is LegacyResolvedSourcePlayer.Senior)
        assertTrue(junior is LegacyResolvedSourcePlayer.Junior)
        assertEquals(101, (senior as LegacyResolvedSourcePlayer.Senior).player.legacyHash)
        assertEquals(201, (junior as LegacyResolvedSourcePlayer.Junior).player.legacyHash)
        assertEquals(LegacySourceRosterKind.SENIOR, senior.ref.rosterKind)
        assertEquals(LegacySourceRosterKind.JUNIOR, junior.ref.rosterKind)
    }

    @Test
    fun genericResolutionRejectsWrongFileAndMissingIndexWithoutFallback() {
        val squads = LegacyManagedClubSourceSquadProjection.from(
            team(
                players = listOf(player("Senior", 301)),
                juniors = listOf(player("Junior", 401)),
            ),
        )

        assertNull(
            squads.player(
                LegacySourcePlayerRef(
                    sourceFileRef = "teams/other.ban",
                    rosterKind = LegacySourceRosterKind.SENIOR,
                    sourceIndex = 0,
                ),
            ),
        )
        assertNull(
            squads.player(
                LegacySourcePlayerRef(
                    sourceFileRef = "teams/exact.ban",
                    rosterKind = LegacySourceRosterKind.JUNIOR,
                    sourceIndex = 1,
                ),
            ),
        )
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

package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerCalendarState
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ManagedClubState
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManagedClubOverviewTest {
    @Test
    fun composesOnlyProvenManagedClubProfileAndSeniorSquad() {
        val other = club("club-a", emptyList())
        val managed = club(
            "club-b",
            listOf(
                player("junior", "club-b", RosterKind.JUNIOR),
                player("senior", "club-b", RosterKind.SENIOR),
            ),
        )

        assertEquals(
            ManagedClubOverview(
                profile = LegacyManagedClubProfile(
                    clubId = "club-b",
                    country = 11,
                    state = 22,
                    level = 33,
                    stadium = "Legacy Stadium",
                    capacity = 44444,
                    reputation = 55,
                ),
                squad = ManagedClubSquadView(
                    clubId = "club-b",
                    players = listOf(
                        LegacySeniorSquadPlayerView(
                            playerId = "senior",
                            name = "senior",
                            age = 27,
                            country = 1,
                            position = 2,
                            status = 3,
                            side = 4,
                            cr1 = 5,
                            cr2 = 6,
                            star = true,
                            worldTop = false,
                        ),
                    ),
                ),
            ),
            ManagedClubOverviews.from(career("club-b"), listOf(other, managed)),
        )
    }

    @Test
    fun doesNotFallbackWhenPersistedManagedClubIsMissing() {
        assertNull(ManagedClubOverviews.from(career("missing"), listOf(club("club-a", emptyList()))))
    }

    private fun career(managedClubId: String): CareerState = CareerState(
        id = "career-1",
        season = SeasonState(number = 1, year = 2026),
        calendar = CareerCalendarState(
            year = 2026,
            currentDayIndex = 0,
            startDayIndex = 0,
            dayCount = 365,
        ),
        managedClub = ManagedClubState(managedClubId),
        random = CareerRandomState(initialSeed = 1L, internalState = 1L, draws = 0L),
    )

    private fun club(id: String, players: List<Player>): Club = Club(
        id = id,
        sourceFileRef = "$id.ban",
        name = id,
        country = 11,
        state = 22,
        level = 33,
        stadium = "Legacy Stadium",
        capacity = 44444,
        reputation = 55,
        players = players,
    )

    private fun player(id: String, clubId: String, rosterKind: RosterKind): Player = Player(
        id = id,
        clubId = clubId,
        rosterKind = rosterKind,
        name = id,
        age = 27,
        country = 1,
        position = 2,
        status = 3,
        side = 4,
        cr1 = 5,
        cr2 = 6,
        star = true,
        worldTop = false,
    )
}

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

class ManagedClubSquadViewTest {
    @Test
    fun exposesOnlySeniorPlayersFromPersistedManagedClub() {
        val other = club("club-a", listOf(player("a1", "club-a", RosterKind.SENIOR)))
        val managed = club(
            "club-b",
            listOf(
                player("b-junior", "club-b", RosterKind.JUNIOR),
                player("b-senior", "club-b", RosterKind.SENIOR),
                player("foreign-membership", "club-a", RosterKind.SENIOR),
            ),
        )

        assertEquals(
            ManagedClubSquadView(
                clubId = "club-b",
                players = listOf(
                    LegacySeniorSquadPlayerView(
                        playerId = "b-senior",
                        name = "b-senior",
                        age = -1,
                        country = 0,
                        position = -2,
                        status = 7,
                        side = -3,
                        cr1 = -4,
                        cr2 = 0,
                        star = true,
                        worldTop = false,
                    ),
                ),
            ),
            ManagedClubSquadViews.from(career("club-b"), listOf(other, managed)),
        )
    }

    @Test
    fun remainsUnresolvedWhenPersistedManagedClubDoesNotExist() {
        assertNull(ManagedClubSquadViews.from(career("missing"), listOf(club("club-a", emptyList()))))
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
        country = 0,
        state = 0,
        level = 0,
        stadium = "",
        capacity = 0,
        reputation = 0,
        players = players,
    )

    private fun player(id: String, clubId: String, rosterKind: RosterKind): Player = Player(
        id,
        clubId,
        rosterKind,
        id,
        -1,
        0,
        -2,
        7,
        -3,
        -4,
        0,
        true,
        false,
    )
}

package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerCalendarState
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ManagedClubState
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class ManagedClubManagerSourceProfileTest {
    @Test
    fun `manager source profile comes from exact sourceFileRef provenance`() {
        val club = Club(
            id = "managed",
            sourceFileRef = "teams/exact.ban",
            name = "Mapped Club",
            country = 1,
            state = 2,
            level = 3,
            stadium = "Mapped Stadium",
            capacity = 100,
            reputation = 4,
            players = emptyList(),
        )
        val wrong = legacyTeam("teams/wrong.ban", "Wrong Club", "Wrong Stadium", 111)
        val exact = legacyTeam("teams/exact.ban", "Exact Legacy Club", "Exact Stadium", 222)

        val result = ManagedClubManagerViews.from(
            career = CareerState(
                id = "career",
                season = SeasonState(number = 1, year = 2026),
                calendar = CareerCalendarState(
                    year = 2026,
                    currentDayIndex = 0,
                    startDayIndex = 0,
                    dayCount = 365,
                ),
                managedClub = ManagedClubState("managed"),
                random = CareerRandomState(initialSeed = 1L, internalState = 1L, draws = 0L),
            ),
            clubs = listOf(club),
            legacyTeams = listOf(wrong, exact),
        )

        assertEquals("teams/exact.ban", result?.sourceProfile?.sourceFileRef)
        assertEquals("Exact Legacy Club", result?.sourceProfile?.name)
        assertEquals("Exact Stadium", result?.sourceProfile?.stadium)
        assertEquals(222, result?.sourceProfile?.capacity)
    }

    private fun legacyTeam(
        fileRef: String,
        name: String,
        stadium: String,
        capacity: Int,
    ): LegacyTeamSnapshot = LegacyTeamSnapshot(
        name = name,
        fileRef = fileRef,
        country = 7,
        state = 8,
        level = 9,
        stadium = stadium,
        capacity = capacity,
        reputation = 10,
        players = emptyList(),
        juniors = emptyList(),
    )
}

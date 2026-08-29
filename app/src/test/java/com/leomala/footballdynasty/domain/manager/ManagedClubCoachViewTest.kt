package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerCalendarState
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ManagedClubState
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManagedClubCoachViewTest {
    @Test
    fun resolvesCoachOnlyThroughExactManagedClubSourceFile() {
        val club = club("club-b", "teams/b.ban")
        val wrongSameName = legacyTeam("teams/a.ban", "Coach A", 10)
        val exactSource = legacyTeam("teams/b.ban", "Coach B", 20)

        assertEquals(
            ManagedClubCoachView(
                clubId = "club-b",
                coach = LegacyCoachProfile(
                    teamFileRef = "teams/b.ban",
                    coachName = "Coach B",
                    coachCountry = 20,
                ),
            ),
            ManagedClubCoachViews.from(
                career = career("club-b"),
                clubs = listOf(club),
                legacyTeams = listOf(wrongSameName, exactSource),
            ),
        )
    }

    @Test
    fun doesNotFallbackWhenManagedClubOrExactLegacySourceIsMissing() {
        val club = club("club-b", "teams/b.ban")
        val otherSource = legacyTeam("teams/a.ban", "Coach A", 10)

        assertNull(
            ManagedClubCoachViews.from(
                career = career("missing"),
                clubs = listOf(club),
                legacyTeams = listOf(otherSource),
            ),
        )
        assertNull(
            ManagedClubCoachViews.from(
                career = career("club-b"),
                clubs = listOf(club),
                legacyTeams = listOf(otherSource),
            ),
        )
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

    private fun club(id: String, sourceFileRef: String): Club = Club(
        id = id,
        sourceFileRef = sourceFileRef,
        name = "Same Name",
        country = 1,
        state = 2,
        level = 3,
        stadium = "Legacy Stadium",
        capacity = 10000,
        reputation = 4,
        players = emptyList(),
    )

    private fun legacyTeam(fileRef: String, coach: String, coachCountry: Int): LegacyTeamSnapshot =
        LegacyTeamSnapshot(
            name = "Same Name",
            fileRef = fileRef,
            country = 1,
            state = 2,
            level = 3,
            stadium = "Legacy Stadium",
            capacity = 10000,
            reputation = 4,
            players = emptyList(),
            juniors = emptyList(),
            coach = coach,
            coachCountry = coachCountry,
        )
}

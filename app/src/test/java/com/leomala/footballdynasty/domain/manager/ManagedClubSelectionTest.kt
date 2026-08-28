package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.career.CareerCalendarState
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ManagedClubState
import com.leomala.footballdynasty.domain.career.SeasonState
import com.leomala.footballdynasty.domain.model.Club
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ManagedClubSelectionTest {
    @Test
    fun resolvesOnlyThePersistedManagedClubId() {
        val first = club("club-a")
        val managed = club("club-b")
        val career = career("club-b")

        assertEquals(managed, ManagedClubSelection.resolve(career, listOf(first, managed)))
    }

    @Test
    fun doesNotFallbackWhenPersistedManagedClubIsMissing() {
        val career = career("missing-club")

        assertNull(ManagedClubSelection.resolve(career, listOf(club("club-a"), club("club-b"))))
    }

    @Test
    fun returnsNullWhenCareerHasNoManagedClub() {
        assertNull(ManagedClubSelection.resolve(career(null), listOf(club("club-a"))))
    }

    private fun career(managedClubId: String?): CareerState = CareerState(
        id = "career-1",
        season = SeasonState(number = 1, year = 2026),
        calendar = CareerCalendarState(
            year = 2026,
            currentDayIndex = 0,
            startDayIndex = 0,
            dayCount = 365,
        ),
        managedClub = managedClubId?.let(::ManagedClubState),
        random = CareerRandomState(initialSeed = 1L, internalState = 1L, draws = 0L),
    )

    private fun club(id: String): Club = Club(
        id = id,
        sourceFileRef = "$id.ban",
        name = id,
        country = 0,
        state = 0,
        level = 0,
        stadium = "",
        capacity = 0,
        reputation = 0,
        players = emptyList(),
    )
}

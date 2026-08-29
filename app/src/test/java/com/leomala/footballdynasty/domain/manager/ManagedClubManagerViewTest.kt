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
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagedClubManagerViewTest {
    @Test
    fun composesOverviewCoachVisualIdentityAndExactLegacyProvenanceForPersistedClub() {
        val club = club("club-b", "teams/b.ban")
        val legacyTeam = legacyTeam(
            fileRef = "teams/b.ban",
            coach = "Coach B",
            coachCountry = 20,
            primaryColor = " Legacy Primary ",
            secondaryColor = "Legacy Secondary",
            baseColor = 0x123456,
            legacyAid = 101,
            legacySid = 102,
            legacyTid = 103,
            legacyVid = 104,
            legacyId = 105,
            legacyValid = true,
        )

        val result = ManagedClubManagerViews.from(
            career = career("club-b"),
            clubs = listOf(club),
            legacyTeams = listOf(legacyTeam),
        )

        assertEquals("club-b", result?.overview?.profile?.clubId)
        assertEquals("club-b", result?.overview?.squad?.clubId)
        assertSame(legacyTeam, result?.legacyTeam)
        assertEquals(101, result?.sourceIdentity?.legacyAid)
        assertEquals(102, result?.sourceIdentity?.legacySid)
        assertEquals(103, result?.sourceIdentity?.legacyTid)
        assertEquals(104, result?.sourceIdentity?.legacyVid)
        assertEquals(105, result?.sourceIdentity?.legacyId)
        assertTrue(result?.sourceIdentity?.legacyValid == true)
        assertEquals(" Legacy Primary ", result?.visualIdentity?.primaryColor)
        assertEquals("Legacy Secondary", result?.visualIdentity?.secondaryColor)
        assertEquals(0x123456, result?.visualIdentity?.baseColor)
        assertEquals("club-b", result?.coach?.clubId)
        assertEquals("Coach B", result?.coach?.coach?.coachName)
        assertEquals(20, result?.coach?.coach?.coachCountry)
    }

    @Test
    fun remainsUnavailableWhenEitherExactClubOrLegacySourceIsMissing() {
        val club = club("club-b", "teams/b.ban")
        val wrongLegacySource = legacyTeam("teams/a.ban", "Coach A", 10)

        assertNull(
            ManagedClubManagerViews.from(
                career = career("missing"),
                clubs = listOf(club),
                legacyTeams = listOf(wrongLegacySource),
            ),
        )
        assertNull(
            ManagedClubManagerViews.from(
                career = career("club-b"),
                clubs = listOf(club),
                legacyTeams = listOf(wrongLegacySource),
            ),
        )
    }

    @Test
    fun doesNotFallbackToAnotherLegacyTeamWithMatchingPresentationFields() {
        val club = club("club-b", "teams/b.ban")
        val wrongSource = legacyTeam(
            fileRef = "teams/a.ban",
            coach = "Coach A",
            coachCountry = 10,
            primaryColor = "Wrong Primary",
            secondaryColor = "Wrong Secondary",
            baseColor = 1,
            legacyId = 999,
        )
        val exactSource = legacyTeam(
            fileRef = "teams/b.ban",
            coach = "Coach B",
            coachCountry = 20,
            primaryColor = "Exact Primary",
            secondaryColor = "Exact Secondary",
            baseColor = 2,
            legacyId = 123,
        )

        val result = ManagedClubManagerViews.from(
            career = career("club-b"),
            clubs = listOf(club),
            legacyTeams = listOf(wrongSource, exactSource),
        )

        assertSame(exactSource, result?.legacyTeam)
        assertEquals(123, result?.sourceIdentity?.legacyId)
        assertEquals("Coach B", result?.coach?.coach?.coachName)
        assertEquals("Exact Primary", result?.visualIdentity?.primaryColor)
        assertEquals("Exact Secondary", result?.visualIdentity?.secondaryColor)
        assertEquals(2, result?.visualIdentity?.baseColor)
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

    private fun legacyTeam(
        fileRef: String,
        coach: String,
        coachCountry: Int,
        primaryColor: String = "",
        secondaryColor: String = "",
        baseColor: Int = 0,
        legacyAid: Int = 0,
        legacySid: Int = 0,
        legacyTid: Int = 0,
        legacyVid: Int = 0,
        legacyId: Int = 0,
        legacyValid: Boolean = false,
    ): LegacyTeamSnapshot = LegacyTeamSnapshot(
        name = "Same Name",
        fileRef = fileRef,
        country = 1,
        state = 2,
        level = 3,
        stadium = "Legacy Stadium",
        capacity = 10000,
        reputation = 4,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        baseColor = baseColor,
        players = emptyList(),
        juniors = emptyList(),
        coach = coach,
        coachCountry = coachCountry,
        legacyAid = legacyAid,
        legacySid = legacySid,
        legacyTid = legacyTid,
        legacyVid = legacyVid,
        legacyId = legacyId,
        legacyValid = legacyValid,
    )
}

package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyCoachAssociatedClub
import com.leomala.footballdynasty.domain.manager.LegacyCoachLeagueStandingInput
import org.junit.Assert.assertEquals
import org.junit.Test

class CareerCoachPostMatchUpdateResolverTest {
    @Test
    fun `type seven runs j only in exact home then away manager order`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)
        val homeCoach = coach(0, 100, currentClubId = home.clubId, rawG = 44, rawH = 45)
        val awayCoach = coach(1, 200, currentClubId = away.clubId, rawG = 54, rawH = 55)

        val updates = CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(homeCoach, awayCoach),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = 200,
                rawCompetitionType = 7,
                homeGoals = 2,
                awayGoals = 1,
            ),
        )

        assertEquals(listOf("home", "away"), updates.map { it.resolvedClubId })
        assertEquals(homeCoach, updates[0].expectedBefore)
        assertEquals(1, updates[0].after.rawD)
        assertEquals(1, updates[0].after.rawE)
        assertEquals(0, updates[0].after.rawF)
        assertEquals(44, updates[0].after.rawG)
        assertEquals(45, updates[0].after.rawH)

        assertEquals(awayCoach, updates[1].expectedBefore)
        assertEquals(1, updates[1].after.rawD)
        assertEquals(0, updates[1].after.rawE)
        assertEquals(1, updates[1].after.rawF)
        assertEquals(54, updates[1].after.rawG)
        assertEquals(55, updates[1].after.rawH)
    }

    @Test
    fun `same first manager resolved by both clubs observes home mutation before away call`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)
        val shared = coach(0, 100, currentClubId = home.clubId)

        val updates = CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(shared),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = 100,
                rawCompetitionType = 7,
                homeGoals = 1,
                awayGoals = 0,
            ),
        )

        assertEquals(2, updates.size)
        assertEquals("home", updates[0].resolvedClubId)
        assertEquals("away", updates[1].resolvedClubId)
        assertEquals(shared, updates[0].expectedBefore)
        assertEquals(updates[0].after, updates[1].expectedBefore)
        assertEquals(1, updates[0].after.rawD)
        assertEquals(2, updates[1].after.rawD)
        assertEquals(2, updates[1].after.rawE)
    }

    @Test
    fun `type one composes j before exact i adjustment evidence`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)
        val before = coach(
            sourceOrdinal = 0,
            legacyManagerId = 100,
            currentClubId = home.clubId,
            rawG = 50,
            rawH = 31,
        )

        val updates = CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(before),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = -1,
                rawCompetitionType = 1,
                homeGoals = 0,
                awayGoals = 4,
                subtype = 1,
                adjustment = CareerCoachPostMatchAdjustmentEvidence(
                    homeStrength = 80,
                    awayStrength = 70,
                    isLegacyLeagueCompetition = true,
                    standingByClubId = mapOf(
                        home.clubId to LegacyCoachLeagueStandingInput(
                            position = 17,
                            tableSize = 20,
                            relegationCount = 4,
                        )
                    ),
                ),
            ),
        )

        val after = updates.single().after
        assertEquals(1, after.rawD)
        assertEquals(0, after.rawE)
        assertEquals(1, after.rawF)
        // f0.i: home loss by four, strength >= opponent => -3; band 5 => -7;
        // H 31 -> 21; low H then applies the final G -5.
        assertEquals(35, after.rawG)
        assertEquals(21, after.rawH)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `caller type requiring i fails closed when exact adjustment evidence is absent`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)

        CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(coach(0, 100, currentClubId = home.clubId)),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = -1,
                rawCompetitionType = 1,
                homeGoals = 1,
                awayGoals = 0,
                subtype = 1,
            ),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `league i fails closed when standing evidence was not supplied`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)

        CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(coach(0, 100, currentClubId = home.clubId)),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = -1,
                rawCompetitionType = 1,
                homeGoals = 1,
                awayGoals = 0,
                subtype = 1,
                adjustment = CareerCoachPostMatchAdjustmentEvidence(
                    homeStrength = 70,
                    awayStrength = 70,
                    isLegacyLeagueCompetition = true,
                ),
            ),
        )
    }

    @Test
    fun `explicit null standing remains a characterized legacy miss instead of unknown evidence`() {
        val home = LegacyCoachAssociatedClub("home", 10)
        val away = LegacyCoachAssociatedClub("away", 20)
        val before = coach(0, 100, currentClubId = home.clubId, rawG = 50, rawH = 50)

        val after = CareerCoachPostMatchUpdateResolver.resolve(
            managersInWorldOrder = listOf(before),
            evidence = evidence(
                home = home,
                away = away,
                homeStoredManagerId = 100,
                awayStoredManagerId = -1,
                rawCompetitionType = 1,
                homeGoals = 1,
                awayGoals = 1,
                subtype = 1,
                adjustment = CareerCoachPostMatchAdjustmentEvidence(
                    homeStrength = 80,
                    awayStrength = 70,
                    isLegacyLeagueCompetition = true,
                    standingByClubId = mapOf(home.clubId to null),
                ),
            ),
        ).single().after

        // With a characterized c0.I miss, the matrix is skipped but the preliminary home-draw G -1
        // and H path still execute exactly as f0.i does.
        assertEquals(49, after.rawG)
    }

    private fun evidence(
        home: LegacyCoachAssociatedClub,
        away: LegacyCoachAssociatedClub,
        homeStoredManagerId: Int,
        awayStoredManagerId: Int,
        rawCompetitionType: Int,
        homeGoals: Int,
        awayGoals: Int,
        subtype: Int? = null,
        adjustment: CareerCoachPostMatchAdjustmentEvidence? = null,
    ) = CareerCoachPostMatchLegacyEvidence(
        seasonId = 2026,
        rawCompetitionType = rawCompetitionType,
        leagueCompetitionSubtype = subtype,
        homeClub = home,
        awayClub = away,
        homeStoredManagerId = homeStoredManagerId,
        awayStoredManagerId = awayStoredManagerId,
        homeGoals = homeGoals,
        awayGoals = awayGoals,
        associatedClubsById = mapOf(home.clubId to home, away.clubId to away),
        adjustment = adjustment,
    )

    private fun coach(
        sourceOrdinal: Int,
        legacyManagerId: Int,
        currentClubId: String?,
        rawG: Int = 50,
        rawH: Int = 50,
    ) = CareerCoachRuntimeState(
        sourceOrdinal = sourceOrdinal,
        legacyManagerId = legacyManagerId,
        isUserControlled = false,
        currentClubId = currentClubId,
        alternativeClubId = null,
        previousClubId = null,
        previousClubCountry = null,
        previousClubDivisionIndex = null,
        rawG = rawG,
        rawH = rawH,
        rawD = 0,
        rawE = 0,
        rawF = 0,
        rawO = 0,
        rawM = 0,
        records = emptyList(),
    )
}

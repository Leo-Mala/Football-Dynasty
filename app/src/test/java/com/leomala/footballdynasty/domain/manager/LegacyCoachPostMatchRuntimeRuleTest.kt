package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCoachPostMatchRuntimeRuleTest {
    @Test
    fun `standing projection reproduces exact c0 I bands and missing state`() {
        assertEquals(1, standing(type = 1, position = 1).band)
        assertEquals(2, standing(type = 1, position = 4).band)
        assertEquals(3, standing(type = 1, position = 10).band)
        assertEquals(4, standing(type = 1, position = 14).band)
        assertEquals(5, standing(type = 1, position = 17).band)
        assertEquals(2, standing(type = 3, position = 2).band)
        assertTrue(standing(type = 1, position = -1).missing)
        assertTrue(
            LegacyCoachStandingProjectionRule.resolve(
                rawCompetitionType = 4,
                isLegacyLeagueCompetition = true,
                input = LegacyCoachLeagueStandingInput(1, 20, 4),
            ).missing,
        )
    }

    @Test
    fun `i applies preliminary G then competition matrix then H dependent G`() {
        val result = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(rawG = 50, rawH = 31),
            side = LegacyCoachAdjustmentSide.HOME,
            type = 1,
            homeGoals = 0,
            awayGoals = 4,
            homeStrength = 80,
            awayStrength = 70,
            standing = LegacyCoachLeagueStandingInput(position = 17, tableSize = 20, relegationCount = 4),
        )

        // loss by 4 at home: G -3, band-5 league loss -7, H -5 then -5 => 21, then G -5.
        assertEquals(listOf(-3, -7, -5), result.gDeltasInOrder)
        assertEquals(35, result.state.rawG)
        assertEquals(21, result.state.rawH)
    }

    @Test
    fun `i preserves exact user debt penalties before matrix write`() {
        val league = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(50, 50),
            side = LegacyCoachAdjustmentSide.HOME,
            type = 1,
            homeGoals = 1,
            awayGoals = 0,
            homeStrength = 70,
            awayStrength = 80,
            standing = LegacyCoachLeagueStandingInput(3, 20, 4),
            user = true,
            cash = -1,
        )
        assertEquals(listOf(-10, 4), league.gDeltasInOrder)
        assertEquals(44, league.state.rawG)

        val cupLikeNonePath = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(50, 50),
            side = LegacyCoachAdjustmentSide.NONE,
            type = 4,
            homeGoals = 5,
            awayGoals = 1,
            homeStrength = 90,
            awayStrength = 20,
            standing = null,
            user = true,
            cash = -1,
        )
        // NONE forces effective 0-0, preliminary -1, debt -5, non-1/3 index 2 draw matrix +1.
        assertEquals(0, cupLikeNonePath.effectiveManagerGoals)
        assertEquals(0, cupLikeNonePath.effectiveOpponentGoals)
        assertEquals(listOf(-1, -5, 1), cupLikeNonePath.gDeltasInOrder)
        assertEquals(45, cupLikeNonePath.state.rawG)
    }

    @Test
    fun `i skips structurally present type 4 matrix when current club is an actual match side`() {
        val result = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(50, 50),
            side = LegacyCoachAdjustmentSide.HOME,
            type = 4,
            homeGoals = 1,
            awayGoals = 1,
            homeStrength = 80,
            awayStrength = 70,
            standing = LegacyCoachLeagueStandingInput(1, 20, 4),
        )

        // c0.I marks type 4 missing on a real side, therefore only preliminary home-draw G -1 is reached.
        assertTrue(result.standingProjection.missing)
        assertEquals(listOf(-1), result.gDeltasInOrder)
        assertEquals(49, result.state.rawG)
    }

    @Test
    fun `i preserves away and large margin H sequencing`() {
        val win = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(50, 95),
            side = LegacyCoachAdjustmentSide.AWAY,
            type = 2,
            homeGoals = 0,
            awayGoals = 4,
            homeStrength = 90,
            awayStrength = 70,
            standing = null,
        )
        assertEquals(100, win.state.rawH)

        val loss = adjustment(
            before = LegacyCoachPostMatchAdjustmentState(50, 5),
            side = LegacyCoachAdjustmentSide.HOME,
            type = 2,
            homeGoals = 0,
            awayGoals = 4,
            homeStrength = 70,
            awayStrength = 90,
            standing = null,
        )
        assertEquals(0, loss.state.rawH)
        assertEquals(-5, loss.gDeltasInOrder.last())
    }

    @Test
    fun `caller competition predicate remains exact and fail closed outside it`() {
        val before = LegacyCoachPostMatchAdjustmentState(41, 42)
        val result = adjustment(
            before = before,
            side = LegacyCoachAdjustmentSide.HOME,
            type = 7,
            homeGoals = 4,
            awayGoals = 0,
            homeStrength = 90,
            awayStrength = 10,
            standing = null,
        )
        assertFalse(result.applied)
        assertEquals(before, result.state)
    }

    @Test
    fun `j resolves current club before alternative l and alternative when current does not match`() {
        val current = LegacyCoachAssociatedClub("current", 10)
        val alternative = LegacyCoachAssociatedClub("alternative", 20)

        assertEquals(
            LegacyCoachAdjustmentSide.HOME,
            LegacyCoachMatchAssociationRule.resolve(current, alternative, "current", "alternative")?.side,
        )
        assertEquals(
            current,
            LegacyCoachMatchAssociationRule.resolve(current, alternative, "current", "alternative")?.club,
        )
        assertEquals(
            alternative,
            LegacyCoachMatchAssociationRule.resolve(current, alternative, "other", "alternative")?.club,
        )
        assertEquals(
            LegacyCoachAdjustmentSide.AWAY,
            LegacyCoachMatchAssociationRule.resolve(current, alternative, "other", "alternative")?.side,
        )
    }

    @Test
    fun `j updates first season club record and exact aggregate D E F o`() {
        val current = LegacyCoachAssociatedClub("club", 10)
        val first = LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 4, rawWins = 2, rawLosses = 1, rawPoints = 9)
        val duplicate = LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 100, rawWins = 100, rawLosses = 100, rawPoints = 100)
        val before = LegacyCoachPostMatchStatisticsState(10, 4, 3, 20, listOf(first, duplicate))

        val result = statistics(
            before = before,
            current = current,
            alternative = null,
            homeClubId = "club",
            awayClubId = "other",
            homeGoals = 2,
            awayGoals = 0,
            type = 1,
            subtype = 1,
        )

        assertEquals(0, result.updatedRecordIndex)
        assertEquals(11, result.state.rawD)
        assertEquals(5, result.state.rawE)
        assertEquals(3, result.state.rawF)
        assertEquals(24, result.state.rawO)
        assertEquals(first.copy(rawMatches = 5, rawWins = 3, rawPoints = 13), result.state.records[0])
        assertEquals(duplicate, result.state.records[1])
    }

    @Test
    fun `j appends new season club record without backfilling unrelated history`() {
        val current = LegacyCoachAssociatedClub("club", 10)
        val old = LegacyCoachSeasonClubRecord(2025, 10, rawMatches = 9, rawWins = 3, rawLosses = 3, rawPoints = 12, rawOtherCount = 2)
        val before = LegacyCoachPostMatchStatisticsState(9, 3, 3, 12, listOf(old))
        val result = statistics(
            before = before,
            current = current,
            alternative = null,
            homeClubId = "club",
            awayClubId = "other",
            homeGoals = 0,
            awayGoals = 1,
            type = 2,
            subtype = null,
        )

        assertEquals(1, result.updatedRecordIndex)
        assertEquals(old, result.state.records[0])
        assertEquals(
            LegacyCoachSeasonClubRecord(2026, 10, rawMatches = 1, rawLosses = 1),
            result.state.records[1],
        )
        assertEquals(10, result.state.rawD)
        assertEquals(4, result.state.rawF)
        assertEquals(12, result.state.rawO)
    }

    @Test
    fun `j uses exact win point table subtype and away bonus`() {
        val current = LegacyCoachAssociatedClub("club", 10)
        val before = LegacyCoachPostMatchStatisticsState(0, 0, 0, 0, emptyList())

        assertEquals(4, statistics(before, current, null, "club", "x", 1, 0, 1, 1).pointsAwarded)
        assertEquals(3, statistics(before, current, null, "club", "x", 1, 0, 1, 2).pointsAwarded)
        assertEquals(2, statistics(before, current, null, "club", "x", 1, 0, 1, 3).pointsAwarded)
        assertEquals(5, statistics(before, current, null, "x", "club", 0, 1, 1, 1).pointsAwarded)
        assertEquals(7, statistics(before, current, null, "x", "club", 0, 1, 5, null).pointsAwarded)
        assertEquals(5, statistics(before, current, null, "x", "club", 0, 1, 7, null).pointsAwarded)
        assertEquals(1, statistics(before, current, null, "x", "club", 0, 1, null, null).pointsAwarded)
    }

    @Test
    fun `j uses exact draw point table including alternative club mando`() {
        val current = LegacyCoachAssociatedClub("current", 10)
        val alternative = LegacyCoachAssociatedClub("national", 20)
        val before = LegacyCoachPostMatchStatisticsState(0, 0, 0, 0, emptyList())

        assertEquals(2, statistics(before, current, null, "current", "x", 1, 1, 1, 1).pointsAwarded)
        assertEquals(1, statistics(before, current, null, "current", "x", 1, 1, 1, 2).pointsAwarded)
        assertEquals(0, statistics(before, current, null, "current", "x", 1, 1, 3, 1).pointsAwarded)
        val alternativeAway = statistics(before, current, alternative, "x", "national", 2, 2, 4, null)
        assertEquals(alternative, alternativeAway.association.club)
        assertEquals(LegacyCoachAdjustmentSide.AWAY, alternativeAway.association.side)
        assertEquals(2, alternativeAway.pointsAwarded)
    }

    @Test(expected = IllegalStateException::class)
    fun `j fails closed when neither A nor l belongs to the match`() {
        statistics(
            before = LegacyCoachPostMatchStatisticsState(0, 0, 0, 0, emptyList()),
            current = LegacyCoachAssociatedClub("current", 10),
            alternative = LegacyCoachAssociatedClub("alternative", 20),
            homeClubId = "home",
            awayClubId = "away",
            homeGoals = 0,
            awayGoals = 0,
            type = 1,
            subtype = 1,
        )
    }

    private fun standing(type: Int, position: Int): LegacyCoachStandingProjection =
        LegacyCoachStandingProjectionRule.resolve(
            rawCompetitionType = type,
            isLegacyLeagueCompetition = true,
            input = LegacyCoachLeagueStandingInput(position, 20, 4),
        )

    private fun adjustment(
        before: LegacyCoachPostMatchAdjustmentState,
        side: LegacyCoachAdjustmentSide,
        type: Int,
        homeGoals: Int,
        awayGoals: Int,
        homeStrength: Int,
        awayStrength: Int,
        standing: LegacyCoachLeagueStandingInput?,
        user: Boolean = false,
        cash: Long? = 100L,
    ): LegacyCoachPostMatchAdjustmentResult =
        LegacyCoachPostMatchAdjustmentRule.apply(
            before,
            LegacyCoachPostMatchAdjustmentContext(
                rawCompetitionType = type,
                managerSide = side,
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                homeStrength = homeStrength,
                awayStrength = awayStrength,
                isLegacyLeagueCompetition = true,
                managerStanding = standing,
                managerIsUserControlled = user,
                currentClubCash = cash,
            ),
        )

    private fun statistics(
        before: LegacyCoachPostMatchStatisticsState,
        current: LegacyCoachAssociatedClub?,
        alternative: LegacyCoachAssociatedClub?,
        homeClubId: String,
        awayClubId: String,
        homeGoals: Int,
        awayGoals: Int,
        type: Int?,
        subtype: Int?,
    ): LegacyCoachPostMatchStatisticsResult =
        LegacyCoachPostMatchStatisticsRule.apply(
            before,
            LegacyCoachPostMatchStatisticsContext(
                seasonId = 2026,
                currentClub = current,
                alternativeClub = alternative,
                homeClubId = homeClubId,
                awayClubId = awayClubId,
                homeGoals = homeGoals,
                awayGoals = awayGoals,
                rawCompetitionType = type,
                leagueCompetitionSubtype = subtype,
            ),
        )
}

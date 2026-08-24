package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualSquadFloorRulesTest {
    @Test
    fun `annual maintenance skips Q0 clubs`() {
        assertTrue(LegacyAnnualSquadFloorRules.annualClubEligible(false))
        assertFalse(LegacyAnnualSquadFloorRules.annualClubEligible(true))
    }

    @Test
    fun `c0 n uses one attempt for each position below two three three five three`() {
        assertEquals(
            listOf(0, 1, 2, 3, 4),
            LegacyAnnualSquadFloorRules.deficientPositions(intArrayOf(1, 2, 2, 4, 2)),
        )
        assertEquals(
            emptyList<Int>(),
            LegacyAnnualSquadFloorRules.deficientPositions(intArrayOf(2, 3, 3, 5, 3)),
        )
        assertEquals(
            listOf(3),
            LegacyAnnualSquadFloorRules.deficientPositions(intArrayOf(4, 5, 5, 4, 5)),
        )
    }

    @Test
    fun `requested overall is the same threshold used by c0 n`() {
        assertEquals(40, LegacyAnnualSquadFloorRules.requestedOverall(true, 1, 0))
        assertEquals(50, LegacyAnnualSquadFloorRules.requestedOverall(false, 0, 4))
    }

    @Test
    fun `national donor window reaches two divisions below and one above`() {
        assertEquals(
            LegacyAnnualSquadFloorRules.DivisionRange(2, 5),
            LegacyAnnualSquadFloorRules.donorDivisionRange(true, 4),
        )
        assertEquals(
            LegacyAnnualSquadFloorRules.DivisionRange(0, 2),
            LegacyAnnualSquadFloorRules.donorDivisionRange(true, 1),
        )
    }

    @Test
    fun `standard donor window reaches one division below and one above`() {
        assertEquals(
            LegacyAnnualSquadFloorRules.DivisionRange(3, 5),
            LegacyAnnualSquadFloorRules.donorDivisionRange(false, 4),
        )
        assertEquals(
            LegacyAnnualSquadFloorRules.DivisionRange(0, 1),
            LegacyAnnualSquadFloorRules.donorDivisionRange(false, 0),
        )
    }

    @Test
    fun `standard donor filter requires same J and rejects target and Q0`() {
        assertTrue(
            LegacyAnnualSquadFloorRules.donorClubEligible(
                targetR0 = false,
                targetP0 = 3,
                targetJ = 7,
                donorP0 = 2,
                donorJ = 7,
                donorQ0 = false,
                isTarget = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorClubEligible(
                targetR0 = false,
                targetP0 = 3,
                targetJ = 7,
                donorP0 = 2,
                donorJ = 8,
                donorQ0 = false,
                isTarget = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorClubEligible(
                targetR0 = false,
                targetP0 = 3,
                targetJ = 7,
                donorP0 = 2,
                donorJ = 7,
                donorQ0 = true,
                isTarget = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorClubEligible(
                targetR0 = false,
                targetP0 = 3,
                targetJ = 7,
                donorP0 = 2,
                donorJ = 7,
                donorQ0 = false,
                isTarget = true,
            )
        )
    }

    @Test
    fun `national donor filter relies on the already scoped pool instead of J equality`() {
        assertTrue(
            LegacyAnnualSquadFloorRules.donorClubEligible(
                targetR0 = true,
                targetP0 = 3,
                targetJ = 7,
                donorP0 = 1,
                donorJ = 99,
                donorQ0 = false,
                isTarget = false,
            )
        )
    }

    @Test
    fun `donor player must match position requested overall band and flags`() {
        assertTrue(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 50,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = false,
            )
        )
        assertTrue(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 45,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = false,
            )
        )
        assertTrue(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 55,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 56,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 2,
                requestedPosition = 3,
                playerOverall = 50,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 50,
                requestedOverall = 50,
                playerO0 = true,
                playerW0 = false,
            )
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(
                playerPosition = 3,
                requestedPosition = 3,
                playerOverall = 50,
                requestedOverall = 50,
                playerO0 = false,
                playerW0 = true,
            )
        )
    }

    @Test
    fun `donor overall band is clamped to five and one hundred`() {
        assertTrue(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(0, 0, 5, 1, false, false)
        )
        assertFalse(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(0, 0, 4, 1, false, false)
        )
        assertTrue(
            LegacyAnnualSquadFloorRules.donorPlayerEligible(0, 0, 100, 99, false, false)
        )
    }

    @Test
    fun `donor surplus thresholds preserve three four four six four`() {
        assertTrue(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(0, 3))
        assertFalse(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(0, 2))
        assertTrue(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(1, 4))
        assertTrue(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(2, 4))
        assertTrue(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(3, 6))
        assertTrue(LegacyAnnualSquadFloorRules.donorHasSafeSurplus(4, 4))
    }
}

package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualPCompetitionRulesTest {
    @Test
    fun `failed cD guard only preserves unconditional global f1 clear`() {
        val calls = mutableListOf<Int>()
        val result =
            LegacyAnnualPCompetitionRules.apply<String>(
                hasLegacyGroup29 = false,
                legacyY1 = true,
                rounds = emptyList(),
                groupS0Pool = listOf("pool"),
                legacyF0 = { argument -> calls += argument; emptyList() },
            )

        assertFalse(result.guardedMutationRan)
        assertTrue(result.clearGlobalF1)
        assertNull(result.rebuiltW)
        assertTrue(calls.isEmpty())
    }

    @Test
    fun `qualified last round applies exact mutations then rebuilds W from first three plus refill`() {
        val calls = mutableListOf<Int>()
        val rounds =
            listOf(
                round(10, "a", "b"),
                round(20, "c"),
                round(30, "d"),
                round(2, "existing"),
            )

        val result =
            LegacyAnnualPCompetitionRules.apply(
                hasLegacyGroup29 = true,
                legacyY1 = true,
                rounds = rounds,
                groupS0Pool = listOf("x", "r1", "r2", "y"),
                legacyF0 = { argument ->
                    calls += argument
                    when (argument) {
                        2 -> listOf("r1", "r2")
                        124 -> listOf("fill-a", "fill-b")
                        else -> error("unexpected argument=$argument")
                    }
                },
            )

        assertTrue(result.guardedMutationRan)
        assertEquals(listOf(2, 124), calls)
        assertEquals(listOf("existing", "r1", "r2"), result.lastRoundParticipants)
        assertEquals(listOf("x", "y"), result.groupS0Pool)
        assertEquals(listOf("a", "b", "c", "d", "fill-a", "fill-b"), result.rebuiltW)
        assertEquals(
            listOf(
                LegacyAnnualPCompetitionRules.LastRoundAction.SET_A1_FALSE,
                LegacyAnnualPCompetitionRules.LastRoundAction.ADD_F0_PARTICIPANTS,
                LegacyAnnualPCompetitionRules.LastRoundAction.REMOVE_FROM_GROUP_S0,
                LegacyAnnualPCompetitionRules.LastRoundAction.SET_A1_FALSE_AGAIN,
                LegacyAnnualPCompetitionRules.LastRoundAction.SET_D1_4,
                LegacyAnnualPCompetitionRules.LastRoundAction.CALL_F1,
                LegacyAnnualPCompetitionRules.LastRoundAction.CALL_H1,
            ),
            result.lastRoundActions,
        )
    }

    @Test
    fun `mismatched last f0 size keeps only first a1 false and still rebuilds W`() {
        val calls = mutableListOf<Int>()
        val result =
            LegacyAnnualPCompetitionRules.apply(
                hasLegacyGroup29 = true,
                legacyY1 = true,
                rounds = listOf(round(1, "a"), round(1, "b"), round(1, "c"), round(3, "last")),
                groupS0Pool = listOf("r1", "r2"),
                legacyF0 = { argument ->
                    calls += argument
                    if (argument == 3) listOf("r1", "r2") else emptyList()
                },
            )

        assertEquals(listOf(3, 125), calls)
        assertEquals(listOf(LegacyAnnualPCompetitionRules.LastRoundAction.SET_A1_FALSE), result.lastRoundActions)
        assertEquals(listOf("last"), result.lastRoundParticipants)
        assertEquals(listOf("r1", "r2"), result.groupS0Pool)
        assertEquals(listOf("a", "b", "c"), result.rebuiltW)
    }

    @Test
    fun `null fourth round skips its f0 call but still rebuilds from first three`() {
        val calls = mutableListOf<Int>()
        val result =
            LegacyAnnualPCompetitionRules.apply(
                hasLegacyGroup29 = true,
                legacyY1 = true,
                rounds = listOf(round(1, "a"), round(1, "b"), round(1, "c"), null),
                groupS0Pool = emptyList(),
                legacyF0 = { argument -> calls += argument; emptyList() },
            )

        assertEquals(listOf(125), calls)
        assertTrue(result.lastRoundActions.isEmpty())
        assertNull(result.lastRoundParticipants)
        assertEquals(listOf("a", "b", "c"), result.rebuiltW)
    }

    private fun round(
        legacyS0: Int,
        vararg participants: String,
    ) = LegacyAnnualPCompetitionRules.Round(legacyS0 = legacyS0, participants = participants.toList())
}

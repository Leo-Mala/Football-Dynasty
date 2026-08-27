package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchGoalMaterializationRulesTest {
    private val N = LegacyMatchGoalEventRules.GoalSubtype.NORMAL
    private val A = LegacyMatchGoalEventRules.GoalSubtype.AGAINST
    private val P = LegacyMatchGoalEventRules.GoalSubtype.PENALTY
    private val F = LegacyMatchGoalEventRules.GoalSubtype.FOUL
    private val C = LegacyMatchGoalEventRules.GoalSubtype.CORNER

    @Test
    fun `normal goal keeps primary adds distinct secondary and mutates primary twice`() {
        val primary = player("primary", 1)
        val secondary = player("secondary", 1)

        val result = resolve(N, primary, secondary = { secondary })

        assertSame(primary, result.eventPrimary)
        assertSame(secondary, result.eventSecondary)
        assertEquals(
            listOf(
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
                mutation(secondary, LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_L),
                mutation(secondary, LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_COMPETITION_SIDE_EFFECT),
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
            ),
            result.statMutations,
        )
        assertTrue(result.incrementInternalGoalCounter)
        assertTrue(result.incrementScoreForCurrentSide)
    }

    @Test
    fun `normal secondary equal by identity to primary is ignored`() {
        val primary = player("primary", 1)

        val result = resolve(N, primary, secondary = { primary })

        assertNull(result.eventSecondary)
        assertEquals(2, result.statMutations.size)
    }

    @Test
    fun `corner with zero l0 falls back to normal after secondary window`() {
        val primary = player("primary", 0)
        var secondaryCalls = 0

        val result = resolve(
            subtype = C,
            primary = primary,
            secondary = {
                secondaryCalls++
                player("should-not-run", 1)
            },
        )

        assertEquals(N, result.finalSubtype)
        assertEquals(0, secondaryCalls)
        assertNull(result.eventSecondary)
        assertEquals(
            listOf(
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
            ),
            result.statMutations,
        )
    }

    @Test
    fun `null initial primary is resolved before corner fallback`() {
        val fallback = player("fallback", 0)
        var fallbackCalls = 0

        val result = resolve(
            subtype = C,
            primary = null,
            fallback = {
                fallbackCalls++
                fallback
            },
        )

        assertEquals(1, fallbackCalls)
        assertEquals(N, result.finalSubtype)
        assertSame(fallback, result.eventPrimary)
    }

    @Test
    fun `corner designated author replaces event author but not primary stat target`() {
        val primary = player("primary", 2)
        val designated = player("corner", 2)

        val result = resolve(
            subtype = C,
            primary = primary,
            designatedCorner = LegacyMatchGoalMaterializationRules.Designated(designated, true),
        )

        assertEquals(C, result.finalSubtype)
        assertSame(designated, result.eventPrimary)
        assertEquals(
            listOf(
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
            ),
            result.statMutations,
        )
    }

    @Test
    fun `inactive designated corner author does not replace primary`() {
        val primary = player("primary", 2)
        val designated = player("corner", 2)

        val result = resolve(
            subtype = C,
            primary = primary,
            designatedCorner = LegacyMatchGoalMaterializationRules.Designated(designated, false),
        )

        assertSame(primary, result.eventPrimary)
    }

    @Test
    fun `penalty uses designated author sets flag and primary stat only once`() {
        val primary = player("primary", 1)
        val designated = player("penalty", 1)

        val result = resolve(
            subtype = P,
            primary = primary,
            designatedPenaltyOrFoul = LegacyMatchGoalMaterializationRules.Designated(designated, true),
        )

        assertSame(designated, result.eventPrimary)
        assertTrue(result.penaltyFlag)
        assertEquals(
            listOf(mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S)),
            result.statMutations,
        )
    }

    @Test
    fun `foul uses same designated author path but primary stat remains doubled`() {
        val primary = player("primary", 1)
        val designated = player("foul", 1)

        val result = resolve(
            subtype = F,
            primary = primary,
            designatedPenaltyOrFoul = LegacyMatchGoalMaterializationRules.Designated(designated, true),
        )

        assertSame(designated, result.eventPrimary)
        assertFalse(result.penaltyFlag)
        assertEquals(2, result.statMutations.count { it.operation == LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S })
    }

    @Test
    fun `own goal replaces displayed author but final primary stat stays on original`() {
        val primary = player("primary", 1)
        val own = player("own", 1)

        val result = resolve(A, primary, own = { own })

        assertSame(own, result.eventPrimary)
        assertEquals(
            listOf(
                mutation(own, LegacyMatchGoalMaterializationRules.StatOperation.OWN_GOAL_T),
                mutation(primary, LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S),
            ),
            result.statMutations,
        )
    }

    @Test
    fun `own goal without selected author falls back normal without running secondary selector`() {
        val primary = player("primary", 1)
        var secondaryCalls = 0

        val result = resolve(
            subtype = A,
            primary = primary,
            secondary = {
                secondaryCalls++
                player("secondary", 1)
            },
            own = { null },
        )

        assertEquals(N, result.finalSubtype)
        assertEquals(0, secondaryCalls)
        assertNull(result.eventSecondary)
        assertSame(primary, result.eventPrimary)
        assertEquals(1, result.statMutations.size)
    }

    @Test
    fun `selectors are invoked only in their matching legacy branches`() {
        val primary = player("primary", 1)
        val trace = mutableListOf<String>()

        resolve(
            subtype = N,
            primary = primary,
            fallback = { trace += "fallback"; null },
            secondary = { trace += "secondary"; null },
            own = { trace += "own"; null },
        )

        assertEquals(listOf("secondary"), trace)
    }

    private fun resolve(
        subtype: LegacyMatchGoalEventRules.GoalSubtype,
        primary: LegacyMatchGoalMaterializationRules.Player<String>?,
        fallback: () -> LegacyMatchGoalMaterializationRules.Player<String>? = { null },
        secondary: (LegacyMatchGoalMaterializationRules.Player<String>?) -> LegacyMatchGoalMaterializationRules.Player<String>? = { null },
        own: () -> LegacyMatchGoalMaterializationRules.Player<String>? = { null },
        designatedPenaltyOrFoul: LegacyMatchGoalMaterializationRules.Designated<String>? = null,
        designatedCorner: LegacyMatchGoalMaterializationRules.Designated<String>? = null,
    ) = LegacyMatchGoalMaterializationRules.resolve(
        initialSubtype = subtype,
        initialPrimary = primary,
        fallbackPrimary = fallback,
        normalSecondary = secondary,
        ownGoalAuthor = own,
        designatedPenaltyOrFoul = designatedPenaltyOrFoul,
        designatedCorner = designatedCorner,
    )

    private fun player(value: String, l0: Int) = LegacyMatchGoalMaterializationRules.Player(value, l0)

    private fun mutation(
        player: LegacyMatchGoalMaterializationRules.Player<String>,
        operation: LegacyMatchGoalMaterializationRules.StatOperation,
    ) = LegacyMatchGoalMaterializationRules.StatMutation(player, operation)
}

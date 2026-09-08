package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchN2RuntimeIntegrationTest {
    @Test
    fun `legacy type three event mutates exact n2 M and N counters`() {
        val fixture = fixture()

        LegacyMatchTransientRuntime.applyEvent(
            state = fixture.state,
            legacyType = 3,
            legacySubtype = -1,
            eventClub = fixture.home,
            originalPrimary = fixture.primary,
            legacyPeriod = 1,
            legacyMinute = 20,
            random = NoRandomSource,
        )

        assertEquals(1, fixture.primary.legacyStatM)
        assertEquals(1, fixture.primary.legacyStatN)
        assertEquals(
            LegacyMatchN2CounterRules.GetterSnapshot(
                legacyA = 0,
                legacyB = 1,
                legacyC = 1,
                legacyD = 0,
                legacyE = 0,
                legacyF = 0,
                legacyG = 0,
                legacyH = 0,
                legacyI = 0,
                legacyJ = 0,
                legacyK = 0,
            ),
            LegacyMatchN2CounterRules.snapshot(fixture.primary.legacyN2),
        )
    }

    @Test
    fun `normal goal applies two legacy S mutations and one L without leaking competition side effect into n2`() {
        val fixture = fixture()
        val primary = LegacyMatchGoalMaterializationRules.Player(fixture.primary, fixture.primary.legacyL0)
        val secondary = LegacyMatchGoalMaterializationRules.Player(fixture.secondary, fixture.secondary.legacyL0)
        val goal = LegacyMatchGoalMaterializationRules.Result(
            finalSubtype = LegacyMatchGoalEventRules.GoalSubtype.NORMAL,
            eventPrimary = primary,
            eventSecondary = secondary,
            penaltyFlag = false,
            statMutations = listOf(
                LegacyMatchGoalMaterializationRules.StatMutation(
                    primary,
                    LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S,
                ),
                LegacyMatchGoalMaterializationRules.StatMutation(
                    secondary,
                    LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_L,
                ),
                LegacyMatchGoalMaterializationRules.StatMutation(
                    secondary,
                    LegacyMatchGoalMaterializationRules.StatOperation.SECONDARY_COMPETITION_SIDE_EFFECT,
                ),
                LegacyMatchGoalMaterializationRules.StatMutation(
                    primary,
                    LegacyMatchGoalMaterializationRules.StatOperation.PRIMARY_S,
                ),
            ),
        )

        LegacyMatchR3RuntimeRules.apply(
            state = fixture.state,
            currentSide = 0,
            plan = plan(LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT),
            r3State = emptyR3State(),
            goal = goal,
            legacyPeriod = 1,
            legacyMinute = 22,
        )

        assertEquals(
            LegacyMatchN2CounterRules.GetterSnapshot(
                legacyA = 0,
                legacyB = 0,
                legacyC = 0,
                legacyD = 2,
                legacyE = 0,
                legacyF = 0,
                legacyG = 0,
                legacyH = 2,
                legacyI = 0,
                legacyJ = 0,
                legacyK = 0,
            ),
            LegacyMatchN2CounterRules.snapshot(fixture.primary.legacyN2),
        )
        assertEquals(
            LegacyMatchN2CounterRules.GetterSnapshot(
                legacyA = 1,
                legacyB = 0,
                legacyC = 0,
                legacyD = 0,
                legacyE = 0,
                legacyF = 0,
                legacyG = 0,
                legacyH = 0,
                legacyI = 0,
                legacyJ = 0,
                legacyK = 0,
            ),
            LegacyMatchN2CounterRules.snapshot(fixture.secondary.legacyN2),
        )
    }

    @Test
    fun `own goal T mutates only exact n2 i getter`() {
        val fixture = fixture()
        val ownAuthor = LegacyMatchGoalMaterializationRules.Player(fixture.secondary, fixture.secondary.legacyL0)
        val goal = LegacyMatchGoalMaterializationRules.Result(
            finalSubtype = LegacyMatchGoalEventRules.GoalSubtype.AGAINST,
            eventPrimary = ownAuthor,
            eventSecondary = null,
            penaltyFlag = false,
            statMutations = listOf(
                LegacyMatchGoalMaterializationRules.StatMutation(
                    ownAuthor,
                    LegacyMatchGoalMaterializationRules.StatOperation.OWN_GOAL_T,
                ),
            ),
        )

        LegacyMatchR3RuntimeRules.apply(
            state = fixture.state,
            currentSide = 0,
            plan = plan(LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT),
            r3State = emptyR3State(),
            goal = goal,
            legacyPeriod = 2,
            legacyMinute = 8,
        )

        assertEquals(1, LegacyMatchN2CounterRules.snapshot(fixture.secondary.legacyN2).legacyI)
    }

    @Test
    fun `r3 primary P mutates transient n2 even when optional projection counter is absent`() {
        val fixture = fixture()
        val result = LegacyMatchR3RuntimeRules.apply(
            state = fixture.state,
            currentSide = 0,
            plan = plan(LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P),
            r3State = emptyR3State(primaryP = null),
            goal = null,
            legacyPeriod = 1,
            legacyMinute = 12,
            r3Primary = fixture.primary,
        )

        assertEquals(null, result.r3State.primaryLegacyR0P)
        assertEquals(1, LegacyMatchN2CounterRules.snapshot(fixture.primary.legacyN2).legacyE)
    }

    private data class Fixture(
        val state: LegacyMatchTransientRuntime.State<String, String>,
        val home: LegacyMatchTransientRuntime.Club<String, String>,
        val primary: LegacyMatchTransientRuntime.Player<String>,
        val secondary: LegacyMatchTransientRuntime.Player<String>,
    )

    private fun fixture(): Fixture {
        val primary = player("primary")
        val secondary = player("secondary")
        val home = LegacyMatchTransientRuntime.Club(
            value = "home",
            legacyClubId = 101,
            active = mutableListOf(primary),
            bench = mutableListOf(),
            substitutionsRemaining = 0,
        )
        val away = LegacyMatchTransientRuntime.Club<String, String>(
            value = "away",
            legacyClubId = 202,
            active = mutableListOf(secondary),
            bench = mutableListOf(),
            substitutionsRemaining = 0,
        )
        return Fixture(
            state = LegacyMatchTransientRuntime.State(2026, home, away),
            home = home,
            primary = primary,
            secondary = secondary,
        )
    }

    private fun player(value: String) = LegacyMatchTransientRuntime.Player(
        value = value,
        legacyG0 = 10,
        legacyL0 = 1,
        legacyF0 = 1,
        legacyR = 1,
        age = 25,
        energy = 50,
        skill = 80,
    )

    private fun emptyR3State(primaryP: Int? = 0) = LegacyMatchR3MutationApplicationRules.State(
        legacyIBySide = listOf(0, 0),
        legacyYBySide = listOf(0, 0),
        legacyZBySide = listOf(0, 0),
        primaryLegacyR0P = primaryP,
    )

    private fun plan(vararg mutations: LegacyMatchR3EventRoutingRules.Mutation) =
        LegacyMatchR3EventRoutingRules.Result(
            selectedIndex = -1,
            weightTable = LegacyMatchR3EventRoutingRules.WeightTable.B0,
            multipliers = listOf(1.0, 1.0, 1.0),
            storedLegacyGAfter = 0.0,
            sIncrementTiming = LegacyMatchR3EventRoutingRules.SIncrementTiming.AFTER_WEIGHTED_DRAW,
            mutations = mutations.toList(),
        )

    private object NoRandomSource : RandomSource {
        override var draws: Long = 0
        override fun nextInt(bound: Int): Int = error("unexpected RNG draw")
        override fun nextBoolean(): Boolean = error("unexpected RNG draw")
        override fun nextDouble(): Double = error("unexpected RNG draw")
    }
}

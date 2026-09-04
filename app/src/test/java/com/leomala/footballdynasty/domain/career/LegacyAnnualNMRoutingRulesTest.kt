package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.career.LegacyAnnualNMRoutingRules.Action
import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualNMRoutingRulesTest {
    @Test
    fun `maintenance queues wrap the unconditional random gate in exact order`() {
        val random = FixedIntRandomSource(51)

        val actions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = true,
                hasG0Entries = true,
                legacyV0 = false,
                annualRouteE1 = false,
            ),
            random = random,
        )

        assertEquals(
            listOf(Action.TRY_D4, Action.CALL_G4, Action.TRY_E4, Action.CALL_J2_ONE),
            actions,
        )
        assertEquals(1L, random.draws)
    }

    @Test
    fun `g4 threshold is strictly greater than fifty and draw remains unconditional`() {
        val atThreshold = FixedIntRandomSource(50)
        val belowActions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = false,
                annualRouteE1 = false,
            ),
            random = atThreshold,
        )
        assertEquals(listOf(Action.CALL_J2_ONE), belowActions)
        assertEquals(1L, atThreshold.draws)

        val aboveThreshold = FixedIntRandomSource(51)
        val aboveActions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = false,
                annualRouteE1 = false,
            ),
            random = aboveThreshold,
        )
        assertEquals(listOf(Action.CALL_G4, Action.CALL_J2_ONE), aboveActions)
        assertEquals(1L, aboveThreshold.draws)
    }

    @Test
    fun `original p0 zero sets F2 only after j2`() {
        val random = FixedIntRandomSource(0)

        val actions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 0,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = false,
                annualRouteE1 = false,
            ),
            random = random,
        )

        assertEquals(listOf(Action.CALL_J2_ONE, Action.SET_F2_TRUE), actions)
    }

    @Test
    fun `V0 true and E1 true calls F then starts end year activity`() {
        val random = FixedIntRandomSource(0)

        val actions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = true,
                annualRouteE1 = true,
            ),
            random = random,
        )

        assertEquals(
            listOf(Action.CALL_J2_ONE, Action.CALL_F, Action.START_ACTIVITY_FIM_ANO),
            actions,
        )
    }

    @Test
    fun `V0 true and E1 false routes only to n i`() {
        val random = FixedIntRandomSource(0)

        val actions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = true,
                annualRouteE1 = false,
            ),
            random = random,
        )

        assertEquals(listOf(Action.CALL_J2_ONE, Action.CALL_N_I), actions)
    }

    @Test
    fun `V0 false and E1 true calls F without starting activity`() {
        val random = FixedIntRandomSource(0)

        val actions = LegacyAnnualNMRoutingRules.plan(
            input = LegacyAnnualNMRoutingRules.Input(
                originalP0 = 1,
                hasO0Entries = false,
                hasG0Entries = false,
                legacyV0 = false,
                annualRouteE1 = true,
            ),
            random = random,
        )

        assertEquals(listOf(Action.CALL_J2_ONE, Action.CALL_F), actions)
    }

    private class FixedIntRandomSource(
        vararg values: Int,
    ) : RandomSource {
        private val iterator = values.iterator()

        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            val value = iterator.nextInt()
            require(value in 0 until bound) { "value=$value bound=$bound" }
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}

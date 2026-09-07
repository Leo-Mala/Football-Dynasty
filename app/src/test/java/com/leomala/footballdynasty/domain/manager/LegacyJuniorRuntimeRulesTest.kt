package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyJuniorRuntimeRulesTest {
    @Test
    fun `trial precheck preserves cash-before-cap ordering and raw finance category`() {
        assertEquals(
            LegacyJuniorRuntimeRules.TrialAvailability.INSUFFICIENT_CASH,
            LegacyJuniorRuntimeRules.trialAvailability(cash = 99, cost = 100, juniorCount = 18),
        )
        assertEquals(
            LegacyJuniorRuntimeRules.TrialAvailability.READY,
            LegacyJuniorRuntimeRules.trialAvailability(cash = 100, cost = 100, juniorCount = 17),
        )
        assertEquals(
            LegacyJuniorRuntimeRules.TrialAvailability.JUNIOR_LIMIT_REACHED,
            LegacyJuniorRuntimeRules.trialAvailability(cash = 100, cost = 100, juniorCount = 18),
        )
        assertEquals(9, LegacyJuniorRuntimeRules.TRIAL_EXPENSE_RAW_CODE)
    }

    @Test
    fun `trial interleaves each successful p-d generation before the next gate draw`() {
        val random = QueueRandomSource(0, 5, 1, 0, 6, 2, 0, 4, 0, 3)
        val generated = LegacyJuniorRuntimeRules.executeTrial(random, currentJuniorCount = 0) { position, rng ->
            "$position:${rng.nextInt(7)}"
        }

        assertEquals(listOf("0:5", "2:6", "3:4", "4:3"), generated)
        assertEquals(listOf(3, 7, 3, 3, 7, 3, 3, 7, 3, 7), random.bounds)
        assertEquals(10L, random.draws)
    }

    @Test
    fun `trial still consumes all six gates after youth cap is reached`() {
        val random = QueueRandomSource(0, 2, 0, 0, 0, 0, 0)
        val generated = LegacyJuniorRuntimeRules.executeTrial(random, currentJuniorCount = 17) { position, rng ->
            "$position:${rng.nextInt(7)}"
        }

        assertEquals(listOf("0:2"), generated)
        assertEquals(listOf(3, 7, 3, 3, 3, 3, 3), random.bounds)
        assertEquals(7L, random.draws)
    }

    @Test
    fun `manual promotion is blocked exactly when senior count reaches thirty`() {
        assertTrue(LegacyJuniorRuntimeRules.canPromoteManually(29))
        assertFalse(LegacyJuniorRuntimeRules.canPromoteManually(30))
        assertFalse(LegacyJuniorRuntimeRules.canPromoteManually(31))
    }

    @Test
    fun `development uses exact age and potential increments and carries remainder above one`() {
        val before = LegacyJuniorRuntimeRules.DevelopmentState(
            age = 16,
            legacyN = 3,
            legacyO = 72,
            remainder = 0.50,
        )
        val after = LegacyJuniorRuntimeRules.progressDevelopment(before)

        assertEquals(73, after.legacyO)
        assertEquals(0.03, after.remainder, 1e-12)
        assertEquals(16, after.age)
        assertEquals(3, after.legacyN)
    }

    @Test
    fun `development leaves over-twenty junior untouched`() {
        val before = LegacyJuniorRuntimeRules.DevelopmentState(21, 10, 99, 0.75)
        assertSame(before, LegacyJuniorRuntimeRules.progressDevelopment(before))
    }

    @Test
    fun `annual rule increments age then promotes and stages replacement under exact thresholds`() {
        val decision = LegacyJuniorRuntimeRules.annualDecision(
            age = 19,
            legacyN = 4,
            legacyE = 0,
            context = LegacyJuniorRuntimeRules.AnnualContext(
                clubP0 = 1,
                seniorPositionCounts = listOf(2, 5, 5, 8, 6),
                seniorCount = 29,
                clubB0 = 9,
                clubQ0 = false,
            ),
        )
        assertEquals(20, decision.ageAfterIncrement)
        assertEquals(
            LegacyJuniorRuntimeRules.AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT,
            decision.action,
        )
    }

    @Test
    fun `annual qualified junior does nothing when senior cap is already thirty`() {
        val decision = LegacyJuniorRuntimeRules.annualDecision(
            age = 19,
            legacyN = 4,
            legacyE = 0,
            context = LegacyJuniorRuntimeRules.AnnualContext(
                clubP0 = 1,
                seniorPositionCounts = listOf(2, 5, 5, 8, 6),
                seniorCount = 30,
                clubB0 = 0,
                clubQ0 = false,
            ),
        )
        assertEquals(LegacyJuniorRuntimeRules.AnnualAction.NONE, decision.action)
    }

    @Test
    fun `annual nonqualified junior refreshes only when club q0 is false`() {
        val refresh = LegacyJuniorRuntimeRules.annualDecision(
            age = 19,
            legacyN = 5,
            legacyE = 4,
            context = LegacyJuniorRuntimeRules.AnnualContext(
                clubP0 = 5,
                seniorPositionCounts = listOf(3, 5, 5, 8, 6),
                seniorCount = 20,
                clubB0 = 10,
                clubQ0 = false,
            ),
        )
        val keep = LegacyJuniorRuntimeRules.annualDecision(
            age = 19,
            legacyN = 5,
            legacyE = 4,
            context = LegacyJuniorRuntimeRules.AnnualContext(
                clubP0 = 5,
                seniorPositionCounts = listOf(3, 5, 5, 8, 6),
                seniorCount = 20,
                clubB0 = 10,
                clubQ0 = true,
            ),
        )
        assertEquals(LegacyJuniorRuntimeRules.AnnualAction.REFRESH_DRAFT, refresh.action)
        assertEquals(LegacyJuniorRuntimeRules.AnnualAction.NONE, keep.action)
    }

    @Test
    fun `annual rule only increments age before twenty`() {
        val decision = LegacyJuniorRuntimeRules.annualDecision(
            age = 18,
            legacyN = 10,
            legacyE = 0,
            context = LegacyJuniorRuntimeRules.AnnualContext(
                clubP0 = 0,
                seniorPositionCounts = listOf(0, 0, 0, 0, 0),
                seniorCount = 0,
                clubB0 = 0,
                clubQ0 = false,
            ),
        )
        assertEquals(19, decision.ageAfterIncrement)
        assertEquals(LegacyJuniorRuntimeRules.AnnualAction.NONE, decision.action)
    }

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val values = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(values.isNotEmpty()) { "No queued value for bound=$bound" }
            val value = values.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}

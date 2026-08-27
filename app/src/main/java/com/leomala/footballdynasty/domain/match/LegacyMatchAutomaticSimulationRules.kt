package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure orchestration of the proven automatic `best.s.Q0()` simulation boundary through the end of
 * the second-half minute loop.
 *
 * This rule intentionally stops before the post-simulation `P0()/o()` and club-flag side effects.
 * Per legacy SMALI, both added-time values are drawn before either half; every minute executes the
 * legacy `best.s.k(...)` callback before the `components.r3.K()` callback; a non-null K event is
 * stamped with that zero-based minute index and period before being appended to the returned list;
 * and `j(2, 0)` is invoked between the two half loops.
 */
object LegacyMatchAutomaticSimulationRules {
    data class StampedEvent<T>(
        val event: T,
        val legacyMinute: Int,
        val legacyPeriod: Int,
    )

    data class Result<T>(
        val firstHalfAddedMinutes: Int,
        val secondHalfAddedMinutes: Int,
        val events: List<StampedEvent<T>>,
    )

    fun <T> run(
        random: RandomSource,
        runMinuteRule: (half: Int, minute: Int) -> Unit,
        advanceR3: (half: Int, minute: Int) -> T?,
        halftimeTransition: (half: Int, minute: Int) -> Unit,
    ): Result<T> {
        val firstHalfAdded = LegacyMatchScheduleRules.drawAutomaticFirstHalfAddedMinutes(random)
        val secondHalfAdded = LegacyMatchScheduleRules.drawAutomaticSecondHalfAddedMinutes(random)
        val events = mutableListOf<StampedEvent<T>>()

        simulateHalf(
            half = 1,
            addedMinutes = firstHalfAdded,
            runMinuteRule = runMinuteRule,
            advanceR3 = advanceR3,
            events = events,
        )

        halftimeTransition(2, 0)

        simulateHalf(
            half = 2,
            addedMinutes = secondHalfAdded,
            runMinuteRule = runMinuteRule,
            advanceR3 = advanceR3,
            events = events,
        )

        return Result(
            firstHalfAddedMinutes = firstHalfAdded,
            secondHalfAddedMinutes = secondHalfAdded,
            events = events.toList(),
        )
    }

    private fun <T> simulateHalf(
        half: Int,
        addedMinutes: Int,
        runMinuteRule: (half: Int, minute: Int) -> Unit,
        advanceR3: (half: Int, minute: Int) -> T?,
        events: MutableList<StampedEvent<T>>,
    ) {
        for (minute in LegacyMatchScheduleRules.automaticHalfMinutes(addedMinutes)) {
            runMinuteRule(half, minute)
            val event = advanceR3(half, minute)
            if (event != null) {
                events += StampedEvent(
                    event = event,
                    legacyMinute = minute,
                    legacyPeriod = half,
                )
            }
        }
    }
}

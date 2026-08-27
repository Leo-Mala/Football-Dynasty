package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.model.Match
import com.leomala.footballdynasty.foundation.random.RandomSource
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource
import com.leomala.footballdynasty.foundation.random.StatefulRandomSnapshot

/**
 * Persistence-independent Phase 9 bridge between the career calendar and the certified Phase 8
 * match runtime.
 *
 * The bridge deliberately does not invent competition rules. It consumes already-scheduled match
 * events, uses the characterized legacy next-playable-day selection, restores the exact career RNG
 * state, and returns the updated schedule/state for an atomic persistence boundary to commit.
 */
data class ScheduledCareerMatch(
    val matchId: String,
    val dayIndex: Int,
    val eventTypeCode: Int,
    val homeClubId: String,
    val awayClubId: String,
    val processed: Boolean = false,
)

data class CareerMatchRuntimeResult(
    val state: CareerState,
    val schedule: List<ScheduledCareerMatch>,
    val match: Match,
    val nextPlayableDayIndex: Int?,
)

object CareerMatchRuntimeBridge {
    fun run(
        state: CareerState,
        schedule: List<ScheduledCareerMatch>,
        matchId: String,
        simulate: (scheduled: ScheduledCareerMatch, random: RandomSource) -> Match,
    ): CareerMatchRuntimeResult {
        CareerIntegrityValidator.validate(state)
        validateSchedule(state, schedule)

        val target = schedule.singleOrNull { it.matchId == matchId }
            ?: throw IllegalArgumentException("Scheduled match $matchId must resolve exactly once")
        require(!target.processed) { "Scheduled match $matchId is already processed" }

        val nextBefore = LegacyCalendarRules.selectNextPlayableDay(
            state = state,
            scheduledDays = toCalendarDays(schedule),
        )
        require(nextBefore.found) { "Career has no playable scheduled match" }
        require(target.dayIndex == nextBefore.selectedIndex) {
            "Scheduled match $matchId is on day ${target.dayIndex}, before next playable day is ${nextBefore.selectedIndex}"
        }

        val executionState = nextBefore.state
        val random = StatefulJavaRandomSource.restore(
            StatefulRandomSnapshot(
                initialSeed = executionState.random.initialSeed,
                internalState = executionState.random.internalState,
                draws = executionState.random.draws,
            )
        )

        val modernMatch = simulate(target, random)
        validateResult(target, modernMatch)

        val randomSnapshot = random.snapshot()
        val stateAfterMatch = executionState.copy(
            random = CareerRandomState(
                initialSeed = randomSnapshot.initialSeed,
                internalState = randomSnapshot.internalState,
                draws = randomSnapshot.draws,
            )
        )
        val updatedSchedule = schedule.map { event ->
            if (event.matchId == target.matchId) event.copy(processed = true) else event
        }

        val nextAfter = LegacyCalendarRules.selectNextPlayableDay(
            state = stateAfterMatch,
            scheduledDays = toCalendarDays(updatedSchedule),
        )
        val finalState = nextAfter.state
        CareerIntegrityValidator.validate(finalState)

        return CareerMatchRuntimeResult(
            state = finalState,
            schedule = updatedSchedule,
            match = modernMatch,
            nextPlayableDayIndex = nextAfter.selectedIndex,
        )
    }

    private fun validateSchedule(
        state: CareerState,
        schedule: List<ScheduledCareerMatch>,
    ) {
        require(schedule.map { it.matchId }.distinct().size == schedule.size) {
            "Scheduled match ids must be unique"
        }
        schedule.forEach { event ->
            require(event.matchId.isNotBlank()) { "Scheduled match id must not be blank" }
            require(event.dayIndex in 0 until state.calendar.dayCount) {
                "Scheduled match ${event.matchId} day ${event.dayIndex} is outside career calendar"
            }
            require(event.eventTypeCode > 0) {
                "Scheduled match ${event.matchId} must use a positive legacy event type code"
            }
            require(event.homeClubId.isNotBlank() && event.awayClubId.isNotBlank()) {
                "Scheduled match ${event.matchId} must resolve both clubs"
            }
            require(event.homeClubId != event.awayClubId) {
                "Scheduled match ${event.matchId} cannot use the same club twice"
            }
        }
    }

    private fun validateResult(
        scheduled: ScheduledCareerMatch,
        result: Match,
    ) {
        require(result.id == scheduled.matchId) {
            "Runtime returned match ${result.id} for scheduled match ${scheduled.matchId}"
        }
        require(result.homeClubId == scheduled.homeClubId) {
            "Runtime home club ${result.homeClubId} diverged from ${scheduled.homeClubId}"
        }
        require(result.awayClubId == scheduled.awayClubId) {
            "Runtime away club ${result.awayClubId} diverged from ${scheduled.awayClubId}"
        }
        require(result.homeGoals != null && result.homeGoals >= 0) {
            "Runtime must return a resolved non-negative home score"
        }
        require(result.awayGoals != null && result.awayGoals >= 0) {
            "Runtime must return a resolved non-negative away score"
        }
    }

    private fun toCalendarDays(schedule: List<ScheduledCareerMatch>): List<ScheduledCalendarDay> =
        schedule
            .groupBy { it.dayIndex }
            .toSortedMap()
            .map { (dayIndex, events) ->
                ScheduledCalendarDay(
                    dayIndex = dayIndex,
                    eventTypeCode = events.maxOf { it.eventTypeCode },
                    matchCount = events.size,
                    processed = events.all { it.processed },
                )
            }
}

package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.error.SeasonBoundaryRequiredException

sealed interface CareerCommand {
    data object AdvanceOneDay : CareerCommand
    data object TransitionSeason : CareerCommand
    data class MoveToNextScheduledEvent(
        val days: List<ScheduledCalendarDay>,
    ) : CareerCommand
}

data class CareerTransition(
    val state: CareerState,
    val checkpoint: CareerCheckpoint,
    val legacyReturnValue: Int? = null,
    val eventFound: Boolean? = null,
)

/** Pure Kotlin transition engine. It has no Android, Room, legacy-object or UI dependency. */
class CareerSimulationEngine {
    fun apply(state: CareerState, command: CareerCommand): CareerTransition {
        CareerIntegrityValidator.validate(state)
        val before = CareerFingerprint.of(state)
        val next: CareerState
        var legacyReturnValue: Int? = null
        var eventFound: Boolean? = null

        when (command) {
            CareerCommand.AdvanceOneDay -> {
                if (state.calendar.currentDayIndex >= state.calendar.dayCount - 1) {
                    throw SeasonBoundaryRequiredException(
                        "Legacy season transition must be explicit at end of year"
                    )
                }
                next = state.copy(
                    calendar = state.calendar.copy(
                        currentDayIndex = state.calendar.currentDayIndex + 1,
                    )
                )
            }
            CareerCommand.TransitionSeason -> {
                next = LegacyCalendarRules.transitionSeason(state)
            }
            is CareerCommand.MoveToNextScheduledEvent -> {
                val selection = LegacyCalendarRules.selectNextPlayableDay(state, command.days)
                next = selection.state
                legacyReturnValue = selection.legacyReturnValue
                eventFound = selection.found
            }
        }

        CareerIntegrityValidator.validate(next)
        val after = CareerFingerprint.of(next)
        return CareerTransition(
            state = next,
            checkpoint = CareerCheckpoint(
                event = command::class.simpleName ?: "CareerCommand",
                beforeFingerprint = before,
                afterFingerprint = after,
            ),
            legacyReturnValue = legacyReturnValue,
            eventFound = eventFound,
        )
    }
}

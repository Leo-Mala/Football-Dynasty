package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.error.CareerIntegrityException
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource

object CareerIntegrityValidator {
    fun validate(
        state: CareerState,
        knownClubIds: Set<String>? = null,
    ) {
        if (state.id.isBlank()) throw CareerIntegrityException("Career id must not be blank")
        if (state.stateVersion != CAREER_STATE_VERSION) {
            throw CareerIntegrityException("Unsupported career state version ${state.stateVersion}")
        }
        if (state.season.number < 1) throw CareerIntegrityException("Season number must be >= 1")
        val expectedYear = LegacyCalendarRules.seasonYear(state.season.number)
        if (state.season.year != expectedYear) {
            throw CareerIntegrityException("Season year ${state.season.year} != expected $expectedYear")
        }
        if (state.calendar.year != expectedYear) {
            throw CareerIntegrityException("Calendar year ${state.calendar.year} != season year $expectedYear")
        }
        val expectedDayCount = LegacyCalendarRules.datesForYear(expectedYear).size
        if (state.calendar.dayCount != expectedDayCount) {
            throw CareerIntegrityException("Calendar dayCount ${state.calendar.dayCount} != expected $expectedDayCount")
        }
        val expectedStart = LegacyCalendarRules.firstSundayOfJanuaryIndex(expectedYear)
        if (state.calendar.startDayIndex != expectedStart) {
            throw CareerIntegrityException("Calendar start ${state.calendar.startDayIndex} != first Sunday $expectedStart")
        }
        if (state.calendar.currentDayIndex !in 0 until state.calendar.dayCount) {
            throw CareerIntegrityException("Current day index outside calendar")
        }
        if (state.transitionCount < 0L) throw CareerIntegrityException("Transition count must not be negative")
        if (state.random.draws < 0L) throw CareerIntegrityException("RNG draw count must not be negative")
        if (state.random.internalState !in 0L..StatefulJavaRandomSource.MASK) {
            throw CareerIntegrityException("RNG internal state outside Java Random 48-bit range")
        }
        val clubId = state.managedClub?.clubId
        if (clubId != null) {
            if (clubId.isBlank()) throw CareerIntegrityException("Managed club id must not be blank")
            if (knownClubIds != null && clubId !in knownClubIds) {
                throw CareerIntegrityException("Managed club id does not resolve")
            }
        }
    }
}

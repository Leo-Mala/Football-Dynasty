package com.leomala.footballdynasty.domain.career

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Rules copied from proven legacy behavior in a.b/a.h, not from current football rules.
 * Legacy java.util.Calendar stores January as month 0; GameDate intentionally exposes 1..12.
 */
object LegacyCalendarRules {
    const val BASE_YEAR: Int = 2026

    fun seasonYear(seasonNumber: Int): Int {
        require(seasonNumber >= 1) { "Season number must be >= 1" }
        return BASE_YEAR + seasonNumber - 1
    }

    fun datesForYear(year: Int): List<GameDate> {
        require(year >= 1) { "Year must be positive" }
        val result = ArrayList<GameDate>(366)
        var cursor = LocalDate.of(year, 1, 1)
        while (cursor.year == year) {
            result += GameDate(cursor.year, cursor.monthValue, cursor.dayOfMonth)
            cursor = cursor.plusDays(1)
        }
        return result
    }

    fun firstSundayOfJanuaryIndex(year: Int): Int {
        var cursor = LocalDate.of(year, 1, 1)
        var index = 0
        while (cursor.monthValue == 1) {
            if (cursor.dayOfWeek == DayOfWeek.SUNDAY) return index
            cursor = cursor.plusDays(1)
            index++
        }
        error("January without a Sunday is impossible")
    }

    fun calendarForSeason(seasonNumber: Int): CareerCalendarState {
        val year = seasonYear(seasonNumber)
        return CareerCalendarState(
            year = year,
            currentDayIndex = firstSundayOfJanuaryIndex(year),
            startDayIndex = firstSundayOfJanuaryIndex(year),
            dayCount = datesForYear(year).size,
        )
    }

    fun dateAt(calendar: CareerCalendarState): GameDate {
        require(calendar.currentDayIndex in 0 until calendar.dayCount) {
            "Current day index ${calendar.currentDayIndex} outside calendar"
        }
        return datesForYear(calendar.year)[calendar.currentDayIndex]
    }

    /** Exact selection predicate/order of legacy a.b.dJ(). */
    fun selectNextPlayableDay(
        state: CareerState,
        scheduledDays: List<ScheduledCalendarDay>,
    ): NextEventSelection {
        val byIndex = scheduledDays.associateBy { it.dayIndex }
        for (index in state.calendar.currentDayIndex until state.calendar.dayCount) {
            val day = byIndex[index] ?: continue
            if (!day.processed && day.eventTypeCode > 0 && day.matchCount > 0) {
                return NextEventSelection(
                    state = state.copy(
                        calendar = state.calendar.copy(currentDayIndex = index),
                    ),
                    found = true,
                    selectedIndex = index,
                    legacyReturnValue = index,
                )
            }
        }
        return NextEventSelection(
            state = state,
            found = false,
            selectedIndex = null,
            legacyReturnValue = 0,
        )
    }

    /** Proven minimal part of legacy a.b.dx(): season++, rebuild year/calendar. */
    fun transitionSeason(state: CareerState): CareerState {
        val nextSeasonNumber = state.season.number + 1
        val nextYear = seasonYear(nextSeasonNumber)
        return state.copy(
            season = SeasonState(nextSeasonNumber, nextYear),
            calendar = calendarForSeason(nextSeasonNumber),
            transitionCount = state.transitionCount + 1,
        )
    }
}

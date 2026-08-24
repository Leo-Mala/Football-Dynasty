package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Characterization values revalidated against the official Brasfoot 2026/27 baseline.
 * `best.b.l()` rebuilds the standard calendar from `2026 + (season - 1)`.
 */
class LegacyCareerCalendarCharacterizationTest {
    @Test
    fun `legacy initial career calendar is 2026 and starts on first Sunday`() {
        val state = CareerStateFactory.create("characterization", seed = 7L)

        assertEquals(2026, state.season.year)
        assertEquals(1, state.season.number)
        assertEquals(365, state.calendar.dayCount)
        assertEquals(3, state.calendar.startDayIndex)
        assertEquals(3, state.calendar.currentDayIndex)
        assertEquals(GameDate(2026, 1, 4), LegacyCalendarRules.dateAt(state.calendar))
        assertEquals(GameDate(2026, 1, 1), LegacyCalendarRules.datesForYear(2026).first())
        assertEquals(GameDate(2026, 12, 31), LegacyCalendarRules.datesForYear(2026).last())
    }

    @Test
    fun `legacy season transition increments season and rebuilds next year calendar`() {
        val state = CareerStateFactory.create("season-transition", seed = 9L)
        val next = LegacyCalendarRules.transitionSeason(state)

        assertEquals(2, next.season.number)
        assertEquals(2027, next.season.year)
        assertEquals(2027, next.calendar.year)
        assertEquals(2, next.calendar.startDayIndex)
        assertEquals(GameDate(2027, 1, 3), LegacyCalendarRules.dateAt(next.calendar))
        assertEquals(1L, next.transitionCount)
    }

    @Test
    fun `standard Brasfoot 2026 calendar formula remains deterministic across leap year`() {
        val season1 = CareerStateFactory.create("leap-year", seed = 11L)
        val season2 = LegacyCalendarRules.transitionSeason(season1)
        val season3 = LegacyCalendarRules.transitionSeason(season2)

        assertEquals(2028, season3.season.year)
        assertEquals(2028, season3.calendar.year)
        assertEquals(366, season3.calendar.dayCount)
        assertEquals(GameDate(2028, 1, 2), LegacyCalendarRules.dateAt(season3.calendar))
        assertEquals(GameDate(2028, 12, 31), LegacyCalendarRules.datesForYear(2028).last())
        assertEquals(2L, season3.transitionCount)
    }

    @Test
    fun `legacy dJ predicate picks first unprocessed coded day with matches`() {
        val state = CareerStateFactory.create("next-event", seed = 1L)
        val schedule = listOf(
            ScheduledCalendarDay(3, eventTypeCode = 0, matchCount = 1, processed = false),
            ScheduledCalendarDay(4, eventTypeCode = 1, matchCount = 0, processed = false),
            ScheduledCalendarDay(5, eventTypeCode = 1, matchCount = 1, processed = true),
            ScheduledCalendarDay(8, eventTypeCode = 2, matchCount = 1, processed = false),
            ScheduledCalendarDay(9, eventTypeCode = 3, matchCount = 2, processed = false),
        )

        val selected = LegacyCalendarRules.selectNextPlayableDay(state, schedule)
        assertTrue(selected.found)
        assertEquals(8, selected.selectedIndex)
        assertEquals(8, selected.legacyReturnValue)
        assertEquals(8, selected.state.calendar.currentDayIndex)
    }

    @Test
    fun `legacy dJ returns zero without mutating when no event is found`() {
        val state = CareerStateFactory.create("no-event", seed = 1L)
        val selected = LegacyCalendarRules.selectNextPlayableDay(
            state,
            listOf(ScheduledCalendarDay(10, 1, 1, processed = true)),
        )

        assertFalse(selected.found)
        assertEquals(null, selected.selectedIndex)
        assertEquals(0, selected.legacyReturnValue)
        assertEquals(state, selected.state)
    }
}

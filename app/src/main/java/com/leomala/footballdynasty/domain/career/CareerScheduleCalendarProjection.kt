package com.leomala.footballdynasty.domain.career

/**
 * Canonical projection from persisted match schedule rows to the legacy best.b.dJ()/dK()
 * day-level view. The aggregation is the same one already certified in CareerMatchRuntimeBridge.
 */
object CareerScheduleCalendarProjection {
    fun calendarDays(schedule: List<ScheduledCareerMatch>): List<ScheduledCalendarDay> =
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

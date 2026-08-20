package com.leomala.footballdynasty.domain.career

const val CAREER_STATE_VERSION: Int = 1

data class GameDate(
    val year: Int,
    val month: Int,
    val day: Int,
)

data class SeasonState(
    val number: Int,
    val year: Int,
)

data class ManagedClubState(
    val clubId: String,
)

data class CareerCalendarState(
    val year: Int,
    val currentDayIndex: Int,
    val startDayIndex: Int,
    val dayCount: Int,
)

data class CareerRandomState(
    val initialSeed: Long,
    val internalState: Long,
    val draws: Long,
)

data class CareerState(
    val id: String,
    val stateVersion: Int = CAREER_STATE_VERSION,
    val season: SeasonState,
    val calendar: CareerCalendarState,
    val managedClub: ManagedClubState?,
    val random: CareerRandomState,
    val transitionCount: Long = 0L,
)

/** Minimal scheduling projection matching the fields read by legacy a.b.dJ()/dK(). */
data class ScheduledCalendarDay(
    val dayIndex: Int,
    val eventTypeCode: Int,
    val matchCount: Int,
    val processed: Boolean,
)

data class NextEventSelection(
    val state: CareerState,
    val found: Boolean,
    val selectedIndex: Int?,
    /** Exact legacy dJ() return contract: selected index, or 0 when none is found. */
    val legacyReturnValue: Int,
)

data class CareerCheckpoint(
    val event: String,
    val beforeFingerprint: String,
    val afterFingerprint: String,
)

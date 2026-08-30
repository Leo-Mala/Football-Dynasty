package com.leomala.footballdynasty.domain.manager

import java.util.Calendar

/**
 * Pure reconstruction of the serialized `components.o2` record created by
 * `best.o.q(best.c0)` immediately before the legacy loan movement calls
 * `best.o.T1(...)`.
 *
 * The recovered constructor stores the player and the source club, appends the
 * record to the global ArrayList in insertion order, seeds a Calendar from
 * `Calendar.getInstance()`, replaces only YEAR/MONTH/DAY_OF_MONTH with the
 * current in-game date, then adds exactly 319 calendar days.
 *
 * The creation Calendar is therefore an explicit input here. That preserves the
 * legacy time-of-day/time-zone behavior without introducing wall-clock access in
 * modern deterministic gameplay code.
 */
data class LegacyLoanRecord(
    val playerCode: Int,
    val sourceClubCode: Int,
    val expiryMillis: Long,
)

object LegacyLoanRecordRule {
    const val LEGACY_EXPIRY_DAYS: Int = 319

    fun create(
        playerCode: Int,
        sourceClubCode: Int,
        gameCalendar: Calendar,
        creationCalendar: Calendar,
    ): LegacyLoanRecord {
        val expiry = creationCalendar.clone() as Calendar
        expiry.set(
            gameCalendar.get(Calendar.YEAR),
            gameCalendar.get(Calendar.MONTH),
            gameCalendar.get(Calendar.DAY_OF_MONTH),
        )
        expiry.add(Calendar.DAY_OF_MONTH, LEGACY_EXPIRY_DAYS)

        return LegacyLoanRecord(
            playerCode = playerCode,
            sourceClubCode = sourceClubCode,
            expiryMillis = expiry.timeInMillis,
        )
    }

    fun append(
        existing: List<LegacyLoanRecord>,
        playerCode: Int,
        sourceClubCode: Int,
        gameCalendar: Calendar,
        creationCalendar: Calendar,
    ): List<LegacyLoanRecord> = existing + create(
        playerCode = playerCode,
        sourceClubCode = sourceClubCode,
        gameCalendar = gameCalendar,
        creationCalendar = creationCalendar,
    )
}

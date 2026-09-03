package com.leomala.footballdynasty.domain.manager

import java.util.Calendar

/**
 * Pure reconstruction of the serialized `components.o2` loan record created by
 * `best.o.q(best.c0)` and consumed by `best.b.d4()`.
 *
 * Creation evidence:
 * - stores the player and the source club;
 * - appends the record to the global ArrayList in insertion order;
 * - seeds a Calendar from `Calendar.getInstance()`;
 * - replaces only YEAR/MONTH/DAY_OF_MONTH with the current in-game date;
 * - adds exactly 319 calendar days.
 *
 * Expiry evidence from `best.b.d4()`:
 * - expiry is strict `recordCalendar.before(currentCalendar)` (equality is not expired);
 * - when player and stored source club are both present, the player is moved back via
 *   `best.o.U1(sourceClub)`;
 * - `U1` delegates to `T1(sourceClub, 0, false, false, true)`, i.e. a zero-value,
 *   non-financial return movement;
 * - an expired record is removed after the return attempt when that loop iteration
 *   completes normally.
 *
 * Calendar/wall-clock inputs are explicit so the modern rule remains deterministic.
 */
data class LegacyLoanRecord(
    val playerCode: Int,
    val sourceClubCode: Int,
    val expiryMillis: Long,
)

data class LegacyLoanExpiryDecision(
    val expired: Boolean,
    val executeReturnMove: Boolean,
    val removeRecord: Boolean,
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

    fun expiryDecision(
        record: LegacyLoanRecord,
        currentCalendarMillis: Long,
        playerPresent: Boolean,
        storedSourceClubPresent: Boolean,
    ): LegacyLoanExpiryDecision {
        val expired = record.expiryMillis < currentCalendarMillis
        return LegacyLoanExpiryDecision(
            expired = expired,
            executeReturnMove = expired && playerPresent && storedSourceClubPresent,
            removeRecord = expired,
        )
    }

    /**
     * Exact `U1 -> T1(sourceClub, 0, false, false, true)` transfer input used when
     * an expired loan returns to the club stored in `components.o2.c`.
     */
    fun returnTransferInput(
        record: LegacyLoanRecord,
        currentClubPresent: Boolean,
        currentClubActive: Boolean,
        storedSourceClubActive: Boolean,
        playerContractEndMillisBefore: Long,
        currentGameMillis: Long,
        currentCalendarMillis: Long?,
        currentClubPrimarySlotMatchesPlayer: Boolean,
        currentClubSecondarySlotMatchesPlayer: Boolean,
    ): LegacyTransferExecutionInput = LegacyTransferExecutionInput(
        sourceClubPresent = currentClubPresent,
        sourceClubActive = currentClubActive,
        destinationClubActive = storedSourceClubActive,
        destinationClubId = record.sourceClubCode,
        transferValue = 0,
        legacySecondaryChargeFlag = false,
        loanMove = false,
        legacyNonFinancialMoveFlag = true,
        playerContractEndMillisBefore = playerContractEndMillisBefore,
        currentGameMillis = currentGameMillis,
        currentCalendarMillis = currentCalendarMillis,
        sourcePrimarySlotMatchesPlayer = currentClubPrimarySlotMatchesPlayer,
        sourceSecondarySlotMatchesPlayer = currentClubSecondarySlotMatchesPlayer,
    )
}

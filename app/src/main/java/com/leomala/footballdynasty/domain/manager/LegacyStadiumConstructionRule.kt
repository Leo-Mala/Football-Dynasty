package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the stadium construction lifecycle in
 * `ActivityEstadio.d()`, `components.y1.a()` and `best.b.e4()`.
 *
 * Calendar construction itself stays at the compatibility boundary because the
 * legacy activity seeds its Calendar from wall-clock state before replacing
 * only year/month/day. This rule receives the resulting timestamp and preserves
 * the proven comparison: a construction completes only when end < current;
 * equality is not complete yet.
 */
data class LegacyStadiumConstructionRecord(
    val stadiumCode: Int,
    val endTimestampMillis: Long,
    val additions: List<Int>,
)

data class LegacyStadiumConstructionStartPlan(
    val accepted: Boolean,
    val debitAmount: Int,
    val financialCategoryCode: Int?,
    val recordToAppend: LegacyStadiumConstructionRecord?,
)

data class LegacyCompletedStadiumConstruction(
    val recordIndex: Int,
    val stadiumCode: Int,
    val positiveAdditionsByCategory: List<Int>,
)

data class LegacyStadiumConstructionSweep(
    val completed: List<LegacyCompletedStadiumConstruction>,
    val remainingRecords: List<LegacyStadiumConstructionRecord>,
)

object LegacyStadiumConstructionRule {
    const val LEGACY_STADIUM_DEBIT_CATEGORY: Int = 7

    fun startPlan(
        clubCash: Long,
        quoteCost: Int,
        stadiumCode: Int,
        endTimestampMillis: Long,
        additions: List<Int>,
    ): LegacyStadiumConstructionStartPlan {
        require(additions.size == 4)

        if (clubCash < quoteCost.toLong()) {
            return LegacyStadiumConstructionStartPlan(
                accepted = false,
                debitAmount = 0,
                financialCategoryCode = null,
                recordToAppend = null,
            )
        }

        return LegacyStadiumConstructionStartPlan(
            accepted = true,
            debitAmount = quoteCost,
            financialCategoryCode = LEGACY_STADIUM_DEBIT_CATEGORY,
            recordToAppend = LegacyStadiumConstructionRecord(
                stadiumCode = stadiumCode,
                endTimestampMillis = endTimestampMillis,
                additions = additions,
            ),
        )
    }

    /**
     * Reconstructs `best.b.e4()`: scan in source order, complete every record
     * whose end Calendar is strictly before the current Calendar, then remove
     * those records after the scan.
     */
    fun sweepCompleted(
        records: List<LegacyStadiumConstructionRecord>,
        currentTimestampMillis: Long,
    ): LegacyStadiumConstructionSweep {
        val completed = records.mapIndexedNotNull { index, record ->
            if (record.endTimestampMillis < currentTimestampMillis) {
                LegacyCompletedStadiumConstruction(
                    recordIndex = index,
                    stadiumCode = record.stadiumCode,
                    positiveAdditionsByCategory = record.additions.map { addition ->
                        if (addition > 0) addition else 0
                    },
                )
            } else {
                null
            }
        }
        val completedIndexes = completed.mapTo(mutableSetOf()) { it.recordIndex }
        val remaining = records.filterIndexed { index, _ -> index !in completedIndexes }

        return LegacyStadiumConstructionSweep(
            completed = completed,
            remainingRecords = remaining,
        )
    }
}

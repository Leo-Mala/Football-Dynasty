package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacySalaryPaymentRule

/**
 * Persisted calendar boundary for the characterized legacy `"ds"` salary event.
 *
 * The finance write uses [CareerManagerRuntimeStore.commitFinanceState], so a concurrent/stale club
 * state is rejected instead of losing another finance mutation. Calendar scheduling and monthly
 * eligibility are both evaluated from the recovered legacy predicates before any Room write.
 */
class CareerSalaryCalendarStore(
    database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)

    suspend fun applyCalendarDay(
        careerId: String,
        clubId: String,
        useDayOfMonthTwoSchedule: Boolean,
        dayOfMonth: Int,
        dayOfWeek: Int,
        currentMonthCode: Int,
        participatingCalendarMonthCodes: Iterable<Int>,
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
    ): LegacyFinanceRuntimeState {
        val before = requireNotNull(managerStore.clubFinanceState(careerId, clubId)) {
            "Missing materialized club finance state $careerId/$clubId"
        }

        if (!LegacySalaryPaymentRule.shouldSchedule(
                useDayOfMonthTwoSchedule = useDayOfMonthTwoSchedule,
                dayOfMonth = dayOfMonth,
                dayOfWeek = dayOfWeek,
            )
        ) {
            return before
        }

        val eligible = LegacySalaryPaymentRule.eligibleForCalendarMonth(
            currentMonthCode = currentMonthCode,
            participatingCalendarMonthCodes = participatingCalendarMonthCodes,
        )
        val after = LegacySalaryPaymentRule.apply(
            state = before,
            seniorSalaryCodes = seniorSalaryCodes,
            youthSalaryCodes = youthSalaryCodes,
            eligibleForCurrentCalendarMonth = eligible,
        )
        if (after == before) return before

        managerStore.commitFinanceState(
            careerId = careerId,
            clubId = clubId,
            expectedBefore = before,
            after = after,
        )
        return after
    }
}

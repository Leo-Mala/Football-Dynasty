package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyFriendlyFinanceRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyFriendlySchedulingResult

/**
 * Persisted finance boundary for the paid-friendly confirmation path proven in
 * legacy `ActivityAmistosos2021.d()`.
 *
 * Negotiation/scheduling remains owned by the already-characterized friendly seam. This store only
 * persists the exact post-acceptance finance mutation: decision 3, requested amount, raw category
 * `-1`, no affordability guard, and the legacy miscellaneous-expense routing.
 */
class CareerFriendlyFinanceStore(
    database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)

    suspend fun applyAcceptedPaidFriendly(
        careerId: String,
        clubId: String,
        schedulingResult: LegacyFriendlySchedulingResult,
    ): LegacyFinanceRuntimeState? {
        val before = requireNotNull(managerStore.clubFinanceState(careerId, clubId)) {
            "Missing materialized club finance state $careerId/$clubId"
        }
        val after = LegacyFriendlyFinanceRuntimeRule.acceptRequestedPayment(
            state = before,
            result = schedulingResult,
        ) ?: return null

        managerStore.commitFinanceState(
            careerId = careerId,
            clubId = clubId,
            expectedBefore = before,
            after = after,
        )
        return after
    }
}

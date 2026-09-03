package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacySponsorPaymentRule

/**
 * Persisted boundary for the annual sponsor mutation reached by `best.b.d() -> best.b.s() -> c0.p()`.
 *
 * The resulting cash + ledger state is committed through [CareerManagerRuntimeStore.commitFinanceState]
 * with stale-state protection. No new scheduling policy is introduced here: callers invoke this only
 * from the already-characterized new-year progression seam.
 */
class CareerSponsorPaymentStore(
    database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)
    private val financeInputResolver = CareerClubFinanceInputResolver(database)

    /**
     * Production-facing annual sponsor path. Country, O(), Q0() and payroll are resolved from the
     * frozen source row plus persisted career state; only the characterized global state-championship
     * flag remains a caller input.
     */
    suspend fun applyAnnualSponsorFromPersistedClubState(
        careerId: String,
        clubId: String,
        playStateChampionship: Boolean,
    ): LegacyFinanceRuntimeState {
        val input = financeInputResolver.resolve(careerId, clubId)
        return applyAnnualSponsor(
            careerId = careerId,
            clubId = clubId,
            rawCountryCode = input.rawCountryCode,
            rawDivisionCode = input.rawDivisionCode,
            playStateChampionship = playStateChampionship,
            seniorSalaryCodes = input.seniorSalaryCodes,
            youthSalaryCodes = input.youthSalaryCodes,
            recordFinanceLedger = input.recordFinanceLedger,
        )
    }

    /** Low-level characterized boundary retained for pure/integration fixtures. */
    suspend fun applyAnnualSponsor(
        careerId: String,
        clubId: String,
        rawCountryCode: Int,
        rawDivisionCode: Int,
        playStateChampionship: Boolean,
        seniorSalaryCodes: Iterable<Int>,
        youthSalaryCodes: Iterable<Int>,
        recordFinanceLedger: Boolean,
    ): LegacyFinanceRuntimeState {
        val before = requireNotNull(managerStore.clubFinanceState(careerId, clubId)) {
            "Missing materialized club finance state $careerId/$clubId"
        }
        val after = LegacySponsorPaymentRule.apply(
            state = before,
            rawCountryCode = rawCountryCode,
            rawDivisionCode = rawDivisionCode,
            playStateChampionship = playStateChampionship,
            seniorSalaryCodes = seniorSalaryCodes,
            youthSalaryCodes = youthSalaryCodes,
            recordFinanceLedger = recordFinanceLedger,
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

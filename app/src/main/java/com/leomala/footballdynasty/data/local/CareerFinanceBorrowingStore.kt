package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeRule
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState

/**
 * Persisted execution boundary for the borrowing controls exposed by legacy `ActivityFinancas`
 * and for the monthly `"dJ"` borrowing-charge pass from `best.a.r()`.
 *
 * Borrow eligibility resolves raw `c0.O()` from the already-materialized V9 club state. The caller
 * cannot inject a division. Monthly charges visit materialized club finance rows in immutable
 * legacy club source order and commit the whole pass in one Room transaction.
 */
class CareerFinanceBorrowingStore(
    private val database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)
    private val ticketStore = CareerTicketRuntimeStore(database)
    private val managerDao = database.careerManagerRuntimeDao()

    suspend fun borrowFromPersistedClubState(
        careerId: String,
        clubId: String,
    ): LegacyFinanceRuntimeState {
        val division = requireNotNull(ticketStore.findClubState(careerId, clubId)) {
            "Missing materialized V9 club finance/ticket state $careerId/$clubId"
        }.rawDivisionCode
        val before = requireNotNull(managerStore.clubFinanceState(careerId, clubId)) {
            "Missing materialized club finance state $careerId/$clubId"
        }
        val result = LegacyFinanceRuntimeRule.borrow(before, division)
        if (!result.accepted) return before

        managerStore.commitFinanceState(
            careerId = careerId,
            clubId = clubId,
            expectedBefore = before,
            after = result.state,
        )
        return result.state
    }

    suspend fun repayFromPersistedClubState(
        careerId: String,
        clubId: String,
    ): LegacyFinanceRuntimeState {
        val before = requireNotNull(managerStore.clubFinanceState(careerId, clubId)) {
            "Missing materialized club finance state $careerId/$clubId"
        }
        val result = LegacyFinanceRuntimeRule.repay(before)
        if (!result.accepted) return before

        managerStore.commitFinanceState(
            careerId = careerId,
            clubId = clubId,
            expectedBefore = before,
            after = result.state,
        )
        return result.state
    }

    /**
     * Executes the legacy world-level monthly borrowing-charge sweep in source club order.
     * Clubs without materialized finance state mirror legacy clubs whose `T()` finance object is
     * absent and are skipped. A positive charge may drive cash negative exactly as `best.a.r()`.
     */
    suspend fun applyMonthlyBorrowingCharges(
        careerId: String,
    ): List<Pair<String, LegacyFinanceRuntimeState>> = database.withTransaction {
        val materializedClubIds = managerDao.clubRuntimeForCareer(careerId)
            .mapTo(linkedSetOf()) { it.clubId }
        val applied = ArrayList<Pair<String, LegacyFinanceRuntimeState>>()

        database.clubDao().all().forEach { club ->
            if (club.id !in materializedClubIds) return@forEach
            val before = requireNotNull(managerStore.clubFinanceState(careerId, club.id)) {
                "Missing materialized club finance state $careerId/${club.id}"
            }
            val after = LegacyFinanceRuntimeRule.applyMonthlyBorrowingCharge(before)
            if (after != before) {
                managerStore.commitFinanceState(
                    careerId = careerId,
                    clubId = club.id,
                    expectedBefore = before,
                    after = after,
                )
                applied += club.id to after
            }
        }
        applied
    }
}

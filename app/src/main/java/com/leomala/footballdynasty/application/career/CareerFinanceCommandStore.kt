package com.leomala.footballdynasty.application.career

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerFinanceBorrowingStore
import com.leomala.footballdynasty.data.local.CareerTicketRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Phase 17 write boundary for the already-characterized legacy borrowing controls of the
 * persisted managed club.
 *
 * The UI supplies only the career identity and the desired legacy action. Club identity,
 * division, finance state, fixed borrowing/repayment step, ceiling and charge are all resolved
 * from already-persisted state or from the certified legacy rules. Missing required state fails
 * closed and no alternate club/default finance state is synthesized.
 */
class CareerFinanceCommandStore(
    private val database: FootballDynastyDatabase,
    private val catalogStore: CareerFinanceCatalogStore = CareerFinanceCatalogStore(database),
    private val borrowingStore: CareerFinanceBorrowingStore = CareerFinanceBorrowingStore(database),
    private val ticketStore: CareerTicketRuntimeStore = CareerTicketRuntimeStore(database),
) {
    data class FinanceCommandResult(
        val snapshot: CareerFinanceCatalogStore.FinanceSnapshot,
        val accepted: Boolean,
    )

    suspend fun borrow(careerId: String): FinanceCommandResult? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val before = catalogStore.loadFinances(careerId) ?: return@withTransaction null

        // Legacy borrowing eligibility depends on c0.O(); the persisted V9 ticket/club state owns
        // that raw division code. Absence must never be replaced with a guessed division.
        if (ticketStore.findClubState(careerId, before.clubId) == null) {
            return@withTransaction null
        }

        borrowingStore.borrowFromPersistedClubState(careerId, before.clubId)
        resultAfter(before)
    }

    suspend fun repay(careerId: String): FinanceCommandResult? = database.withTransaction {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val before = catalogStore.loadFinances(careerId) ?: return@withTransaction null

        borrowingStore.repayFromPersistedClubState(careerId, before.clubId)
        resultAfter(before)
    }

    private suspend fun resultAfter(
        before: CareerFinanceCatalogStore.FinanceSnapshot,
    ): FinanceCommandResult {
        val after = requireNotNull(catalogStore.loadFinances(before.careerId)) {
            "Managed-club finance state disappeared during command ${before.careerId}/${before.clubId}"
        }
        require(after.clubId == before.clubId) {
            "Managed club changed during finance command ${before.clubId} -> ${after.clubId}"
        }
        return FinanceCommandResult(
            snapshot = after,
            accepted = after != before,
        )
    }
}

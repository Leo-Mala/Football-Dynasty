package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerManagerRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase

/**
 * Read-only Phase 17 projection of the persisted legacy finance runtime for the managed club.
 *
 * Missing materialized finance state fails closed. No source/default balance is synthesized.
 */
class CareerFinanceCatalogStore(
    private val database: FootballDynastyDatabase,
    private val managerStore: CareerManagerRuntimeStore = CareerManagerRuntimeStore(database),
) {
    data class FinanceSnapshot(
        val careerId: String,
        val clubId: String,
        val clubName: String,
        val cash: Long,
        val totalIncome: Long,
        val totalExpense: Long,
        val periodBalance: Long,
        val borrowed: Int,
        val monthlyBorrowingCharge: Int,
    )

    suspend fun loadFinances(careerId: String): FinanceSnapshot? {
        val core = database.careerCoreStateDao().findById(careerId) ?: return null
        val clubId = core.managedClubId ?: return null
        val club = database.clubDao().findById(clubId) ?: return null
        val finance = managerStore.clubFinanceState(careerId, clubId) ?: return null
        val ledger = finance.ledger

        return FinanceSnapshot(
            careerId = careerId,
            clubId = clubId,
            clubName = club.name,
            cash = finance.cash,
            totalIncome = ledger.totalIncome(),
            totalExpense = ledger.totalExpense(),
            periodBalance = ledger.balance(),
            borrowed = ledger.borrowed,
            monthlyBorrowingCharge = ledger.monthlyBorrowingCharge,
        )
    }
}

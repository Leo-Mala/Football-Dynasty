package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyStadiumExpansionRule
import com.leomala.footballdynasty.domain.manager.LegacyStadiumFinanceRuntimeResult
import com.leomala.footballdynasty.domain.manager.LegacyStadiumFinanceRuntimeRule

/**
 * Transactional Phase 13 boundary for starting a legacy stadium construction.
 *
 * Current capacities and club finance are resolved from Room inside the same transaction before
 * the characterized legacy quote/debit rules execute. `stadiumCode` stays opaque. V10 additionally
 * persists the already-known `clubId` as modern ownership metadata so a future completion can
 * mutate the correct persisted stadium without reinterpreting any legacy identity field.
 */
class CareerStadiumConstructionRuntimeStore(
    private val database: FootballDynastyDatabase,
) {
    private val dao = database.careerManagerRuntimeDao()

    suspend fun startFromPersistedState(
        careerId: String,
        clubId: String,
        additions: List<Int>,
        legacyJValue: Int,
        stadiumCode: Int,
        endTimestampMillis: Long,
    ): LegacyStadiumFinanceRuntimeResult = database.withTransaction {
        require(additions.size == 4) { "Legacy stadium construction requires four category additions" }

        val clubRuntime = requireNotNull(dao.findClubRuntime(careerId, clubId)) {
            "Missing materialized club runtime $careerId/$clubId"
        }
        val stadiumRuntime = requireNotNull(dao.findStadiumRuntime(careerId, clubId)) {
            "Missing materialized stadium runtime $careerId/$clubId"
        }

        val before = LegacyFinanceRuntimeState(
            cash = clubRuntime.cash,
            ledger = clubRuntime.toLedger(),
        )
        val quote = LegacyStadiumExpansionRule.quote(
            currentCapacities = listOf(
                stadiumRuntime.sector0Capacity,
                stadiumRuntime.sector1Capacity,
                stadiumRuntime.sector2Capacity,
                stadiumRuntime.sector3Capacity,
            ),
            additions = additions,
            legacyJValue = legacyJValue,
        )
        val result = LegacyStadiumFinanceRuntimeRule.startConstruction(
            state = before,
            quote = quote,
            stadiumCode = stadiumCode,
            endTimestampMillis = endTimestampMillis,
        )

        if (result.accepted) {
            val record = requireNotNull(result.recordToAppend) {
                "Accepted stadium construction must carry a record"
            }
            require(record.additions.size == 4)
            dao.upsertClubRuntime(clubRuntime.withFinance(result.state))
            val ordinal = (dao.maxStadiumConstructionOrdinal(careerId) ?: -1) + 1
            dao.upsertStadiumConstruction(
                CareerStadiumConstructionEntity(
                    careerId = careerId,
                    sourceOrdinal = ordinal,
                    stadiumCode = record.stadiumCode,
                    endTimestampMillis = record.endTimestampMillis,
                    addition0 = record.additions[0],
                    addition1 = record.additions[1],
                    addition2 = record.additions[2],
                    addition3 = record.additions[3],
                    ownerClubId = clubId,
                )
            )
        }

        result
    }

    private fun CareerClubManagerRuntimeEntity.toLedger() = LegacyFinanceLedgerState(
        ticketIncome = ticketIncome,
        playerSaleIncome = playerSaleIncome,
        prizeIncome = prizeIncome,
        sponsorIncome = sponsorIncome,
        playerPurchaseExpense = playerPurchaseExpense,
        stadiumExpense = stadiumExpense,
        salaryExpense = salaryExpense,
        borrowingChargeExpense = borrowingChargeExpense,
        fineExpense = fineExpense,
        miscellaneousExpense = miscellaneousExpense,
        borrowed = borrowed,
        monthlyBorrowingCharge = monthlyBorrowingCharge,
    )

    private fun CareerClubManagerRuntimeEntity.withFinance(finance: LegacyFinanceRuntimeState) = copy(
        cash = finance.cash,
        ticketIncome = finance.ledger.ticketIncome,
        playerSaleIncome = finance.ledger.playerSaleIncome,
        prizeIncome = finance.ledger.prizeIncome,
        sponsorIncome = finance.ledger.sponsorIncome,
        playerPurchaseExpense = finance.ledger.playerPurchaseExpense,
        stadiumExpense = finance.ledger.stadiumExpense,
        salaryExpense = finance.ledger.salaryExpense,
        borrowingChargeExpense = finance.ledger.borrowingChargeExpense,
        fineExpense = finance.ledger.fineExpense,
        miscellaneousExpense = finance.ledger.miscellaneousExpense,
        borrowed = finance.ledger.borrowed,
        monthlyBorrowingCharge = finance.ledger.monthlyBorrowingCharge,
    )
}

package com.leomala.footballdynasty.data.repository

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.CareerCoreStateRoomAdapter
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerIntegrityValidator
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerRule
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.repository.CareerStateRepository
import com.leomala.footballdynasty.foundation.error.CareerIntegrityException

class RoomCareerStateRepository(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) : CareerStateRepository {
    override suspend fun save(state: CareerState): CareerState = persistValidated(state)

    override suspend fun saveTransition(
        state: CareerState,
        command: CareerCommand,
    ): CareerState = when (command) {
        CareerCommand.TransitionSeason -> database.withTransaction {
            // Legacy best.b.d() begins with g1().*.l1(), whose proved finance side effect is
            // best.c0.l1() -> best.m.z(). Apply that persisted mutation before the later calendar
            // state becomes observable, without reconstructing any unproved annual stages.
            resetMaterializedFinancePeriods(state.id)
            persistValidated(state)
        }
        else -> persistValidated(state)
    }

    override suspend fun findById(id: String): CareerState? =
        database.careerCoreStateDao().findById(id)?.let { entity ->
            CareerCoreStateRoomAdapter.state(entity).also { state ->
                CareerIntegrityValidator.validate(state)
            }
        }

    private suspend fun persistValidated(state: CareerState): CareerState {
        CareerIntegrityValidator.validate(state)
        if (database.careerMetadataDao().findById(state.id) == null) {
            throw CareerIntegrityException("Career metadata ${state.id} must exist before core state")
        }
        state.managedClub?.let { managed ->
            if (database.clubDao().findById(managed.clubId) == null) {
                throw CareerIntegrityException("Managed club ${managed.clubId} does not resolve")
            }
        }
        database.careerCoreStateDao().upsert(
            CareerCoreStateRoomAdapter.entity(state, clockMillis())
        )
        return requireNotNull(findById(state.id))
    }

    private suspend fun resetMaterializedFinancePeriods(careerId: String) {
        val dao = database.careerManagerRuntimeDao()
        dao.clubRuntimeForCareer(careerId).forEach { runtime ->
            val resetLedger = LegacyFinanceLedgerRule.resetPeriod(runtime.toLedger())
            dao.upsertClubRuntime(runtime.withLedger(resetLedger))
        }
    }

    private fun CareerClubManagerRuntimeEntity.toLedger(): LegacyFinanceLedgerState =
        LegacyFinanceLedgerState(
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

    private fun CareerClubManagerRuntimeEntity.withLedger(
        ledger: LegacyFinanceLedgerState,
    ): CareerClubManagerRuntimeEntity = copy(
        ticketIncome = ledger.ticketIncome,
        playerSaleIncome = ledger.playerSaleIncome,
        prizeIncome = ledger.prizeIncome,
        sponsorIncome = ledger.sponsorIncome,
        playerPurchaseExpense = ledger.playerPurchaseExpense,
        stadiumExpense = ledger.stadiumExpense,
        salaryExpense = ledger.salaryExpense,
        borrowingChargeExpense = ledger.borrowingChargeExpense,
        fineExpense = ledger.fineExpense,
        miscellaneousExpense = ledger.miscellaneousExpense,
        borrowed = ledger.borrowed,
        monthlyBorrowingCharge = ledger.monthlyBorrowingCharge,
    )
}

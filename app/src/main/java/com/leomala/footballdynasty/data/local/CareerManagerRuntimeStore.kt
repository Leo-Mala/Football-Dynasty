package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerActiveLoanEntity
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerCommercialEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerTransferStateEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerRule
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyPlayerCommercialState
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionRecord
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionRule
import com.leomala.footballdynasty.domain.manager.LegacyStadiumConstructionSweep
import com.leomala.footballdynasty.domain.manager.LegacyStadiumFinanceRuntimeResult
import com.leomala.footballdynasty.domain.manager.LegacyTransferClubRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionPlan
import com.leomala.footballdynasty.domain.manager.LegacyTransferPlayerRuntimeState
import com.leomala.footballdynasty.domain.manager.LegacyTransferRuntimeMutation
import com.leomala.footballdynasty.domain.manager.LegacyTransferRuntimeState

/** Optional active-loan record supplied only by the already-characterized loan path. */
data class CareerActiveLoanMaterialization(
    val sourceClubId: String,
    val destinationClubId: String,
    val expiresAtEpochMillis: Long,
)

enum class CareerActiveLoanMutationKind {
    UNCHANGED,
    UPSERT,
    DELETE,
}

/** Explicit loan mutation. Null never means delete. */
data class CareerActiveLoanMutation(
    val kind: CareerActiveLoanMutationKind,
    val materialization: CareerActiveLoanMaterialization? = null,
) {
    init {
        require((kind == CareerActiveLoanMutationKind.UPSERT) == (materialization != null)) {
            "Only UPSERT carries an active-loan materialization"
        }
    }

    companion object {
        val UNCHANGED = CareerActiveLoanMutation(CareerActiveLoanMutationKind.UNCHANGED)
        val DELETE = CareerActiveLoanMutation(CareerActiveLoanMutationKind.DELETE)
        fun upsert(materialization: CareerActiveLoanMaterialization) =
            CareerActiveLoanMutation(CareerActiveLoanMutationKind.UPSERT, materialization)
    }
}

/**
 * Transactional Room boundary for characterized Marco B commercial/transfer/finance state.
 *
 * Missing source state is never replaced with a default. Callers must explicitly materialize the
 * proven values before mutations that depend on them can execute.
 */
class CareerManagerRuntimeStore(
    private val database: FootballDynastyDatabase,
) {
    private val managerDao = database.careerManagerRuntimeDao()
    private val playerDao = database.careerPlayerRuntimeDao()
    private val clubDao = database.clubDao()

    suspend fun materializePlayerState(
        careerId: String,
        playerId: String,
        commercial: LegacyPlayerCommercialState,
        transfer: LegacyTransferPlayerRuntimeState,
    ) = database.withTransaction {
        val runtime = requireNotNull(playerDao.findRuntime(careerId, playerId)) {
            "Cannot materialize manager state for missing player $careerId/$playerId"
        }
        require(commercial.contract.salaryCode == transfer.salaryCode) {
            "Commercial salary diverges from transfer salary for $playerId"
        }
        playerDao.upsertRuntime(
            runtime.copy(
                contractEndEpochMillis = transfer.contractEndMillis,
                legacyX = transfer.rawX,
                legacyY = transfer.rawY,
                legacyZ = transfer.rawZ,
            )
        )
        managerDao.upsertPlayerCommercial(
            commercialEntity(careerId, playerId, commercial)
        )
        managerDao.upsertPlayerTransferState(
            CareerPlayerTransferStateEntity(
                careerId = careerId,
                playerId = playerId,
                legacyPlayerCode = transfer.playerCode,
                legacyClubCode = transfer.clubCode,
                rawCrossActiveFlag = transfer.rawCrossActiveFlag,
                rawOCode = transfer.rawOCode,
                rawDCode = transfer.rawDCode,
            )
        )
    }

    suspend fun materializeClubState(
        careerId: String,
        clubId: String,
        transfer: LegacyTransferClubRuntimeState,
        finance: LegacyFinanceRuntimeState,
    ) = database.withTransaction {
        val club = requireNotNull(clubDao.findById(clubId)) { "Missing club $clubId" }
        require(club.legacyId == transfer.clubCode) {
            "Club $clubId legacy id ${club.legacyId} diverges from transfer code ${transfer.clubCode}"
        }
        require(transfer.funds == finance.cash) {
            "Transfer funds and finance cash diverge for $clubId"
        }
        managerDao.upsertClubRuntime(
            clubEntity(careerId, clubId, transfer, finance.ledger)
        )
    }

    suspend fun playerCommercialState(
        careerId: String,
        playerId: String,
    ): LegacyPlayerCommercialState? = managerDao.findPlayerCommercial(careerId, playerId)?.toDomain()

    suspend fun clubFinanceState(
        careerId: String,
        clubId: String,
    ): LegacyFinanceRuntimeState? = managerDao.findClubRuntime(careerId, clubId)?.toFinanceState()

    suspend fun activeLoanState(
        careerId: String,
        playerId: String,
    ): CareerActiveLoanMaterialization? = managerDao.findActiveLoan(careerId, playerId)?.let { entity ->
        CareerActiveLoanMaterialization(
            sourceClubId = entity.sourceClubId,
            destinationClubId = entity.destinationClubId,
            expiresAtEpochMillis = entity.expiresAtEpochMillis,
        )
    }

    suspend fun stadiumConstructionRecords(careerId: String): List<LegacyStadiumConstructionRecord> =
        managerDao.stadiumConstructions(careerId).map { it.toDomain() }

    /** Lossless replacement of the seven proven commercial fields, plus an optional proven date write. */
    suspend fun commitPlayerCommercialState(
        careerId: String,
        playerId: String,
        expectedBefore: LegacyPlayerCommercialState,
        after: LegacyPlayerCommercialState,
        contractEndEpochMillisAfter: Long? = null,
    ) = database.withTransaction {
        val current = requireNotNull(managerDao.findPlayerCommercial(careerId, playerId)) {
            "Missing materialized commercial state for $playerId"
        }
        require(current.toDomain() == expectedBefore) { "Stale commercial state for $playerId" }
        managerDao.upsertPlayerCommercial(commercialEntity(careerId, playerId, after))
        if (contractEndEpochMillisAfter != null) {
            val runtime = requireNotNull(playerDao.findRuntime(careerId, playerId)) {
                "Missing runtime for $playerId"
            }
            playerDao.upsertRuntime(runtime.copy(contractEndEpochMillis = contractEndEpochMillisAfter))
        }
    }

    /** Persist a previously characterized finance mutation without touching transfer-only club fields. */
    suspend fun commitFinanceState(
        careerId: String,
        clubId: String,
        expectedBefore: LegacyFinanceRuntimeState,
        after: LegacyFinanceRuntimeState,
    ) = database.withTransaction {
        val current = requireNotNull(managerDao.findClubRuntime(careerId, clubId)) {
            "Missing materialized club runtime $careerId/$clubId"
        }
        require(current.toFinanceState() == expectedBefore) { "Stale finance state for $clubId" }
        managerDao.upsertClubRuntime(current.withFinance(after))
    }

    suspend fun mutateActiveLoan(
        careerId: String,
        playerId: String,
        mutation: CareerActiveLoanMutation,
    ) = database.withTransaction {
        applyLoanMutation(careerId, playerId, mutation, expectedSourceClubId = null, expectedDestinationClubId = null)
    }

    suspend fun commitTransfer(
        careerId: String,
        playerId: String,
        destinationLegacyClubId: Int,
        rosterKind: String,
        plan: LegacyTransferExecutionPlan,
        salaryAfterPurchase: Int? = null,
        loanMutation: CareerActiveLoanMutation = CareerActiveLoanMutation.UNCHANGED,
    ): LegacyTransferRuntimeState = database.withTransaction {
        require(plan.destinationClubId == destinationLegacyClubId) {
            "Transfer plan destination ${plan.destinationClubId} diverges from requested $destinationLegacyClubId"
        }
        val destinationClub = uniqueClubByLegacyId(destinationLegacyClubId)
        val currentMembership = playerDao.findMembership(careerId, playerId)
        if (plan.removeFromSourceRoster) {
            requireNotNull(currentMembership) { "Transfer requires source membership for $playerId" }
            require(currentMembership.rosterKind == rosterKind) {
                "Source roster kind ${currentMembership.rosterKind} diverges from $rosterKind"
            }
        }

        val before = loadTransferRuntime(careerId, playerId, destinationClub.id, rosterKind)
        val after = LegacyTransferRuntimeMutation.apply(before, plan, salaryAfterPurchase)

        val runtime = requireNotNull(playerDao.findRuntime(careerId, playerId))
        val commercial = requireNotNull(managerDao.findPlayerCommercial(careerId, playerId)) {
            "Missing materialized commercial state for $playerId"
        }
        val transferPlayer = requireNotNull(managerDao.findPlayerTransferState(careerId, playerId)) {
            "Missing materialized transfer state for $playerId"
        }

        playerDao.upsertRuntime(
            runtime.copy(
                contractEndEpochMillis = after.player.contractEndMillis,
                legacyX = after.player.rawX,
                legacyY = after.player.rawY,
                legacyZ = after.player.rawZ,
            )
        )
        managerDao.upsertPlayerCommercial(commercial.copy(salario = after.player.salaryCode))
        managerDao.upsertPlayerTransferState(
            transferPlayer.copy(
                legacyClubCode = after.player.clubCode,
                rawCrossActiveFlag = after.player.rawCrossActiveFlag,
                rawDCode = after.player.rawDCode,
            )
        )

        val destinationOrdinal =
            (playerDao.maxMembershipOrdinal(careerId, destinationClub.id, rosterKind) ?: -1) + 1
        playerDao.upsertMembership(
            CareerSquadMembershipEntity(
                careerId = careerId,
                playerId = playerId,
                clubId = destinationClub.id,
                rosterKind = rosterKind,
                sourceOrdinal = destinationOrdinal,
            )
        )

        val sourceClubId = currentMembership?.clubId
        if (sourceClubId != null && after.sourceClub != null) {
            val sourceEntity = requireNotNull(managerDao.findClubRuntime(careerId, sourceClubId)) {
                "Missing materialized source club runtime $sourceClubId"
            }
            var sourceLedger = ledger(sourceEntity)
            if (sourceEntity.active) {
                val grossSale = plan.sellerFundsDelta + plan.secondarySellerCharge.toLong()
                if (grossSale > 0L) {
                    require(grossSale <= Int.MAX_VALUE.toLong()) {
                        "Legacy sale ledger amount exceeds Int: $grossSale"
                    }
                    sourceLedger = LegacyFinanceLedgerRule.addIncome(
                        sourceLedger,
                        grossSale.toInt(),
                        LegacyFinanceLedgerRule.INCOME_PLAYER_SALE,
                    )
                }
                if (plan.secondarySellerCharge > 0) {
                    sourceLedger = LegacyFinanceLedgerRule.addExpense(
                        sourceLedger,
                        plan.secondarySellerCharge,
                        LegacyFinanceLedgerRule.EXPENSE_FINE,
                    )
                }
            }
            managerDao.upsertClubRuntime(
                sourceEntity.copy(
                    cash = after.sourceClub.funds,
                    primarySlotPlayerCode = after.sourceClub.primarySlotPlayerCode,
                    secondarySlotPlayerCode = after.sourceClub.secondarySlotPlayerCode,
                    rawStateFlag = after.sourceClub.rawStateFlag,
                ).withLedger(sourceLedger)
            )
        }

        val destinationEntity = requireNotNull(managerDao.findClubRuntime(careerId, destinationClub.id)) {
            "Missing materialized destination club runtime ${destinationClub.id}"
        }
        var destinationLedger = ledger(destinationEntity)
        if (plan.buyerFundsDelta < 0L) {
            val purchase = -plan.buyerFundsDelta
            require(purchase <= Int.MAX_VALUE.toLong()) {
                "Legacy purchase ledger amount exceeds Int: $purchase"
            }
            destinationLedger = LegacyFinanceLedgerRule.addExpense(
                destinationLedger,
                purchase.toInt(),
                LegacyFinanceLedgerRule.EXPENSE_PLAYER_PURCHASE,
            )
        }
        managerDao.upsertClubRuntime(
            destinationEntity.copy(
                cash = after.destinationClub.funds,
                primarySlotPlayerCode = after.destinationClub.primarySlotPlayerCode,
                secondarySlotPlayerCode = after.destinationClub.secondarySlotPlayerCode,
                rawStateFlag = after.destinationClub.rawStateFlag,
            ).withLedger(destinationLedger)
        )

        applyLoanMutation(
            careerId = careerId,
            playerId = playerId,
            mutation = loanMutation,
            expectedSourceClubId = sourceClubId,
            expectedDestinationClubId = destinationClub.id,
        )
        after
    }

    /** Atomically persists the already-characterized stadium debit plus raw construction record. */
    suspend fun commitStadiumConstruction(
        careerId: String,
        clubId: String,
        expectedFinanceBefore: LegacyFinanceRuntimeState,
        result: LegacyStadiumFinanceRuntimeResult,
    ) = database.withTransaction {
        require(result.accepted) { "Rejected stadium construction cannot be persisted" }
        val record = requireNotNull(result.recordToAppend) { "Accepted construction must carry a record" }
        require(record.additions.size == 4)
        val current = requireNotNull(managerDao.findClubRuntime(careerId, clubId)) {
            "Missing materialized club runtime $careerId/$clubId"
        }
        require(current.toFinanceState() == expectedFinanceBefore) { "Stale finance state for $clubId" }
        managerDao.upsertClubRuntime(current.withFinance(result.state))
        val ordinal = (managerDao.maxStadiumConstructionOrdinal(careerId) ?: -1) + 1
        managerDao.upsertStadiumConstruction(record.toEntity(careerId, ordinal, clubId))
    }

    /**
     * Persists exactly the `best.b.e4()` list-removal lifecycle. Capacity/category application is
     * intentionally not invented here; completed raw additions are returned to the next proven seam.
     */
    suspend fun sweepStadiumConstructions(
        careerId: String,
        currentTimestampMillis: Long,
    ): LegacyStadiumConstructionSweep = database.withTransaction {
        val entities = managerDao.stadiumConstructions(careerId)
        val before = entities.map { it.toDomain() }
        val sweep = LegacyStadiumConstructionRule.sweepCompleted(before, currentTimestampMillis)
        val completedIndexes = sweep.completed.mapTo(mutableSetOf()) { it.recordIndex }
        val remaining = entities.filterIndexed { index, _ -> index !in completedIndexes }
        managerDao.deleteStadiumConstructions(careerId)
        managerDao.upsertStadiumConstructions(
            remaining.mapIndexed { index, entity -> entity.copy(sourceOrdinal = index) }
        )
        sweep
    }

    private suspend fun applyLoanMutation(
        careerId: String,
        playerId: String,
        mutation: CareerActiveLoanMutation,
        expectedSourceClubId: String?,
        expectedDestinationClubId: String?,
    ) {
        when (mutation.kind) {
            CareerActiveLoanMutationKind.UNCHANGED -> Unit
            CareerActiveLoanMutationKind.DELETE -> managerDao.deleteActiveLoan(careerId, playerId)
            CareerActiveLoanMutationKind.UPSERT -> {
                val loan = requireNotNull(mutation.materialization)
                if (expectedSourceClubId != null) {
                    require(loan.sourceClubId == expectedSourceClubId) {
                        "Loan source ${loan.sourceClubId} diverges from source membership $expectedSourceClubId"
                    }
                }
                if (expectedDestinationClubId != null) {
                    require(loan.destinationClubId == expectedDestinationClubId) {
                        "Loan destination ${loan.destinationClubId} diverges from $expectedDestinationClubId"
                    }
                }
                managerDao.upsertActiveLoan(
                    CareerActiveLoanEntity(
                        careerId = careerId,
                        playerId = playerId,
                        sourceClubId = loan.sourceClubId,
                        destinationClubId = loan.destinationClubId,
                        expiresAtEpochMillis = loan.expiresAtEpochMillis,
                    )
                )
            }
        }
    }

    private suspend fun loadTransferRuntime(
        careerId: String,
        playerId: String,
        destinationClubId: String,
        rosterKind: String,
    ): LegacyTransferRuntimeState {
        val runtime = requireNotNull(playerDao.findRuntime(careerId, playerId)) { "Missing runtime for $playerId" }
        val commercial = requireNotNull(managerDao.findPlayerCommercial(careerId, playerId)) {
            "Missing commercial state for $playerId"
        }
        val transfer = requireNotNull(managerDao.findPlayerTransferState(careerId, playerId)) {
            "Missing transfer state for $playerId"
        }
        val membership = playerDao.findMembership(careerId, playerId)
        val sourceClub = membership?.let { member ->
            require(member.rosterKind == rosterKind) { "Unexpected source roster kind ${member.rosterKind}" }
            persistedClubState(careerId, member.clubId, rosterKind)
        }
        val destination = persistedClubState(careerId, destinationClubId, rosterKind)
        return LegacyTransferRuntimeState(
            mainTeamDirty = false,
            player = LegacyTransferPlayerRuntimeState(
                playerCode = transfer.legacyPlayerCode,
                clubCode = transfer.legacyClubCode,
                salaryCode = commercial.salario,
                contractEndMillis = runtime.contractEndEpochMillis,
                rawX = runtime.legacyX,
                rawY = runtime.legacyY,
                rawZ = runtime.legacyZ,
                rawCrossActiveFlag = transfer.rawCrossActiveFlag,
                rawOCode = transfer.rawOCode,
                rawDCode = transfer.rawDCode,
            ),
            sourceClub = sourceClub,
            destinationClub = destination,
        )
    }

    private suspend fun persistedClubState(
        careerId: String,
        clubId: String,
        rosterKind: String,
    ): LegacyTransferClubRuntimeState {
        val club = requireNotNull(clubDao.findById(clubId)) { "Missing club $clubId" }
        val entity = requireNotNull(managerDao.findClubRuntime(careerId, clubId)) {
            "Missing materialized club runtime $careerId/$clubId"
        }
        val playerStates = managerDao.playerTransferStateForCareer(careerId).associateBy { it.playerId }
        val rosterCodes = playerDao.membershipsForClub(careerId, clubId, rosterKind).map { membership ->
            requireNotNull(playerStates[membership.playerId]) {
                "Missing transfer identity for roster player ${membership.playerId}"
            }.legacyPlayerCode
        }
        return LegacyTransferClubRuntimeState(
            clubCode = club.legacyId,
            active = entity.active,
            funds = entity.cash,
            rosterPlayerCodes = rosterCodes,
            primarySlotPlayerCode = entity.primarySlotPlayerCode,
            secondarySlotPlayerCode = entity.secondarySlotPlayerCode,
            rawStateFlag = entity.rawStateFlag,
        )
    }

    private suspend fun uniqueClubByLegacyId(legacyId: Int) = clubDao.findByLegacyId(legacyId).let { clubs ->
        require(clubs.size == 1) { "Legacy club id $legacyId must resolve exactly once; found ${clubs.size}" }
        clubs.single()
    }

    private fun commercialEntity(
        careerId: String,
        playerId: String,
        state: LegacyPlayerCommercialState,
    ) = CareerPlayerCommercialEntity(
        careerId = careerId,
        playerId = playerId,
        salario = state.contract.salaryCode,
        rcClause = state.contract.clauseCode,
        rcRenewYear = state.contract.renewalYearCode,
        rcConvYear = state.contract.conversionYearCode,
        pendSaleClub = state.pendingMovement.clubCode,
        pendSaleValue = state.pendingMovement.valueCode,
        pendIsLoan = state.pendingMovement.loanFlag,
    )

    private fun CareerPlayerCommercialEntity.toDomain() = LegacyPlayerCommercialState.fromRaw(
        salario = salario,
        rcClause = rcClause,
        rcRenewYear = rcRenewYear,
        rcConvYear = rcConvYear,
        pendSaleClub = pendSaleClub,
        pendSaleValue = pendSaleValue,
        pendIsLoan = pendIsLoan,
    )

    private fun clubEntity(
        careerId: String,
        clubId: String,
        transfer: LegacyTransferClubRuntimeState,
        ledger: LegacyFinanceLedgerState,
    ) = CareerClubManagerRuntimeEntity(
        careerId = careerId,
        clubId = clubId,
        active = transfer.active,
        cash = transfer.funds,
        primarySlotPlayerCode = transfer.primarySlotPlayerCode,
        secondarySlotPlayerCode = transfer.secondarySlotPlayerCode,
        rawStateFlag = transfer.rawStateFlag,
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

    private fun CareerClubManagerRuntimeEntity.toFinanceState() = LegacyFinanceRuntimeState(
        cash = cash,
        ledger = ledger(this),
    )

    private fun CareerClubManagerRuntimeEntity.withFinance(finance: LegacyFinanceRuntimeState) =
        copy(cash = finance.cash).withLedger(finance.ledger)

    private fun ledger(entity: CareerClubManagerRuntimeEntity) = LegacyFinanceLedgerState(
        ticketIncome = entity.ticketIncome,
        playerSaleIncome = entity.playerSaleIncome,
        prizeIncome = entity.prizeIncome,
        sponsorIncome = entity.sponsorIncome,
        playerPurchaseExpense = entity.playerPurchaseExpense,
        stadiumExpense = entity.stadiumExpense,
        salaryExpense = entity.salaryExpense,
        borrowingChargeExpense = entity.borrowingChargeExpense,
        fineExpense = entity.fineExpense,
        miscellaneousExpense = entity.miscellaneousExpense,
        borrowed = entity.borrowed,
        monthlyBorrowingCharge = entity.monthlyBorrowingCharge,
    )

    private fun CareerClubManagerRuntimeEntity.withLedger(ledger: LegacyFinanceLedgerState) = copy(
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

    private fun CareerStadiumConstructionEntity.toDomain() = LegacyStadiumConstructionRecord(
        stadiumCode = stadiumCode,
        endTimestampMillis = endTimestampMillis,
        additions = listOf(addition0, addition1, addition2, addition3),
    )

    private fun LegacyStadiumConstructionRecord.toEntity(
        careerId: String,
        ordinal: Int,
        ownerClubId: String,
    ): CareerStadiumConstructionEntity {
        require(additions.size == 4)
        return CareerStadiumConstructionEntity(
            careerId = careerId,
            sourceOrdinal = ordinal,
            stadiumCode = stadiumCode,
            endTimestampMillis = endTimestampMillis,
            addition0 = additions[0],
            addition1 = additions[1],
            addition2 = additions[2],
            addition3 = additions[3],
            ownerClubId = ownerClubId,
        )
    }
}

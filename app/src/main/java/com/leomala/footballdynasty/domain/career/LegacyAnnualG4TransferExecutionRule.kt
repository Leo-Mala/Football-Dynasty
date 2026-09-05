package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionInput
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionPlan
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionRule

/**
 * Exact annual `best.b.g4()` transfer-mutation adapter for the already-characterized call
 * `best.o.T1(targetClub, selectedValue, true, false, false)`.
 *
 * This boundary deliberately starts after `best.f` has selected a target club and value. It does
 * not model, replace, seed, or consume any of the raw/non-stateful RNG sources in `best.n.m()`,
 * `best.f`, or `Collections.shuffle(...)`. Those remain an upstream Phase 15 compatibility gap.
 *
 * The returned plan is the same characterized T1 plan already persisted atomically by
 * `CareerManagerRuntimeStore.commitTransfer(...)`; no duplicate transfer semantics are introduced.
 */
data class LegacyAnnualG4TransferExecutionInput(
    val sourceClubPresent: Boolean,
    val sourceClubActive: Boolean,
    val destinationClubActive: Boolean,
    val destinationClubId: Int,
    val transferValue: Int,
    val playerContractEndMillisBefore: Long,
    val currentGameMillis: Long,
    val currentCalendarMillis: Long?,
    val sourcePrimarySlotMatchesPlayer: Boolean,
    val sourceSecondarySlotMatchesPlayer: Boolean,
)

object LegacyAnnualG4TransferExecutionRule {
    fun plan(input: LegacyAnnualG4TransferExecutionInput): LegacyTransferExecutionPlan =
        LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = input.sourceClubPresent,
                sourceClubActive = input.sourceClubActive,
                destinationClubActive = input.destinationClubActive,
                destinationClubId = input.destinationClubId,
                transferValue = input.transferValue,
                legacySecondaryChargeFlag = true,
                loanMove = false,
                legacyNonFinancialMoveFlag = false,
                playerContractEndMillisBefore = input.playerContractEndMillisBefore,
                currentGameMillis = input.currentGameMillis,
                currentCalendarMillis = input.currentCalendarMillis,
                sourcePrimarySlotMatchesPlayer = input.sourcePrimarySlotMatchesPlayer,
                sourceSecondarySlotMatchesPlayer = input.sourceSecondarySlotMatchesPlayer,
            ),
        )
}

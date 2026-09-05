package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionInput
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionPlan
import com.leomala.footballdynasty.domain.manager.LegacyTransferExecutionRule

/**
 * Exact mutation adapter for the expired `components.o2` path in `best.b.d4()`.
 *
 * Official SMALI characterizes `player.U1(club)` as the wrapper
 * `player.T1(club, 0, false, false, true)`. This boundary intentionally starts only after the
 * legacy queue has selected an expired player/club pair. Queue creation, expiry ordering and any
 * durable representation remain separate Phase 15 gaps and are not inferred here.
 */
data class LegacyAnnualDeferredTransferExecutionInput(
    val sourceClubPresent: Boolean,
    val sourceClubActive: Boolean,
    val destinationClubActive: Boolean,
    val destinationClubId: Int,
    val playerContractEndMillisBefore: Long,
    val currentGameMillis: Long,
    val currentCalendarMillis: Long?,
    val sourcePrimarySlotMatchesPlayer: Boolean,
    val sourceSecondarySlotMatchesPlayer: Boolean,
)

object LegacyAnnualDeferredTransferExecutionRule {
    fun plan(input: LegacyAnnualDeferredTransferExecutionInput): LegacyTransferExecutionPlan =
        LegacyTransferExecutionRule.plan(
            LegacyTransferExecutionInput(
                sourceClubPresent = input.sourceClubPresent,
                sourceClubActive = input.sourceClubActive,
                destinationClubActive = input.destinationClubActive,
                destinationClubId = input.destinationClubId,
                transferValue = 0,
                legacySecondaryChargeFlag = false,
                loanMove = false,
                legacyNonFinancialMoveFlag = true,
                playerContractEndMillisBefore = input.playerContractEndMillisBefore,
                currentGameMillis = input.currentGameMillis,
                currentCalendarMillis = input.currentCalendarMillis,
                sourcePrimarySlotMatchesPlayer = input.sourcePrimarySlotMatchesPlayer,
                sourceSecondarySlotMatchesPlayer = input.sourceSecondarySlotMatchesPlayer,
            ),
        )
}

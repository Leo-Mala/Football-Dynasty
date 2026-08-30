package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the state-transition plan in `best.o.T1(best.c0,int,boolean,boolean,boolean)`
 * plus the purchase/loan wrappers used by `ActivityProcura.x(...)` / `ActivityProcura.y(...)`.
 *
 * The two non-loan booleans from T1 remain deliberately neutral because the legacy corpus proves
 * their control-flow effects more strongly than their user-facing meaning. `loanMove` is named
 * because `best.o.q(...)` calls T1 with that flag and `ActivityProcura.y(...)` is the confirmed
 * loan-taking path.
 */
data class LegacyTransferExecutionInput(
    val sourceClubPresent: Boolean,
    val sourceClubActive: Boolean,
    val destinationClubActive: Boolean,
    val destinationClubId: Int,
    val transferValue: Int,
    val legacySecondaryChargeFlag: Boolean,
    val loanMove: Boolean,
    val legacyNonFinancialMoveFlag: Boolean,
    val playerContractEndMillisBefore: Long,
    val currentGameMillis: Long,
    val currentCalendarMillis: Long?,
    val sourcePrimarySlotMatchesPlayer: Boolean,
    val sourceSecondarySlotMatchesPlayer: Boolean,
)

enum class LegacyBooleanMutation {
    UNCHANGED,
    SET_FALSE,
    SET_TRUE,
}

data class LegacyTransferExecutionPlan(
    val markMainTeamDirty: Boolean,
    val destinationClubId: Int,
    val rawXMutation: LegacyBooleanMutation,
    val rawYMutation: LegacyBooleanMutation,
    val rawZMutation: LegacyBooleanMutation,
    val sellerFundsDelta: Long,
    val buyerFundsDelta: Long,
    val secondarySellerCharge: Int,
    val contractEndMillisAfter: Long?,
    val contractDurationDays: Long,
    val clearSourcePrimarySlot: Boolean,
    val clearSourceSecondarySlot: Boolean,
    val setPlayerCrossActiveFlag: Boolean,
    val removeFromSourceRoster: Boolean,
    val addToDestinationRoster: Boolean,
    val resetSourceClubStateFlag: Boolean,
)

data class LegacySearchPurchaseExecution(
    val transferValue: Int,
    val salaryAfterPurchase: Int?,
)

object LegacyTransferExecutionRule {
    private const val DAY_MILLIS = 86_400_000L

    fun purchaseExecution(
        offeredTransferValue: Int,
        playerValue: Int,
        acceptedSalary: Int,
    ): LegacySearchPurchaseExecution = LegacySearchPurchaseExecution(
        transferValue = if (offeredTransferValue > 0) offeredTransferValue else playerValue,
        salaryAfterPurchase = acceptedSalary.takeIf { it > 0 },
    )

    fun plan(input: LegacyTransferExecutionInput): LegacyTransferExecutionPlan {
        val financeEnabled = !input.loanMove && !input.legacyNonFinancialMoveFlag
        val secondaryCharge = if (
            financeEnabled &&
            input.legacySecondaryChargeFlag &&
            input.playerContractEndMillisBefore > 0L &&
            input.currentCalendarMillis != null
        ) {
            calculateSecondaryCharge(
                transferValue = input.transferValue,
                remainingContractDays = remainingContractDays(
                    currentGameMillis = input.currentGameMillis,
                    contractEndMillis = input.playerContractEndMillisBefore,
                ),
            )
        } else {
            0
        }

        val sellerFundsDelta = if (
            financeEnabled &&
            input.transferValue > 0 &&
            input.sourceClubPresent &&
            input.sourceClubActive
        ) {
            input.transferValue.toLong() - secondaryCharge.toLong()
        } else {
            0L
        }

        val buyerFundsDelta = if (
            financeEnabled &&
            input.transferValue > 0 &&
            input.destinationClubActive
        ) {
            -input.transferValue.toLong()
        } else {
            0L
        }

        val durationDays = if (input.loanMove) 365L else 180L
        val contractEndAfter = input.currentCalendarMillis?.let { calendarMillis ->
            calendarMillis + (durationDays * DAY_MILLIS)
        }

        val rawYMutation = when {
            input.legacyNonFinancialMoveFlag -> LegacyBooleanMutation.SET_FALSE
            input.loanMove -> LegacyBooleanMutation.SET_TRUE
            else -> LegacyBooleanMutation.UNCHANGED
        }

        return LegacyTransferExecutionPlan(
            markMainTeamDirty = input.sourceClubActive || input.destinationClubActive,
            destinationClubId = input.destinationClubId,
            rawXMutation = LegacyBooleanMutation.SET_FALSE,
            rawYMutation = rawYMutation,
            rawZMutation = LegacyBooleanMutation.SET_FALSE,
            sellerFundsDelta = sellerFundsDelta,
            buyerFundsDelta = buyerFundsDelta,
            secondarySellerCharge = secondaryCharge,
            contractEndMillisAfter = contractEndAfter,
            contractDurationDays = durationDays,
            clearSourcePrimarySlot = input.sourceClubPresent && input.sourcePrimarySlotMatchesPlayer,
            clearSourceSecondarySlot = input.sourceClubPresent && input.sourceSecondarySlotMatchesPlayer,
            setPlayerCrossActiveFlag = input.sourceClubPresent && input.sourceClubActive && input.destinationClubActive,
            removeFromSourceRoster = input.sourceClubPresent,
            addToDestinationRoster = true,
            resetSourceClubStateFlag = input.sourceClubPresent && input.sourceClubActive && input.destinationClubActive,
        )
    }

    fun remainingContractDays(
        currentGameMillis: Long,
        contractEndMillis: Long,
    ): Int = if (contractEndMillis > currentGameMillis) {
        ((contractEndMillis - currentGameMillis) / DAY_MILLIS).toInt()
    } else {
        0
    }

    fun calculateSecondaryCharge(
        transferValue: Int,
        remainingContractDays: Int,
    ): Int {
        val multiplier = when {
            remainingContractDays <= 0 -> 0.0
            remainingContractDays <= 30 -> 0.12
            remainingContractDays <= 60 -> 0.20
            remainingContractDays <= 90 -> 0.22
            remainingContractDays <= 180 -> 0.25
            else -> 0.30
        }
        return Math.round(transferValue.toDouble() * multiplier).toInt()
    }
}

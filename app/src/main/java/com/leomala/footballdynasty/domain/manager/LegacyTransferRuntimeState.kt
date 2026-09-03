package com.leomala.footballdynasty.domain.manager

/**
 * Immutable manager-runtime projection for the state mutations proven by
 * `best.o.T1(...)` together with `best.c0.B/D/f/f1`.
 *
 * IDs/codes remain raw legacy integers. This layer deliberately does not assign
 * persistence, UI, transfer-window, or accounting semantics beyond the effects
 * directly visible in the recovered SMALI.
 */
data class LegacyTransferClubRuntimeState(
    val clubCode: Int,
    val active: Boolean,
    val funds: Long,
    val rosterPlayerCodes: List<Int>,
    val primarySlotPlayerCode: Int?,
    val secondarySlotPlayerCode: Int?,
    val rawStateFlag: Boolean,
)

data class LegacyTransferPlayerRuntimeState(
    val playerCode: Int,
    val clubCode: Int,
    val salaryCode: Int,
    val contractEndMillis: Long,
    val rawX: Boolean,
    val rawY: Boolean,
    val rawZ: Boolean,
    val rawCrossActiveFlag: Boolean,
    /** Raw `best.o.o`; meaning intentionally unassigned. */
    val rawOCode: Int = 0,
    /** Raw `best.o.D`; T1 always executes Q1(), which copies o into D. */
    val rawDCode: Int = 0,
)

data class LegacyTransferRuntimeState(
    val mainTeamDirty: Boolean,
    val player: LegacyTransferPlayerRuntimeState,
    val sourceClub: LegacyTransferClubRuntimeState?,
    val destinationClub: LegacyTransferClubRuntimeState,
)

object LegacyTransferRuntimeMutation {
    fun apply(
        state: LegacyTransferRuntimeState,
        plan: LegacyTransferExecutionPlan,
        salaryAfterPurchase: Int? = null,
    ): LegacyTransferRuntimeState {
        val playerCode = state.player.playerCode

        val sourceAfter = state.sourceClub?.let { source ->
            source.copy(
                funds = source.funds + plan.sellerFundsDelta,
                rosterPlayerCodes = if (plan.removeFromSourceRoster) {
                    source.rosterPlayerCodes.toMutableList().also { it.remove(playerCode) }
                } else {
                    source.rosterPlayerCodes
                },
                primarySlotPlayerCode = if (plan.clearSourcePrimarySlot) null else source.primarySlotPlayerCode,
                secondarySlotPlayerCode = if (plan.clearSourceSecondarySlot) null else source.secondarySlotPlayerCode,
                rawStateFlag = if (plan.resetSourceClubStateFlag) false else source.rawStateFlag,
            )
        }

        val destinationRoster = if (plan.addToDestinationRoster) {
            state.destinationClub.rosterPlayerCodes + playerCode
        } else {
            state.destinationClub.rosterPlayerCodes
        }
        val destinationAfter = state.destinationClub.copy(
            funds = state.destinationClub.funds + plan.buyerFundsDelta,
            rosterPlayerCodes = destinationRoster,
        )

        val playerAfter = state.player.copy(
            clubCode = plan.destinationClubId,
            salaryCode = salaryAfterPurchase ?: state.player.salaryCode,
            contractEndMillis = plan.contractEndMillisAfter ?: state.player.contractEndMillis,
            rawX = plan.rawXMutation.applyTo(state.player.rawX),
            rawY = plan.rawYMutation.applyTo(state.player.rawY),
            rawZ = plan.rawZMutation.applyTo(state.player.rawZ),
            rawCrossActiveFlag = state.player.rawCrossActiveFlag || plan.setPlayerCrossActiveFlag,
            rawDCode = state.player.rawOCode,
        )

        return LegacyTransferRuntimeState(
            mainTeamDirty = state.mainTeamDirty || plan.markMainTeamDirty,
            player = playerAfter,
            sourceClub = sourceAfter,
            destinationClub = destinationAfter,
        )
    }

    private fun LegacyBooleanMutation.applyTo(current: Boolean): Boolean = when (this) {
        LegacyBooleanMutation.UNCHANGED -> current
        LegacyBooleanMutation.SET_FALSE -> false
        LegacyBooleanMutation.SET_TRUE -> true
    }
}

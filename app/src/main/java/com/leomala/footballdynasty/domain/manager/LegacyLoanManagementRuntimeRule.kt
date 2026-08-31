package com.leomala.footballdynasty.domain.manager

/**
 * Executable composition of the already-characterized `DialogIgrokInfo.q()/i()/h()` loan
 * management path.
 *
 * This runtime deliberately keeps the legacy `M()` list flag independent from an active loan.
 * It also does not guess player identity or a destination club: the compatibility layer must
 * provide the first identity-match index used by `h()`, and the recovered `U1(sourceClub)`
 * invocation is exposed as an effect for the caller to apply at the real player/club boundary.
 */
data class LegacyLoanManagementRuntimeState(
    val listedForLoan: Boolean,
    val loanRecords: List<LegacyLoanRecord>,
    val mainTeamDirty: Boolean = false,
)

data class LegacyLoanManagementRuntimeResult(
    val decision: LegacyLoanMenuDecision,
    val listMutation: LegacyLoanListMutation,
    val earlyReturnPlan: LegacyEarlyLoanReturnPlan?,
    val invokeReturnMove: Boolean,
    val returnMoveSourceClubCode: Int?,
    val state: LegacyLoanManagementRuntimeState,
)

object LegacyLoanManagementRuntimeRule {
    fun execute(
        state: LegacyLoanManagementRuntimeState,
        currentlyOnLoan: Boolean,
        firstClubLoanCount: Int,
        secondClubLoanCount: Int,
        firstIdentityMatchIndex: Int?,
    ): LegacyLoanManagementRuntimeResult {
        val decision = LegacyLoanManagementRule.menuDecision(
            currentlyOnLoan = currentlyOnLoan,
            listedForLoan = state.listedForLoan,
            firstClubLoanCount = firstClubLoanCount,
            secondClubLoanCount = secondClubLoanCount,
        )
        val actionCode = decision.actionCode

        if (actionCode == null) {
            return LegacyLoanManagementRuntimeResult(
                decision = decision,
                listMutation = LegacyLoanListMutation.UNCHANGED,
                earlyReturnPlan = null,
                invokeReturnMove = false,
                returnMoveSourceClubCode = null,
                state = state,
            )
        }

        if (actionCode == LegacyLoanManagementRule.ACTION_CANCEL_CURRENT_LOAN) {
            val plan = LegacyLoanManagementRule.earlyReturnPlan(
                records = state.loanRecords,
                firstIdentityMatchIndex = firstIdentityMatchIndex,
            )
            val nextState = state.copy(
                loanRecords = LegacyLoanManagementRule.removeMatchingRecord(state.loanRecords, plan),
                mainTeamDirty = state.mainTeamDirty || plan.markMainTeamDirty,
            )
            return LegacyLoanManagementRuntimeResult(
                decision = decision,
                listMutation = LegacyLoanListMutation.UNCHANGED,
                earlyReturnPlan = plan,
                invokeReturnMove = plan.invokeReturnMove,
                returnMoveSourceClubCode = plan.storedSourceClubCode,
                state = nextState,
            )
        }

        val mutation = LegacyLoanManagementRule.listMutation(actionCode)
        val nextListedForLoan = when (mutation) {
            LegacyLoanListMutation.SET_FALSE -> false
            LegacyLoanListMutation.SET_TRUE -> true
            LegacyLoanListMutation.UNCHANGED -> state.listedForLoan
        }
        return LegacyLoanManagementRuntimeResult(
            decision = decision,
            listMutation = mutation,
            earlyReturnPlan = null,
            invokeReturnMove = false,
            returnMoveSourceClubCode = null,
            state = state.copy(listedForLoan = nextListedForLoan),
        )
    }
}

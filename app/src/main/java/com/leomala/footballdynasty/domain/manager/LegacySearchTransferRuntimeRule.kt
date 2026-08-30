package com.leomala.footballdynasty.domain.manager

/**
 * Executable composition of the already-characterized `ActivityProcura.u(int)`
 * dispatch with the purchase/loan wrappers and `best.o.T1(...)` runtime mutation.
 *
 * This layer deliberately adds no transfer policy: it only wires the proven
 * legacy action code to the independently reconstructed execution plan and state
 * mutation. Unknown action codes remain a no-op.
 */
data class LegacySearchTransferRuntimeResult(
    val action: LegacySearchTransferAction,
    val executionPlan: LegacyTransferExecutionPlan?,
    val state: LegacyTransferRuntimeState,
)

object LegacySearchTransferRuntimeRule {
    fun execute(
        state: LegacyTransferRuntimeState,
        legacyActionCode: Int,
        suggestedSalaryCode: Int,
        storedCounterOfferValue: Int,
        playerValue: Int,
        baseInput: LegacyTransferExecutionInput,
    ): LegacySearchTransferRuntimeResult {
        val action = LegacySearchTransferActionRule.resolve(
            legacyActionCode = legacyActionCode,
            suggestedSalaryCode = suggestedSalaryCode,
            storedCounterOfferValue = storedCounterOfferValue,
        )

        return when (action) {
            LegacySearchTransferAction.None -> LegacySearchTransferRuntimeResult(
                action = action,
                executionPlan = null,
                state = state,
            )

            LegacySearchTransferAction.Loan -> {
                val plan = LegacyTransferExecutionRule.plan(
                    baseInput.copy(
                        loanMove = true,
                        legacyNonFinancialMoveFlag = false,
                    ),
                )
                LegacySearchTransferRuntimeResult(
                    action = action,
                    executionPlan = plan,
                    state = LegacyTransferRuntimeMutation.apply(state, plan),
                )
            }

            else -> {
                val purchase = requireNotNull(
                    LegacySearchTransferActionRule.purchaseExecution(
                        action = action,
                        playerValue = playerValue,
                    ),
                )
                val plan = LegacyTransferExecutionRule.plan(
                    baseInput.copy(
                        transferValue = purchase.transferValue,
                        loanMove = false,
                        legacyNonFinancialMoveFlag = false,
                    ),
                )
                LegacySearchTransferRuntimeResult(
                    action = action,
                    executionPlan = plan,
                    state = LegacyTransferRuntimeMutation.apply(
                        state = state,
                        plan = plan,
                        salaryAfterPurchase = purchase.salaryAfterPurchase,
                    ),
                )
            }
        }
    }
}

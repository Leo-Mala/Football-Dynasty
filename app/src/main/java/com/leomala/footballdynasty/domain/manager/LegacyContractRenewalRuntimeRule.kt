package com.leomala.footballdynasty.domain.manager

/**
 * Runtime composition for the contract-renewal path characterized from
 * `DialogIgrokInfo.s/f/e/l`.
 *
 * This intentionally stops at the exact legacy calls that are proven. In
 * particular, `p.c(days, false)` is represented as an invocation instead of
 * guessing how that legacy method stores/calculates the resulting end date.
 */
data class LegacyContractRenewalRuntimeState(
    val salaryCode: Int,
    val rawSaleFlag: Boolean,
    /** Raw `best.o.o`; semantic meaning remains intentionally unassigned. */
    val rawOCode: Int,
    /** Raw `best.o.D`; accepted renewal executes Q1(), copying o into D. */
    val rawDCode: Int,
    val mainTeamDirty: Boolean,
)

data class LegacyContractWriteInvocation(
    val durationDays: Int,
    val booleanArgument: Boolean,
)

data class LegacyContractRenewalRuntimeResult(
    val decision: LegacyContractRenewalDecision,
    val state: LegacyContractRenewalRuntimeState,
    val contractWrite: LegacyContractWriteInvocation?,
)

object LegacyContractRenewalRuntimeRule {
    fun execute(
        state: LegacyContractRenewalRuntimeState,
        remainingContractDays: Int,
        termIndex: Int,
        offeredSalary: Int,
    ): LegacyContractRenewalRuntimeResult {
        val decision = LegacyContractRenewalRule.evaluate(
            currentSalary = state.salaryCode,
            remainingContractDays = remainingContractDays,
            termIndex = termIndex,
            offeredSalary = offeredSalary,
        )

        if (decision.result != LegacyContractOfferResult.ACCEPTED) {
            return LegacyContractRenewalRuntimeResult(
                decision = decision,
                state = state,
                contractWrite = null,
            )
        }

        val plan = LegacyContractRenewalRule.acceptedApplyPlan(
            offeredSalary = offeredSalary,
            termIndex = termIndex,
        )
        check(plan.clearRawSaleFlag)
        check(plan.copyRawOToD)
        check(plan.markMainTeamDirty)

        return LegacyContractRenewalRuntimeResult(
            decision = decision,
            state = state.copy(
                salaryCode = plan.newSalary,
                rawSaleFlag = false,
                rawDCode = state.rawOCode,
                mainTeamDirty = true,
            ),
            contractWrite = LegacyContractWriteInvocation(
                durationDays = plan.durationDays,
                booleanArgument = plan.contractBooleanArgument,
            ),
        )
    }
}

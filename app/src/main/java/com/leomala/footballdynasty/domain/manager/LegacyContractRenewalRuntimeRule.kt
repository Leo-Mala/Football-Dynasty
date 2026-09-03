package com.leomala.footballdynasty.domain.manager

/**
 * Runtime composition for the contract-renewal path characterized from
 * `DialogIgrokInfo.s/f/e/l`.
 *
 * Accepted renewals preserve the exact `p.c(days, false)` invocation. The previously fail-closed
 * date-write body is now separately reconstructed by [LegacyContractDateWriteRule] from official
 * Java + SMALI (`best.o.c(long, boolean)`, 25 instructions / 2 branches), so persistence layers can
 * compose the invocation without guessing calendar semantics.
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

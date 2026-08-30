package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of `DialogIgrokInfo.s/f/e/l` for contract renewal.
 *
 * The legacy order is preserved:
 * - salary input must be positive;
 * - the UI rejects values above current salary * 5;
 * - the minimum accepted salary depends on remaining contract days and the
 *   selected term index;
 * - fewer than 60 remaining days uses the discount percentages [1,3,5,12];
 * - 60 or more days uses the uplift percentages [10,12,15,5];
 * - accepted terms are exactly [180,365,730,1095] days;
 * - applying an accepted renewal clears the raw sale flag through b1(FALSE),
 *   executes Q1 (raw o -> D copy), writes the new contract with c(days,false),
 *   writes salary through F1, and marks ActivityMainTeam.F dirty.
 *
 * Integer multiplication/division deliberately uses Int operations to preserve
 * JVM overflow and truncation behavior before the legacy Math.round(float).
 */
enum class LegacyContractOfferResult {
    INVALID_NON_POSITIVE,
    ABOVE_SALARY_LIMIT,
    COUNTER_REQUIRED,
    ACCEPTED,
}

data class LegacyContractRenewalDecision(
    val result: LegacyContractOfferResult,
    val requiredSalary: Int?,
    val maximumSalary: Int,
    val durationDays: Int,
)

data class LegacyContractRenewalApplyPlan(
    val newSalary: Int,
    val durationDays: Int,
    val clearRawSaleFlag: Boolean,
    val copyRawOToD: Boolean,
    val contractBooleanArgument: Boolean,
    val markMainTeamDirty: Boolean,
)

object LegacyContractRenewalRule {
    private val discountPercentages = intArrayOf(1, 3, 5, 12)
    private val upliftPercentages = intArrayOf(10, 12, 15, 5)
    private val durationDays = intArrayOf(180, 365, 730, 1095)

    const val EXPIRING_CONTRACT_THRESHOLD_DAYS: Int = 60
    const val MAX_SALARY_MULTIPLIER: Int = 5

    fun minimumAcceptedSalary(
        currentSalary: Int,
        remainingContractDays: Int,
        termIndex: Int,
    ): Int {
        require(termIndex in durationDays.indices)
        val percentages = if (remainingContractDays < EXPIRING_CONTRACT_THRESHOLD_DAYS) {
            discountPercentages
        } else {
            upliftPercentages
        }
        val delta = currentSalary * percentages[termIndex] / 100
        return if (remainingContractDays < EXPIRING_CONTRACT_THRESHOLD_DAYS) {
            currentSalary - delta
        } else {
            currentSalary + delta
        }
    }

    fun evaluate(
        currentSalary: Int,
        remainingContractDays: Int,
        termIndex: Int,
        offeredSalary: Int,
    ): LegacyContractRenewalDecision {
        require(termIndex in durationDays.indices)
        val maxSalary = currentSalary * MAX_SALARY_MULTIPLIER
        val duration = durationDays[termIndex]

        if (offeredSalary <= 0) {
            return LegacyContractRenewalDecision(
                result = LegacyContractOfferResult.INVALID_NON_POSITIVE,
                requiredSalary = null,
                maximumSalary = maxSalary,
                durationDays = duration,
            )
        }

        if (offeredSalary > maxSalary) {
            return LegacyContractRenewalDecision(
                result = LegacyContractOfferResult.ABOVE_SALARY_LIMIT,
                requiredSalary = null,
                maximumSalary = maxSalary,
                durationDays = duration,
            )
        }

        val required = minimumAcceptedSalary(currentSalary, remainingContractDays, termIndex)
        return LegacyContractRenewalDecision(
            result = if (offeredSalary < required) {
                LegacyContractOfferResult.COUNTER_REQUIRED
            } else {
                LegacyContractOfferResult.ACCEPTED
            },
            requiredSalary = required,
            maximumSalary = maxSalary,
            durationDays = duration,
        )
    }

    fun acceptedApplyPlan(
        offeredSalary: Int,
        termIndex: Int,
    ): LegacyContractRenewalApplyPlan {
        require(termIndex in durationDays.indices)
        return LegacyContractRenewalApplyPlan(
            newSalary = offeredSalary,
            durationDays = durationDays[termIndex],
            clearRawSaleFlag = true,
            copyRawOToD = true,
            contractBooleanArgument = false,
            markMainTeamDirty = true,
        )
    }
}

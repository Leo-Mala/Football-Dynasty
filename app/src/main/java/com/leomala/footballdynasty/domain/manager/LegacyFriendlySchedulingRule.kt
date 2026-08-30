package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the reachable friendly-match negotiation in legacy
 * `konrent.a.V()/Z()` and its confirmed `ActivityAmistosos2021` caller.
 *
 * The integer result codes and club-level fields intentionally remain raw: the
 * corpus proves their control-flow meaning at this call site, but this rule does
 * not rename the underlying legacy sporting classification.
 */
data class LegacyFriendlySchedulingResult(
    /** Exact return code from legacy `konrent.a.V()`: 0..3. */
    val rawDecisionCode: Int,
    /** Exact value stored in legacy static `konrent.a.o` and exposed by `Z()`. */
    val requestedPayment: Int,
) {
    val refused: Boolean get() = rawDecisionCode == LegacyFriendlySchedulingRule.REFUSED
    val scheduleWithoutPayment: Boolean get() = rawDecisionCode == LegacyFriendlySchedulingRule.SCHEDULE_DIRECTLY
    val alreadyScheduled: Boolean get() = rawDecisionCode == LegacyFriendlySchedulingRule.ALREADY_SCHEDULED
    val requiresPaymentConfirmation: Boolean get() = rawDecisionCode == LegacyFriendlySchedulingRule.REQUIRES_PAYMENT
}

data class LegacyFriendlySchedulingPayment(
    val cashBefore: Long,
    val cashAfter: Long,
    val amount: Int,
    /** `ActivityAmistosos2021.d()` passes exactly -1 to `c0.D`. */
    val rawCashCategoryCode: Int,
)

object LegacyFriendlySchedulingRule {
    const val REFUSED: Int = 0
    const val SCHEDULE_DIRECTLY: Int = 1
    const val ALREADY_SCHEDULED: Int = 2
    const val REQUIRES_PAYMENT: Int = 3
    const val RAW_CASH_CATEGORY_CODE: Int = -1

    private val sideZeroSameGroup = intArrayOf(0, 0, 10_000, 50_000, 150_000, 250_000)
    private val sideZeroOtherGroup = intArrayOf(0, 0, 20_000, 80_000, 200_000, 450_000)
    private val sideNonZeroOtherGroup = intArrayOf(0, 0, 30_000, 100_000, 250_000, 500_000)

    /**
     * Reconstructs `konrent.a.V(requestingClub, opponent, rawSideCode, dateIndex)`.
     *
     * `opponentAlreadyScheduledOnDate` represents the result of legacy `b0` for
     * the selected date. `opponentControlled` is the exact `opponent.Q0()` branch.
     * Array indexing is deliberately not normalized: invalid raw level codes fail
     * just as the legacy array lookup would.
     */
    fun evaluate(
        requesterLevelCode: Int,
        opponentLevelCode: Int,
        sameRawGroupCode: Boolean,
        rawSideCode: Int,
        opponentAlreadyScheduledOnDate: Boolean,
        opponentControlled: Boolean,
    ): LegacyFriendlySchedulingResult {
        if (opponentAlreadyScheduledOnDate) {
            return LegacyFriendlySchedulingResult(ALREADY_SCHEDULED, 0)
        }
        if (opponentControlled) {
            return LegacyFriendlySchedulingResult(SCHEDULE_DIRECTLY, 0)
        }

        if (rawSideCode == 0) {
            // Preserve the exact legacy ordering. The following `< 3` refusal for
            // level 5 and `<= 1` refusal for level 4 are shadowed by these first
            // branches and are therefore intentionally unreachable.
            if (opponentLevelCode == 5 && requesterLevelCode < 4) {
                return LegacyFriendlySchedulingResult(REQUIRES_PAYMENT, 300_000)
            }
            if (opponentLevelCode == 5 && requesterLevelCode < 3) {
                return LegacyFriendlySchedulingResult(REFUSED, 0)
            }
            if (opponentLevelCode == 4 && requesterLevelCode < 2) {
                return LegacyFriendlySchedulingResult(REQUIRES_PAYMENT, 200_000)
            }
            if (opponentLevelCode == 4 && requesterLevelCode <= 1) {
                return LegacyFriendlySchedulingResult(REFUSED, 0)
            }

            val payment = if (sameRawGroupCode) {
                sideZeroSameGroup[opponentLevelCode]
            } else {
                sideZeroOtherGroup[opponentLevelCode]
            }
            return decisionFor(payment)
        }

        if (opponentLevelCode == 5 && requesterLevelCode != 5) {
            return LegacyFriendlySchedulingResult(REFUSED, 0)
        }
        if (opponentLevelCode == 4 && requesterLevelCode < 2) {
            return LegacyFriendlySchedulingResult(REFUSED, 0)
        }

        val payment = if (sameRawGroupCode) {
            sideZeroOtherGroup[opponentLevelCode]
        } else {
            sideNonZeroOtherGroup[opponentLevelCode]
        }
        return decisionFor(payment)
    }

    /**
     * Reconstructs the confirmed paid path in `ActivityAmistosos2021.d()` after
     * the user accepts decision code 3. The legacy caller does not perform a cash
     * sufficiency check before `c0.D`, so negative balances remain possible.
     */
    fun acceptRequestedPayment(cash: Long, result: LegacyFriendlySchedulingResult): LegacyFriendlySchedulingPayment? {
        if (!result.requiresPaymentConfirmation) return null
        return LegacyFriendlySchedulingPayment(
            cashBefore = cash,
            cashAfter = cash - result.requestedPayment.toLong(),
            amount = result.requestedPayment,
            rawCashCategoryCode = RAW_CASH_CATEGORY_CODE,
        )
    }

    private fun decisionFor(payment: Int): LegacyFriendlySchedulingResult =
        LegacyFriendlySchedulingResult(
            rawDecisionCode = if (payment > 0) REQUIRES_PAYMENT else SCHEDULE_DIRECTLY,
            requestedPayment = payment,
        )
}

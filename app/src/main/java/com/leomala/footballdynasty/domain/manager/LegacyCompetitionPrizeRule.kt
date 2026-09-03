package com.leomala.footballdynasty.domain.manager

/**
 * Pure reconstruction of the prize amount and winner-credit path in legacy `konrent.f0`.
 *
 * Official Java + SMALI evidence:
 * - `konrent.f0.e(...)` stores raw competition type `i5`, stage `i2`, then executes
 *   `y(l(type, stage))` before creating the round matches;
 * - private `konrent.f0.l(int,int)` selects the exact static prize table below;
 * - `konrent.f0.d(boolean,boolean)` resolves each pairing winner and calls
 *   `winner.B(prize, 3)` only when `prize > 0` and the winner's legacy `Q0()` flag is true;
 * - `best.c0.B` credits cash and routes category `3` to `best.m.a(...)`;
 * - `best.m.a(..., 3)` is the period prize-income accumulator.
 *
 * Bounds intentionally mirror the bytecode rather than being normalized. In particular, type `6`
 * checks the OUTER `U1` array length (`2`) before indexing the selected row, so only stage 0 or 1
 * reaches a prize even though each recovered row contains six values.
 */
object LegacyCompetitionPrizeRule {
    const val RAW_INCOME_CATEGORY_CODE: Int = LegacyFinanceLedgerRule.INCOME_PRIZE

    private val q1 = intArrayOf(
        10_000,
        10_000,
        100_000,
        200_000,
        500_000,
        2_000_000,
        3_500_000,
    )

    private val s1 = arrayOf(
        intArrayOf(500_000, 1_000_000, 3_000_000, 7_000_000, 0, 0),
        intArrayOf(500_000, 1_000_000, 2_500_000, 5_000_000, 0, 0),
        intArrayOf(200_000, 500_000, 1_000_000, 2_000_000, 0, 0),
        intArrayOf(200_000, 500_000, 1_000_000, 2_000_000, 0, 0),
        intArrayOf(200_000, 500_000, 1_000_000, 2_000_000, 0, 0),
        intArrayOf(200_000, 500_000, 1_000_000, 2_000_000, 0, 0),
    )

    private val t1 = intArrayOf(2_000_000, 5_000_000, 5_000_000)

    private val u1 = arrayOf(
        intArrayOf(100_000, 200_000, 500_000, 1_000_000, 3_000_000, 0),
        intArrayOf(100_000, 200_000, 500_000, 1_000_000, 2_000_000, 0),
    )

    private val v1 = intArrayOf(1_000_000, 500_000)

    /** Mirrors private legacy `konrent.f0.l(type, stage)` exactly. */
    fun prizeAmount(
        rawCompetitionType: Int,
        rawStageIndex: Int,
        rawCompetitionI0: Int,
        rawCompetitionPCode: Int,
    ): Int = when (rawCompetitionType) {
        2 -> {
            val index = rawStageIndex + (q1.lastIndex - rawCompetitionI0)
            if (index < q1.size) q1[index] else 0
        }

        4 -> {
            val row = if (rawCompetitionPCode >= 0) rawCompetitionPCode else 1
            if (rawStageIndex < s1.size) s1[row][rawStageIndex] else 0
        }

        5 -> if (rawStageIndex < t1.size) t1[rawStageIndex] else 0

        6 -> {
            val row = if (rawCompetitionPCode in 0..1) rawCompetitionPCode else 1
            // Legacy checks `array-length U1` (2), not the selected inner row length (6).
            if (rawStageIndex < u1.size) u1[row][rawStageIndex] else 0
        }

        8 -> v1[0]
        else -> 0
    }

    /** Mirrors the winner-only `prize > 0 && winner.Q0() -> winner.B(prize, 3)` mutation. */
    fun applyWinnerPrize(
        state: LegacyFinanceRuntimeState,
        prizeAmount: Int,
        winnerLegacyQ0: Boolean,
    ): LegacyFinanceRuntimeState {
        if (prizeAmount <= 0 || !winnerLegacyQ0) return state
        return LegacyFinanceRuntimeState(
            cash = state.cash + prizeAmount.toLong(),
            ledger = LegacyFinanceLedgerRule.addIncome(
                state = state.ledger,
                amount = prizeAmount,
                rawCategoryCode = RAW_INCOME_CATEGORY_CODE,
            ),
        )
    }
}

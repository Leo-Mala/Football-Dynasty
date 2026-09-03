package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import kotlin.math.abs
import kotlin.math.roundToInt

/** Exact four-sector gate/attendance calculation reconstructed from legacy `best.k.b(best.s)`. */
data class LegacyTicketCalculationInput(
    val capacities: List<Int>,
    val rawCompetitionType: Int,
    val homeRawO: Int,
    val homeRawP0: Int,
    val awayRawP0: Int,
    val homeRawJ: Int,
    val homeRegionalPercent: Int?,
    val rawCompetitionAIsKonrentA0: Boolean,
)

data class LegacyTicketCalculationResult(
    val attendanceBySector: List<Int>,
    val priceBySector: List<Int>,
    val grossTicketIncome: Int,
)

object LegacyTicketFinanceRule {
    const val RAW_INCOME_CATEGORY_CODE: Int = LegacyFinanceLedgerRule.INCOME_TICKET

    private val attendanceBase = arrayOf(
        intArrayOf(200, 500, 50, 0),
        intArrayOf(1_000, 5_000, 1_200, 20),
        intArrayOf(2_000, 10_000, 1_500, 50),
        intArrayOf(4_000, 20_000, 2_500, 300),
        intArrayOf(4_500, 30_000, 3_500, 400),
        intArrayOf(5_000, 40_000, 5_500, 500),
    )

    private val randomBounds = arrayOf(
        intArrayOf(10, 20, 5, 0),
        intArrayOf(100, 500, 200, 10),
        intArrayOf(300, 1_000, 400, 20),
        intArrayOf(400, 1_200, 500, 30),
        intArrayOf(500, 1_500, 1_000, 50),
        intArrayOf(500, 1_500, 1_000, 50),
    )

    // Exact `best.j0.z0` rows used by `best.k.b`.
    private val prices = arrayOf(
        arrayOf(
            intArrayOf(5, 15, 20, 30), intArrayOf(3, 12, 15, 25),
            intArrayOf(3, 12, 15, 25), intArrayOf(3, 12, 15, 25),
            intArrayOf(3, 12, 15, 25), intArrayOf(3, 12, 15, 25),
        ),
        arrayOf(
            intArrayOf(3, 12, 15, 30), intArrayOf(10, 15, 25, 80),
            intArrayOf(7, 13, 20, 70), intArrayOf(5, 12, 17, 40),
            intArrayOf(3, 12, 15, 30), intArrayOf(3, 12, 15, 30),
        ),
        arrayOf(
            intArrayOf(3, 12, 15, 30), intArrayOf(3, 12, 15, 30),
            intArrayOf(3, 12, 15, 30), intArrayOf(7, 13, 20, 70),
            intArrayOf(10, 15, 25, 80), intArrayOf(10, 15, 25, 80),
            intArrayOf(10, 15, 25, 80),
        ),
        arrayOf(
            intArrayOf(3, 5, 12, 20), intArrayOf(3, 12, 15, 30),
            intArrayOf(3, 12, 15, 30), intArrayOf(5, 12, 20, 50),
            intArrayOf(10, 15, 25, 70), intArrayOf(10, 15, 25, 70),
        ),
        arrayOf(intArrayOf(30, 45, 65, 200), intArrayOf(20, 35, 55, 150)),
        arrayOf(intArrayOf(0, 0, 0, 0)),
        arrayOf(intArrayOf(20, 25, 45, 150), intArrayOf(20, 25, 40, 120)),
        arrayOf(intArrayOf(0, 0, 0, 0)),
        arrayOf(intArrayOf(20, 25, 45, 150), intArrayOf(20, 25, 40, 120)),
    )

    fun calculate(input: LegacyTicketCalculationInput, random: RandomSource): LegacyTicketCalculationResult {
        require(input.capacities.size == 4)
        val capacities = input.capacities.map { if (it < 0) 0 else it }
        val homeP0ForAttendance = input.homeRawP0.takeIf { it in 0..5 } ?: 3
        val attendance = attendanceBase[homeP0ForAttendance].copyOf()

        when (input.rawCompetitionType) {
            3 -> attendance.indices.forEach { attendance[it] = Math.round(attendance[it] * 0.7).toInt() }
            0 -> attendance.indices.forEach { attendance[it] = Math.round(attendance[it] * 0.4).toInt() }
        }

        var stadiumShare = if (input.rawCompetitionAIsKonrentA0) 0.45 else 0.30
        if (input.rawCompetitionType == 4) stadiumShare += 0.30
        else if (input.rawCompetitionType == 6 || input.rawCompetitionType == 8) stadiumShare += 0.15
        attendance.indices.forEach {
            attendance[it] += Math.round(capacities[it] * stadiumShare).toInt()
        }

        val rawGap = input.awayRawP0 - input.homeRawP0
        val gapRate = doubleArrayOf(0.0, 0.05, 0.10, 0.15, 0.20, 0.25)[abs(rawGap)]
        if (rawGap > 0) attendance.indices.forEach {
            attendance[it] += Math.round(attendance[it] * gapRate).toInt()
        } else if (rawGap < 0) attendance.indices.forEach {
            attendance[it] -= Math.round(attendance[it] * gapRate).toInt()
        }

        val regionalMultiplier = (input.homeRegionalPercent ?: 80) / 100.0
        attendance.indices.forEach {
            attendance[it] = Math.round(attendance[it] * regionalMultiplier).toInt()
        }

        val homeOForRandom = input.homeRawO.takeIf { it in 0..4 } ?: 3
        attendance.indices.forEach { sector ->
            val bound = randomBounds[homeOForRandom][sector]
            if (bound > 0) attendance[sector] += random.nextInt(bound)
        }

        val selectedPrices = priceRow(input).toList()
        var income = 0
        attendance.indices.forEach { sector ->
            if (attendance[sector] < 0) attendance[sector] = 0
            if (attendance[sector] > capacities[sector]) attendance[sector] = capacities[sector]
            income += attendance[sector] * selectedPrices[sector]
        }
        return LegacyTicketCalculationResult(attendance.toList(), selectedPrices, income)
    }

    /** Exact `best.s.h()` credit gate after `best.k.b` has written `best.s.l`. */
    fun applyHomeTicketIncome(
        state: LegacyFinanceRuntimeState,
        rawCompetitionType: Int,
        homeLegacyQ0: Boolean,
        grossTicketIncome: Int,
    ): LegacyFinanceRuntimeState {
        if (rawCompetitionType == 7 || rawCompetitionType == 5 || !homeLegacyQ0) return state
        return LegacyFinanceRuntimeState(
            cash = state.cash + grossTicketIncome.toLong(),
            ledger = LegacyFinanceLedgerRule.addIncome(
                state = state.ledger,
                amount = grossTicketIncome,
                rawCategoryCode = RAW_INCOME_CATEGORY_CODE,
            ),
        )
    }

    private fun priceRow(input: LegacyTicketCalculationInput): IntArray {
        val default = intArrayOf(10, 25, 35, 50)
        val homeO = input.homeRawO.takeIf { it in 0..4 } ?: 3
        val homeP0 = input.homeRawP0.takeIf { it in 0..5 } ?: 0
        val cappedJ = if (input.homeRawJ > 1) 1 else input.homeRawJ
        return when (input.rawCompetitionType) {
            1 -> prices[1][homeO]
            2, 3 -> prices[input.rawCompetitionType][homeP0]
            4, 6, 8 -> prices[input.rawCompetitionType][cappedJ]
            5, 7 -> prices[1][homeO]
            else -> default
        }
    }
}

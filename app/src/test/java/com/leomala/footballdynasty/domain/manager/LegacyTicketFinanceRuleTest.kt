package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyTicketFinanceRuleTest {
    @Test
    fun `four sector calculation preserves draw order clamp and price multiplication`() {
        val random = QueueRandom(9, 19, 4)
        val result = LegacyTicketFinanceRule.calculate(
            LegacyTicketCalculationInput(
                capacities = listOf(1_000, 5_000, 1_200, 20),
                rawCompetitionType = 1,
                homeRawO = 0,
                homeRawP0 = 1,
                awayRawP0 = 1,
                homeRawJ = 0,
                homeRegionalPercent = null,
                rawCompetitionAIsKonrentA0 = false,
            ),
            random,
        )
        assertEquals(3L, random.draws) // sector 3 has bound zero for raw O=0
        assertEquals(listOf(1_000, 5_000, 1_200, 20), result.attendanceBySector)
        assertEquals(listOf(3, 12, 15, 30), result.priceBySector)
        assertEquals(81_600, result.grossTicketIncome)
    }

    @Test
    fun `competition five and seven calculate but do not credit ticket income`() {
        val before = LegacyFinanceRuntimeState(100L, LegacyFinanceLedgerState(ticketIncome = 7))
        for (type in listOf(5, 7)) {
            assertEquals(before, LegacyTicketFinanceRule.applyHomeTicketIncome(before, type, true, 500))
        }
    }

    @Test
    fun `eligible home credits cash and category five ledger`() {
        val before = LegacyFinanceRuntimeState(100L, LegacyFinanceLedgerState(ticketIncome = 7))
        val after = LegacyTicketFinanceRule.applyHomeTicketIncome(before, 1, true, 500)
        assertEquals(600L, after.cash)
        assertEquals(507, after.ledger.ticketIncome)
    }

    private class QueueRandom(private vararg val values: Int) : RandomSource {
        private var index = 0
        override var draws: Long = 0
            private set
        override fun nextInt(bound: Int): Int {
            val value = values[index++]
            require(value in 0 until bound)
            draws++
            return value
        }
        override fun nextBoolean(): Boolean = error("unused")
        override fun nextDouble(): Double = error("unused")
    }
}

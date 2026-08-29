package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LegacyPlayerCommercialStateTest {
    @Test
    fun `preserves all proven commercial fields without interpreting raw codes`() {
        val state = LegacyPlayerCommercialState.fromRaw(
            salario = -17,
            rcClause = 0,
            rcRenewYear = 2031,
            rcConvYear = -1,
            pendSaleClub = -9,
            pendSaleValue = 0,
            pendIsLoan = false,
        )

        assertEquals(-17, state.contract.salaryCode)
        assertEquals(0, state.contract.clauseCode)
        assertEquals(2031, state.contract.renewalYearCode)
        assertEquals(-1, state.contract.conversionYearCode)
        assertEquals(-9, state.pendingMovement.clubCode)
        assertEquals(0, state.pendingMovement.valueCode)
        assertFalse(state.pendingMovement.loanFlag)
    }
}

package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
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

    @Test
    fun `replacing raw contract slice preserves the exact pending movement object`() {
        val original = LegacyPlayerCommercialState.fromRaw(
            salario = 1,
            rcClause = 2,
            rcRenewYear = 3,
            rcConvYear = 4,
            pendSaleClub = Int.MIN_VALUE,
            pendSaleValue = Int.MAX_VALUE,
            pendIsLoan = true,
        )

        val updated = original.withRawContractFields(
            salario = Int.MIN_VALUE,
            rcClause = Int.MAX_VALUE,
            rcRenewYear = -101,
            rcConvYear = 0,
        )

        assertSame(original.pendingMovement, updated.pendingMovement)
        assertEquals(Int.MIN_VALUE, updated.contract.salaryCode)
        assertEquals(Int.MAX_VALUE, updated.contract.clauseCode)
        assertEquals(-101, updated.contract.renewalYearCode)
        assertEquals(0, updated.contract.conversionYearCode)
        assertEquals(Int.MIN_VALUE, updated.pendingMovement.clubCode)
        assertEquals(Int.MAX_VALUE, updated.pendingMovement.valueCode)
        assertTrue(updated.pendingMovement.loanFlag)
    }

    @Test
    fun `replacing raw pending movement slice preserves the exact contract object`() {
        val original = LegacyPlayerCommercialState.fromRaw(
            salario = Int.MIN_VALUE,
            rcClause = Int.MAX_VALUE,
            rcRenewYear = -7,
            rcConvYear = 0,
            pendSaleClub = 11,
            pendSaleValue = 22,
            pendIsLoan = false,
        )

        val updated = original.withRawPendingMovementFields(
            pendSaleClub = Int.MAX_VALUE,
            pendSaleValue = Int.MIN_VALUE,
            pendIsLoan = true,
        )

        assertSame(original.contract, updated.contract)
        assertEquals(Int.MIN_VALUE, updated.contract.salaryCode)
        assertEquals(Int.MAX_VALUE, updated.contract.clauseCode)
        assertEquals(-7, updated.contract.renewalYearCode)
        assertEquals(0, updated.contract.conversionYearCode)
        assertEquals(Int.MAX_VALUE, updated.pendingMovement.clubCode)
        assertEquals(Int.MIN_VALUE, updated.pendingMovement.valueCode)
        assertTrue(updated.pendingMovement.loanFlag)
    }
}

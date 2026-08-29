package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerPlayerCommercialProjectionTest {
    @Test
    fun `preserves the confirmed legacy commercial snapshot exactly`() {
        val snapshot = LegacyCareerPlayerCommercialSnapshot(
            salario = 17,
            rcClause = 23,
            rcRenewYear = 2031,
            rcConvYear = 2032,
            pendSaleClub = 9,
            pendSaleValue = 101,
            pendIsLoan = true,
        )

        val state = LegacyCareerPlayerCommercialProjection.toDomain(snapshot)

        assertEquals(17, state.contract.salaryCode)
        assertEquals(23, state.contract.clauseCode)
        assertEquals(2031, state.contract.renewalYearCode)
        assertEquals(2032, state.contract.conversionYearCode)
        assertEquals(9, state.pendingMovement.clubCode)
        assertEquals(101, state.pendingMovement.valueCode)
        assertTrue(state.pendingMovement.loanFlag)
    }

    @Test
    fun `round trip preserves every proven commercial field without interpretation`() {
        val original = LegacyCareerPlayerCommercialSnapshot(
            salario = -17,
            rcClause = Int.MAX_VALUE,
            rcRenewYear = 0,
            rcConvYear = -1,
            pendSaleClub = -99,
            pendSaleValue = Int.MIN_VALUE,
            pendIsLoan = false,
        )

        val restored = LegacyCareerPlayerCommercialProjection.toLegacySnapshot(
            LegacyCareerPlayerCommercialProjection.toDomain(original),
        )

        assertEquals(original, restored)
    }
}

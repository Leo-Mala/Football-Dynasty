package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerPlayerCommercialSnapshotTest {
    @Test
    fun `preserves proven commercial fields without interpreting sentinel values`() {
        val sale = LegacyCareerPlayerCommercialSnapshot(
            salario = 1703,
            rcClause = -11,
            rcRenewYear = 2031,
            rcConvYear = 2032,
            pendSaleClub = -7,
            pendSaleValue = 987654,
            pendIsLoan = false,
        )
        val loan = LegacyCareerPlayerCommercialSnapshot(
            salario = -3,
            rcClause = 0,
            rcRenewYear = -1,
            rcConvYear = 0,
            pendSaleClub = 42,
            pendSaleValue = -99,
            pendIsLoan = true,
        )

        assertEquals(1703, sale.salario)
        assertEquals(-11, sale.rcClause)
        assertEquals(2031, sale.rcRenewYear)
        assertEquals(2032, sale.rcConvYear)
        assertEquals(-7, sale.pendSaleClub)
        assertEquals(987654, sale.pendSaleValue)
        assertFalse(sale.pendIsLoan)

        assertEquals(-3, loan.salario)
        assertEquals(0, loan.rcClause)
        assertEquals(-1, loan.rcRenewYear)
        assertEquals(0, loan.rcConvYear)
        assertEquals(42, loan.pendSaleClub)
        assertEquals(-99, loan.pendSaleValue)
        assertTrue(loan.pendIsLoan)
    }
}

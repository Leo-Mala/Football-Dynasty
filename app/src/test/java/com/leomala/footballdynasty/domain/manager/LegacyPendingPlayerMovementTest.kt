package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.legacy.compatibility.LegacyCareerPlayerCommercialSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyPendingPlayerMovementTest {
    @Test
    fun `projects pending movement fields without normalizing sentinel values`() {
        val sale = LegacyCareerPlayerCommercialSnapshot(
            salario = 100,
            rcClause = 200,
            rcRenewYear = 2030,
            rcConvYear = 2031,
            pendSaleClub = -7,
            pendSaleValue = -99,
            pendIsLoan = false,
        )
        val loan = sale.copy(
            pendSaleClub = 42,
            pendSaleValue = 987654,
            pendIsLoan = true,
        )

        val saleProjection = LegacyPendingPlayerMovement.from(sale)
        val loanProjection = LegacyPendingPlayerMovement.from(loan)

        assertEquals(-7, saleProjection.clubCode)
        assertEquals(-99, saleProjection.valueCode)
        assertFalse(saleProjection.loanFlag)

        assertEquals(42, loanProjection.clubCode)
        assertEquals(987654, loanProjection.valueCode)
        assertTrue(loanProjection.loanFlag)
    }
}

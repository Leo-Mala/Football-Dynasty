package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyPendingPlayerMovementTest {
    @Test
    fun `projects pending movement fields without normalizing sentinel values`() {
        val saleProjection = LegacyPendingPlayerMovement.fromRaw(
            pendSaleClub = -7,
            pendSaleValue = -99,
            pendIsLoan = false,
        )
        val loanProjection = LegacyPendingPlayerMovement.fromRaw(
            pendSaleClub = 42,
            pendSaleValue = 987654,
            pendIsLoan = true,
        )

        assertEquals(-7, saleProjection.clubCode)
        assertEquals(-99, saleProjection.valueCode)
        assertFalse(saleProjection.loanFlag)

        assertEquals(42, loanProjection.clubCode)
        assertEquals(987654, loanProjection.valueCode)
        assertTrue(loanProjection.loanFlag)
    }
}

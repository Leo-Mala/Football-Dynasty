package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyPlayerContractFieldsTest {
    @Test
    fun `projects contract fields without interpreting raw values`() {
        val projection = LegacyPlayerContractFields.fromRaw(
            salario = -1,
            rcClause = 0,
            rcRenewYear = -7,
            rcConvYear = 2031,
        )

        assertEquals(-1, projection.salaryCode)
        assertEquals(0, projection.clauseCode)
        assertEquals(-7, projection.renewalYearCode)
        assertEquals(2031, projection.conversionYearCode)
    }
}

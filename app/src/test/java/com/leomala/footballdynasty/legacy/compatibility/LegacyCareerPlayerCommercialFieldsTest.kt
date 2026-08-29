package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerPlayerCommercialFieldsTest {
    @Test
    fun exposesOnlyTheSevenProvenCommercialPlayerFields() {
        assertEquals(
            linkedSetOf(
                "salario",
                "rcClause",
                "rcRenewYear",
                "rcConvYear",
                "pendSaleClub",
                "pendSaleValue",
                "pendIsLoan",
            ),
            LegacyCareerPlayerCommercialFields.confirmedNames,
        )
    }

    @Test
    fun confirmationDoesNotExpandBeyondProvenNames() {
        assertTrue(LegacyCareerPlayerCommercialFields.isConfirmed("salario"))
        assertTrue(LegacyCareerPlayerCommercialFields.isConfirmed("pendIsLoan"))
        assertFalse(LegacyCareerPlayerCommercialFields.isConfirmed("transferPrice"))
        assertFalse(LegacyCareerPlayerCommercialFields.isConfirmed("contractYears"))
    }
}

package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerClubCommercialFieldsTest {
    @Test
    fun `locks only proven club commercial field names`() {
        assertEquals(linkedSetOf("ctInvest", "sponsor"), LegacyCareerClubCommercialFields.confirmedNames)
        assertTrue(LegacyCareerClubCommercialFields.isConfirmed("ctInvest"))
        assertTrue(LegacyCareerClubCommercialFields.isConfirmed("sponsor"))
        assertFalse(LegacyCareerClubCommercialFields.isConfirmed("balance"))
        assertFalse(LegacyCareerClubCommercialFields.isConfirmed("cash"))
        assertFalse(LegacyCareerClubCommercialFields.isConfirmed("revenue"))
    }
}

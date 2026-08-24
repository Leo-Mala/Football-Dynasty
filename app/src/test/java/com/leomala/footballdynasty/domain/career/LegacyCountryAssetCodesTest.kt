package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCountryAssetCodesTest {
    @Test
    fun `official country asset mapping covers exactly P0 through P220`() {
        assertEquals(221, LegacyCountryAssetCodes.count)
        val codes = (0 until LegacyCountryAssetCodes.count)
            .mapNotNull(LegacyCountryAssetCodes::codeForLegacyCountry)
        assertEquals(221, codes.size)
        assertEquals(221, codes.toSet().size)
    }

    @Test
    fun `known corpus positions retain exact asset codes`() {
        val expected = mapOf(
            0 to "AFG",
            11 to "ARG",
            29 to "BRA",
            44 to "CPR",
            45 to "TML",
            47 to "CNG",
            63 to "ELQ",
            64 to "ESV",
            81 to "GUI",
            101 to "IRN",
            110 to "KOS",
            115 to "LBN",
            143 to "NOZ",
            145 to "PGA",
            153 to "PRI",
            192 to "TUR",
            200 to "ZAM",
            216 to "GIB",
            217 to "GDA",
            218 to "GMA",
            219 to "MTI",
            220 to "GFR",
        )
        expected.forEach { (index, code) ->
            assertEquals("P$index", code, LegacyCountryAssetCodes.codeForLegacyCountry(index))
        }
        assertNull(LegacyCountryAssetCodes.codeForLegacyCountry(-1))
        assertNull(LegacyCountryAssetCodes.codeForLegacyCountry(221))
    }

    @Test
    fun `asset codes stay uppercase unique three-character legacy identifiers`() {
        val codes = (0 until LegacyCountryAssetCodes.count)
            .mapNotNull(LegacyCountryAssetCodes::codeForLegacyCountry)
        assertTrue(codes.all { it.length == 3 && it == it.uppercase() })
        assertEquals(codes.size, codes.toSet().size)
    }
}

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
        val groups = (0 until LegacyCountryAssetCodes.count)
            .mapNotNull(LegacyCountryAssetCodes::groupForLegacyCountry)
        assertEquals(221, codes.size)
        assertEquals(221, groups.size)
        assertEquals(221, codes.toSet().size)
        assertTrue(groups.all { it in 0..5 })
    }

    @Test
    fun `known corpus positions retain exact asset codes and groups`() {
        val expected = mapOf(
            0 to ("AFG" to 3),
            11 to ("ARG" to 1),
            29 to ("BRA" to 1),
            44 to ("CPR" to 0),
            45 to ("TML" to 3),
            47 to ("CNG" to 2),
            63 to ("ELQ" to 0),
            64 to ("ESV" to 0),
            81 to ("GUI" to 2),
            101 to ("IRN" to 0),
            110 to ("KOS" to 0),
            115 to ("LBN" to 3),
            143 to ("NOZ" to 5),
            145 to ("PGA" to 0),
            153 to ("PRI" to 4),
            192 to ("TUR" to 0),
            200 to ("ZAM" to 2),
            216 to ("GIB" to 0),
            217 to ("GDA" to 4),
            218 to ("GMA" to 5),
            219 to ("MTI" to 4),
            220 to ("GFR" to 4),
        )
        expected.forEach { (index, expectedData) ->
            assertEquals("P$index code", expectedData.first, LegacyCountryAssetCodes.codeForLegacyCountry(index))
            assertEquals("P$index group", expectedData.second, LegacyCountryAssetCodes.groupForLegacyCountry(index))
        }
        assertNull(LegacyCountryAssetCodes.codeForLegacyCountry(-1))
        assertNull(LegacyCountryAssetCodes.codeForLegacyCountry(221))
        assertNull(LegacyCountryAssetCodes.groupForLegacyCountry(-1))
        assertNull(LegacyCountryAssetCodes.groupForLegacyCountry(221))
    }

    @Test
    fun `asset codes stay uppercase unique three-character legacy identifiers`() {
        val codes = (0 until LegacyCountryAssetCodes.count)
            .mapNotNull(LegacyCountryAssetCodes::codeForLegacyCountry)
        assertTrue(codes.all { it.length == 3 && it == it.uppercase() })
        assertEquals(codes.size, codes.toSet().size)
    }
}

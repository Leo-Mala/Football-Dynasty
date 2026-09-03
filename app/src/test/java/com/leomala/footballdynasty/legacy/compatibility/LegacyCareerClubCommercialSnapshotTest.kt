package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCareerClubCommercialSnapshotTest {
    @Test
    fun `extracts both proven club commercial fields verbatim from exact legacy class and ignores unrelated fields`() {
        val opaqueInvestment = listOf("uninterpreted", 7)
        val opaqueSponsor = linkedMapOf("raw" to true)
        val fields = linkedMapOf<String, Any?>(
            LegacyCareerClubCommercialFields.INVESTMENT to opaqueInvestment,
            LegacyCareerClubCommercialFields.SPONSOR to opaqueSponsor,
            "xp" to emptyList<Any>(),
        )

        assertEquals(
            LegacyCareerClubCommercialSnapshot(
                ctInvest = opaqueInvestment,
                sponsor = opaqueSponsor,
            ),
            LegacyCareerClubCommercialSnapshotExtractor.extract(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                fields = fields,
            ),
        )
    }

    @Test
    fun `preserves present null values because nullability is not yet proven`() {
        val fields = linkedMapOf<String, Any?>(
            LegacyCareerClubCommercialFields.INVESTMENT to null,
            LegacyCareerClubCommercialFields.SPONSOR to null,
        )

        assertEquals(
            LegacyCareerClubCommercialSnapshot(ctInvest = null, sponsor = null),
            LegacyCareerClubCommercialSnapshotExtractor.extract(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                fields = fields,
            ),
        )
    }

    @Test
    fun `rejects fields from a different legacy source class`() {
        val fields = mapOf<String, Any?>(
            LegacyCareerClubCommercialFields.INVESTMENT to 1,
            LegacyCareerClubCommercialFields.SPONSOR to 2,
        )

        assertNull(
            LegacyCareerClubCommercialSnapshotExtractor.extract(
                sourceClassName = "a.p",
                fields = fields,
            ),
        )
    }

    @Test
    fun `rejects missing investment field without manufacturing a default`() {
        assertNull(
            LegacyCareerClubCommercialSnapshotExtractor.extract(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                fields = mapOf(LegacyCareerClubCommercialFields.SPONSOR to 1),
            ),
        )
    }

    @Test
    fun `rejects missing sponsor field without manufacturing a default`() {
        assertNull(
            LegacyCareerClubCommercialSnapshotExtractor.extract(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                fields = mapOf(LegacyCareerClubCommercialFields.INVESTMENT to 1),
            ),
        )
    }
}

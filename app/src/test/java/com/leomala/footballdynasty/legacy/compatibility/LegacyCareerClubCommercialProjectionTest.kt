package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyCareerClubCommercialProjectionTest {
    @Test
    fun roundTripPreservesOpaqueValuesWithoutConversion() {
        val investment = OpaqueValue("investment")
        val sponsor = OpaqueValue("sponsor")
        val source = LegacyCareerClubCommercialSnapshot(
            ctInvest = investment,
            sponsor = sponsor,
        )

        val state = LegacyCareerClubCommercialProjection.toDomain(source)
        val restored = LegacyCareerClubCommercialProjection.toLegacySnapshot(state)

        assertSame(investment, state.investmentRaw)
        assertSame(sponsor, state.sponsorRaw)
        assertSame(investment, restored.ctInvest)
        assertSame(sponsor, restored.sponsor)
        assertEquals(source, restored)
    }

    @Test
    fun roundTripPreservesPresentNullValues() {
        val source = LegacyCareerClubCommercialSnapshot(
            ctInvest = null,
            sponsor = null,
        )

        val state = LegacyCareerClubCommercialProjection.toDomain(source)
        val restored = LegacyCareerClubCommercialProjection.toLegacySnapshot(state)

        assertNull(state.investmentRaw)
        assertNull(state.sponsorRaw)
        assertEquals(source, restored)
    }

    @Test
    fun decodedFieldsBridgePreservesExactOpaqueReferences() {
        val investment = OpaqueValue("decoded-investment")
        val sponsor = OpaqueValue("decoded-sponsor")

        val state = LegacyCareerClubCommercialProjection.fromDecodedFields(
            sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
            fields = linkedMapOf(
                LegacyCareerClubCommercialFields.INVESTMENT to investment,
                LegacyCareerClubCommercialFields.SPONSOR to sponsor,
                "unrelatedClubField" to 99,
            ),
        )

        requireNotNull(state)
        assertSame(investment, state.investmentRaw)
        assertSame(sponsor, state.sponsorRaw)
    }

    @Test
    fun decodedFieldsBridgeKeepsExtractorBoundary() {
        val completeFields = mapOf(
            LegacyCareerClubCommercialFields.INVESTMENT to OpaqueValue("investment"),
            LegacyCareerClubCommercialFields.SPONSOR to OpaqueValue("sponsor"),
        )

        assertNull(
            LegacyCareerClubCommercialProjection.fromDecodedFields(
                sourceClassName = "a.p",
                fields = completeFields,
            ),
        )
        assertNull(
            LegacyCareerClubCommercialProjection.fromDecodedFields(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                fields = mapOf(LegacyCareerClubCommercialFields.INVESTMENT to null),
            ),
        )
    }

    private data class OpaqueValue(val label: String)
}

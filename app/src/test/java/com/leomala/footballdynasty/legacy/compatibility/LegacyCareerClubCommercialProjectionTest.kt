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

    @Test
    fun decodedFieldSliceRoundTripPreservesOnlyProvenOpaqueClubFields() {
        val investment = OpaqueValue("round-trip-investment")
        val sponsor = OpaqueValue("round-trip-sponsor")
        val state = LegacyCareerClubCommercialProjection.toDomain(
            LegacyCareerClubCommercialSnapshot(
                ctInvest = investment,
                sponsor = sponsor,
            ),
        )

        val slice = LegacyCareerClubCommercialProjection.toDecodedFieldSlice(state)
        val restored = LegacyCareerClubCommercialProjection.fromDecodedFields(
            sourceClassName = slice.sourceClassName,
            fields = slice.fields,
        )

        assertEquals(LegacyCareerClubCommercialFields.SOURCE_CLASS, slice.sourceClassName)
        assertEquals(
            listOf(
                LegacyCareerClubCommercialFields.INVESTMENT,
                LegacyCareerClubCommercialFields.SPONSOR,
            ),
            slice.fields.keys.toList(),
        )
        assertSame(investment, slice.fields[LegacyCareerClubCommercialFields.INVESTMENT])
        assertSame(sponsor, slice.fields[LegacyCareerClubCommercialFields.SPONSOR])
        requireNotNull(restored)
        assertSame(investment, restored.investmentRaw)
        assertSame(sponsor, restored.sponsorRaw)
    }

    @Test
    fun decodedFieldSlicePreservesPresentNullsWithoutInventingDefaults() {
        val state = LegacyCareerClubCommercialProjection.toDomain(
            LegacyCareerClubCommercialSnapshot(
                ctInvest = null,
                sponsor = null,
            ),
        )

        val slice = LegacyCareerClubCommercialProjection.toDecodedFieldSlice(state)

        assertEquals(2, slice.fields.size)
        assertEquals(true, slice.fields.containsKey(LegacyCareerClubCommercialFields.INVESTMENT))
        assertEquals(true, slice.fields.containsKey(LegacyCareerClubCommercialFields.SPONSOR))
        assertNull(slice.fields[LegacyCareerClubCommercialFields.INVESTMENT])
        assertNull(slice.fields[LegacyCareerClubCommercialFields.SPONSOR])
    }

    private data class OpaqueValue(val label: String)
}

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

    @Test
    fun writeBackReplacesOnlyProvenFieldsAndPreservesUnknownFieldReferences() {
        val oldInvestment = OpaqueValue("old-investment")
        val oldSponsor = OpaqueValue("old-sponsor")
        val unknownBefore = OpaqueValue("unknown-before")
        val unknownAfter = OpaqueValue("unknown-after")
        val newInvestment = OpaqueValue("new-investment")
        val newSponsor = OpaqueValue("new-sponsor")
        val existing = linkedMapOf<String, Any?>(
            "unknownBefore" to unknownBefore,
            LegacyCareerClubCommercialFields.INVESTMENT to oldInvestment,
            "unknownMiddle" to null,
            LegacyCareerClubCommercialFields.SPONSOR to oldSponsor,
            "unknownAfter" to unknownAfter,
        )
        val state = LegacyCareerClubCommercialProjection.toDomain(
            LegacyCareerClubCommercialSnapshot(
                ctInvest = newInvestment,
                sponsor = newSponsor,
            ),
        )

        val updated = LegacyCareerClubCommercialProjection.writeBackToDecodedFields(
            sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
            existingFields = existing,
            state = state,
        )

        requireNotNull(updated)
        assertEquals(existing.keys.toList(), updated.keys.toList())
        assertSame(unknownBefore, updated["unknownBefore"])
        assertEquals(true, updated.containsKey("unknownMiddle"))
        assertNull(updated["unknownMiddle"])
        assertSame(unknownAfter, updated["unknownAfter"])
        assertSame(newInvestment, updated[LegacyCareerClubCommercialFields.INVESTMENT])
        assertSame(newSponsor, updated[LegacyCareerClubCommercialFields.SPONSOR])
        assertSame(oldInvestment, existing[LegacyCareerClubCommercialFields.INVESTMENT])
        assertSame(oldSponsor, existing[LegacyCareerClubCommercialFields.SPONSOR])
    }

    @Test
    fun writeBackPreservesExplicitNullAndRejectsIncompleteOrWrongSourceObjects() {
        val state = LegacyCareerClubCommercialProjection.toDomain(
            LegacyCareerClubCommercialSnapshot(
                ctInvest = null,
                sponsor = null,
            ),
        )
        val complete = linkedMapOf<String, Any?>(
            LegacyCareerClubCommercialFields.INVESTMENT to OpaqueValue("old-investment"),
            LegacyCareerClubCommercialFields.SPONSOR to OpaqueValue("old-sponsor"),
            "unknown" to OpaqueValue("unknown"),
        )

        val updated = LegacyCareerClubCommercialProjection.writeBackToDecodedFields(
            sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
            existingFields = complete,
            state = state,
        )

        requireNotNull(updated)
        assertEquals(true, updated.containsKey(LegacyCareerClubCommercialFields.INVESTMENT))
        assertEquals(true, updated.containsKey(LegacyCareerClubCommercialFields.SPONSOR))
        assertNull(updated[LegacyCareerClubCommercialFields.INVESTMENT])
        assertNull(updated[LegacyCareerClubCommercialFields.SPONSOR])
        assertNull(
            LegacyCareerClubCommercialProjection.writeBackToDecodedFields(
                sourceClassName = "a.p",
                existingFields = complete,
                state = state,
            ),
        )
        assertNull(
            LegacyCareerClubCommercialProjection.writeBackToDecodedFields(
                sourceClassName = LegacyCareerClubCommercialFields.SOURCE_CLASS,
                existingFields = mapOf(LegacyCareerClubCommercialFields.INVESTMENT to null),
                state = state,
            ),
        )
    }

    private data class OpaqueValue(val label: String)
}

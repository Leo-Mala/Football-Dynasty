package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyCareerPlayerCommercialSnapshotExtractorTest {
    private val provenFields = linkedMapOf<String, Any?>(
        LegacyCareerPlayerCommercialFields.SALARY to -3,
        LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE to 0,
        LegacyCareerPlayerCommercialFields.RENEW_YEAR to -1,
        LegacyCareerPlayerCommercialFields.CONVERSION_YEAR to 2032,
        LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB to 42,
        LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to -99,
        LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to true,
    )

    @Test
    fun `extracts exact proven scalar values and ignores unrelated player fields`() {
        val fields = provenFields + mapOf(
            "nome" to "opaque fixture name",
            "forca" to 77,
        )

        assertEquals(
            LegacyCareerPlayerCommercialSnapshot(
                salario = -3,
                rcClause = 0,
                rcRenewYear = -1,
                rcConvYear = 2032,
                pendSaleClub = 42,
                pendSaleValue = -99,
                pendIsLoan = true,
            ),
            LegacyCareerPlayerCommercialSnapshotExtractor.extract(fields),
        )
    }

    @Test
    fun `rejects every missing proven commercial field`() {
        LegacyCareerPlayerCommercialFields.confirmedNames.forEach { missing ->
            assertNull(
                "missing field must fail extraction: $missing",
                LegacyCareerPlayerCommercialSnapshotExtractor.extract(provenFields - missing),
            )
        }
    }

    @Test
    fun `rejects scalar widening coercion and boolean coercion`() {
        assertNull(
            LegacyCareerPlayerCommercialSnapshotExtractor.extract(
                provenFields + (LegacyCareerPlayerCommercialFields.SALARY to 3L),
            ),
        )
        assertNull(
            LegacyCareerPlayerCommercialSnapshotExtractor.extract(
                provenFields + (LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to 1),
            ),
        )
    }

    @Test
    fun `rejects null instead of manufacturing a default sentinel`() {
        assertNull(
            LegacyCareerPlayerCommercialSnapshotExtractor.extract(
                provenFields + (LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to null),
            ),
        )
    }
}

package com.leomala.footballdynasty.legacy.compatibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCareerPlayerCommercialProjectionTest {
    @Test
    fun `preserves the confirmed legacy commercial snapshot exactly`() {
        val snapshot = LegacyCareerPlayerCommercialSnapshot(
            salario = 17,
            rcClause = 23,
            rcRenewYear = 2031,
            rcConvYear = 2032,
            pendSaleClub = 9,
            pendSaleValue = 101,
            pendIsLoan = true,
        )

        val state = LegacyCareerPlayerCommercialProjection.toDomain(snapshot)

        assertEquals(17, state.contract.salaryCode)
        assertEquals(23, state.contract.clauseCode)
        assertEquals(2031, state.contract.renewalYearCode)
        assertEquals(2032, state.contract.conversionYearCode)
        assertEquals(9, state.pendingMovement.clubCode)
        assertEquals(101, state.pendingMovement.valueCode)
        assertTrue(state.pendingMovement.loanFlag)
    }

    @Test
    fun `round trip preserves every proven commercial field without interpretation`() {
        val original = LegacyCareerPlayerCommercialSnapshot(
            salario = -17,
            rcClause = Int.MAX_VALUE,
            rcRenewYear = 0,
            rcConvYear = -1,
            pendSaleClub = -99,
            pendSaleValue = Int.MIN_VALUE,
            pendIsLoan = false,
        )

        val restored = LegacyCareerPlayerCommercialProjection.toLegacySnapshot(
            LegacyCareerPlayerCommercialProjection.toDomain(original),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `decoded player slice reaches domain without changing proven primitive values`() {
        val state = LegacyCareerPlayerCommercialProjection.fromDecodedFields(
            sourceClassName = LegacyCareerPlayerCommercialFields.SOURCE_CLASS,
            fields = linkedMapOf(
                LegacyCareerPlayerCommercialFields.SALARY to Int.MIN_VALUE,
                LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE to Int.MAX_VALUE,
                LegacyCareerPlayerCommercialFields.RENEW_YEAR to -1,
                LegacyCareerPlayerCommercialFields.CONVERSION_YEAR to 0,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB to -99,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to 101,
                LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to false,
                "unrelatedPlayerField" to "preserved outside this slice",
            ),
        )

        requireNotNull(state)
        assertEquals(Int.MIN_VALUE, state.contract.salaryCode)
        assertEquals(Int.MAX_VALUE, state.contract.clauseCode)
        assertEquals(-1, state.contract.renewalYearCode)
        assertEquals(0, state.contract.conversionYearCode)
        assertEquals(-99, state.pendingMovement.clubCode)
        assertEquals(101, state.pendingMovement.valueCode)
        assertFalse(state.pendingMovement.loanFlag)
    }

    @Test
    fun `domain exports only the exact proven a p commercial slice and round trips losslessly`() {
        val original = LegacyCareerPlayerCommercialProjection.toDomain(
            LegacyCareerPlayerCommercialSnapshot(
                salario = Int.MIN_VALUE,
                rcClause = Int.MAX_VALUE,
                rcRenewYear = -1,
                rcConvYear = 0,
                pendSaleClub = -99,
                pendSaleValue = 101,
                pendIsLoan = true,
            ),
        )

        val decoded = LegacyCareerPlayerCommercialProjection.toDecodedFieldSlice(original)

        assertEquals(LegacyCareerPlayerCommercialFields.SOURCE_CLASS, decoded.sourceClassName)
        assertEquals(
            linkedMapOf(
                LegacyCareerPlayerCommercialFields.SALARY to Int.MIN_VALUE,
                LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE to Int.MAX_VALUE,
                LegacyCareerPlayerCommercialFields.RENEW_YEAR to -1,
                LegacyCareerPlayerCommercialFields.CONVERSION_YEAR to 0,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB to -99,
                LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to 101,
                LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to true,
            ),
            decoded.fields,
        )

        val restored = LegacyCareerPlayerCommercialProjection.fromDecodedFields(
            sourceClassName = decoded.sourceClassName,
            fields = decoded.fields,
        )
        assertEquals(original, restored)
    }

    @Test
    fun `decoded player bridge rejects the wrong serialized source class`() {
        val fields = completeDecodedFields()

        assertNull(
            LegacyCareerPlayerCommercialProjection.fromDecodedFields(
                sourceClassName = "a.ac",
                fields = fields,
            ),
        )
    }

    @Test
    fun `decoded player bridge rejects incomplete or differently typed slices`() {
        val missingField = completeDecodedFields().toMutableMap().apply {
            remove(LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE)
        }
        val widenedInteger = completeDecodedFields().toMutableMap().apply {
            put(LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE, 101L)
        }
        val numericBoolean = completeDecodedFields().toMutableMap().apply {
            put(LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN, 1)
        }

        assertNull(
            LegacyCareerPlayerCommercialProjection.fromDecodedFields(
                sourceClassName = LegacyCareerPlayerCommercialFields.SOURCE_CLASS,
                fields = missingField,
            ),
        )
        assertNull(
            LegacyCareerPlayerCommercialProjection.fromDecodedFields(
                sourceClassName = LegacyCareerPlayerCommercialFields.SOURCE_CLASS,
                fields = widenedInteger,
            ),
        )
        assertNull(
            LegacyCareerPlayerCommercialProjection.fromDecodedFields(
                sourceClassName = LegacyCareerPlayerCommercialFields.SOURCE_CLASS,
                fields = numericBoolean,
            ),
        )
    }

    @Test
    fun `commercial field catalog pins only the proven primitive shapes`() {
        val fields = LegacyCareerPlayerCommercialFields

        assertEquals(
            linkedMapOf(
                fields.SALARY to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.RELEASE_CLAUSE to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.RENEW_YEAR to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.CONVERSION_YEAR to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.PENDING_SALE_CLUB to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.PENDING_SALE_VALUE to LegacyCareerPlayerCommercialFields.ScalarType.INT,
                fields.PENDING_IS_LOAN to LegacyCareerPlayerCommercialFields.ScalarType.BOOLEAN,
            ),
            fields.confirmedTypes,
        )
        assertEquals(fields.confirmedTypes.keys, fields.confirmedNames)
        assertEquals(
            LegacyCareerPlayerCommercialFields.ScalarType.BOOLEAN,
            fields.typeOf(fields.PENDING_IS_LOAN),
        )
        assertNull(fields.typeOf("unknownCommercialField"))
    }

    private fun completeDecodedFields(): Map<String, Any?> = linkedMapOf(
        LegacyCareerPlayerCommercialFields.SALARY to 17,
        LegacyCareerPlayerCommercialFields.RELEASE_CLAUSE to 23,
        LegacyCareerPlayerCommercialFields.RENEW_YEAR to 2031,
        LegacyCareerPlayerCommercialFields.CONVERSION_YEAR to 2032,
        LegacyCareerPlayerCommercialFields.PENDING_SALE_CLUB to 9,
        LegacyCareerPlayerCommercialFields.PENDING_SALE_VALUE to 101,
        LegacyCareerPlayerCommercialFields.PENDING_IS_LOAN to true,
    )
}

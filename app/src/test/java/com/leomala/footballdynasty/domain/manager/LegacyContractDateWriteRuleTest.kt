package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyContractDateWriteRuleTest {
    @Test
    fun `false extends from existing contract when it is later than career calendar`() {
        assertEquals(
            20_000L + (180L * 86_400_000L),
            LegacyContractDateWriteRule.endTimestampMillis(
                currentCareerTimestampMillis = 10_000L,
                currentContractEndMillis = 20_000L,
                durationDays = 180L,
                ignoreExistingContractAsBase = false,
            ),
        )
    }

    @Test
    fun `false extends from career calendar when contract is equal or earlier`() {
        assertEquals(
            20_000L + 86_400_000L,
            LegacyContractDateWriteRule.endTimestampMillis(
                currentCareerTimestampMillis = 20_000L,
                currentContractEndMillis = 20_000L,
                durationDays = 1L,
                ignoreExistingContractAsBase = false,
            ),
        )
        assertEquals(
            20_000L + 86_400_000L,
            LegacyContractDateWriteRule.endTimestampMillis(
                currentCareerTimestampMillis = 20_000L,
                currentContractEndMillis = 10_000L,
                durationDays = 1L,
                ignoreExistingContractAsBase = false,
            ),
        )
    }

    @Test
    fun `true always uses current career calendar even when existing contract is later`() {
        assertEquals(
            10_000L + (365L * 86_400_000L),
            LegacyContractDateWriteRule.endTimestampMillis(
                currentCareerTimestampMillis = 10_000L,
                currentContractEndMillis = Long.MAX_VALUE - 1,
                durationDays = 365L,
                ignoreExistingContractAsBase = true,
            ),
        )
    }

    @Test
    fun `renewal invocation composes exact days and false argument`() {
        val invocation = LegacyContractWriteInvocation(durationDays = 730, booleanArgument = false)
        assertEquals(
            30_000L + (730L * 86_400_000L),
            LegacyContractDateWriteRule.endTimestampMillis(
                currentCareerTimestampMillis = 10_000L,
                currentContractEndMillis = 30_000L,
                invocation = invocation,
            ),
        )
    }

    @Test
    fun `zero and negative durations are not clamped`() {
        assertEquals(
            5_000L,
            LegacyContractDateWriteRule.endTimestampMillis(5_000L, 1_000L, 0L, false),
        )
        assertEquals(
            5_000L - 86_400_000L,
            LegacyContractDateWriteRule.endTimestampMillis(5_000L, 1_000L, -1L, false),
        )
    }
}

package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyContractRenewalRuleTest {
    @Test
    fun `uses discount thresholds when fewer than sixty contract days remain`() {
        val current = 10_000
        assertEquals(9_900, LegacyContractRenewalRule.minimumAcceptedSalary(current, 59, 0))
        assertEquals(9_700, LegacyContractRenewalRule.minimumAcceptedSalary(current, 59, 1))
        assertEquals(9_500, LegacyContractRenewalRule.minimumAcceptedSalary(current, 59, 2))
        assertEquals(8_800, LegacyContractRenewalRule.minimumAcceptedSalary(current, 59, 3))
    }

    @Test
    fun `uses uplift thresholds from exactly sixty remaining days`() {
        val current = 10_000
        assertEquals(11_000, LegacyContractRenewalRule.minimumAcceptedSalary(current, 60, 0))
        assertEquals(11_200, LegacyContractRenewalRule.minimumAcceptedSalary(current, 60, 1))
        assertEquals(11_500, LegacyContractRenewalRule.minimumAcceptedSalary(current, 60, 2))
        assertEquals(10_500, LegacyContractRenewalRule.minimumAcceptedSalary(current, 60, 3))
    }

    @Test
    fun `preserves legacy integer truncation before acceptance comparison`() {
        assertEquals(100, LegacyContractRenewalRule.minimumAcceptedSalary(101, 59, 0))
        assertEquals(111, LegacyContractRenewalRule.minimumAcceptedSalary(101, 60, 0))
    }

    @Test
    fun `rejects non positive and values above exact five salary cap before negotiation`() {
        val invalid = LegacyContractRenewalRule.evaluate(
            currentSalary = 1_000,
            remainingContractDays = 10,
            termIndex = 0,
            offeredSalary = 0,
        )
        assertEquals(LegacyContractOfferResult.INVALID_NON_POSITIVE, invalid.result)
        assertNull(invalid.requiredSalary)
        assertEquals(5_000, invalid.maximumSalary)

        val above = LegacyContractRenewalRule.evaluate(
            currentSalary = 1_000,
            remainingContractDays = 10,
            termIndex = 0,
            offeredSalary = 5_001,
        )
        assertEquals(LegacyContractOfferResult.ABOVE_SALARY_LIMIT, above.result)
        assertNull(above.requiredSalary)

        val exactCap = LegacyContractRenewalRule.evaluate(
            currentSalary = 1_000,
            remainingContractDays = 10,
            termIndex = 0,
            offeredSalary = 5_000,
        )
        assertEquals(LegacyContractOfferResult.ACCEPTED, exactCap.result)
    }

    @Test
    fun `offer below computed requirement produces counter and exact requirement accepts`() {
        val counter = LegacyContractRenewalRule.evaluate(
            currentSalary = 10_000,
            remainingContractDays = 60,
            termIndex = 2,
            offeredSalary = 11_499,
        )
        assertEquals(LegacyContractOfferResult.COUNTER_REQUIRED, counter.result)
        assertEquals(11_500, counter.requiredSalary)
        assertEquals(730, counter.durationDays)

        val accepted = LegacyContractRenewalRule.evaluate(
            currentSalary = 10_000,
            remainingContractDays = 60,
            termIndex = 2,
            offeredSalary = 11_500,
        )
        assertEquals(LegacyContractOfferResult.ACCEPTED, accepted.result)
        assertEquals(11_500, accepted.requiredSalary)
    }

    @Test
    fun `accepted apply plan preserves all four exact legacy durations and side effects`() {
        val expectedDurations = listOf(180, 365, 730, 1095)
        expectedDurations.forEachIndexed { index, expectedDays ->
            val plan = LegacyContractRenewalRule.acceptedApplyPlan(
                offeredSalary = 12_345,
                termIndex = index,
            )
            assertEquals(12_345, plan.newSalary)
            assertEquals(expectedDays, plan.durationDays)
            assertTrue(plan.clearRawSaleFlag)
            assertTrue(plan.copyRawOToD)
            assertFalse(plan.contractBooleanArgument)
            assertTrue(plan.markMainTeamDirty)
        }
    }
}

package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyContractRenewalRuntimeRuleTest {
    @Test
    fun rejectedOfferIsExactRuntimeNoOp() {
        val before = state()
        val result = LegacyContractRenewalRuntimeRule.execute(
            state = before,
            remainingContractDays = 60,
            termIndex = 2,
            offeredSalary = 11_499,
        )

        assertEquals(LegacyContractOfferResult.COUNTER_REQUIRED, result.decision.result)
        assertSame(before, result.state)
        assertNull(result.contractWrite)
    }

    @Test
    fun acceptedOfferAppliesOnlyProvenLegacySideEffects() {
        val result = LegacyContractRenewalRuntimeRule.execute(
            state = state(),
            remainingContractDays = 60,
            termIndex = 2,
            offeredSalary = 11_500,
        )

        assertEquals(LegacyContractOfferResult.ACCEPTED, result.decision.result)
        assertEquals(11_500, result.state.salaryCode)
        assertFalse(result.state.rawSaleFlag)
        assertEquals(77, result.state.rawOCode)
        assertEquals(77, result.state.rawDCode)
        assertTrue(result.state.mainTeamDirty)
        assertEquals(LegacyContractWriteInvocation(730, false), result.contractWrite)
    }

    @Test
    fun acceptedRenewalPreservesAllFourExactContractWriteArguments() {
        val expectedDays = listOf(180, 365, 730, 1095)
        expectedDays.forEachIndexed { termIndex, days ->
            val result = LegacyContractRenewalRuntimeRule.execute(
                state = state(),
                remainingContractDays = 10,
                termIndex = termIndex,
                offeredSalary = 10_000,
            )

            assertEquals(LegacyContractOfferResult.ACCEPTED, result.decision.result)
            assertEquals(LegacyContractWriteInvocation(days, false), result.contractWrite)
        }
    }

    @Test
    fun alreadyDirtyStateAndRawOArePreservedExactlyThroughAcceptedPath() {
        val before = state().copy(mainTeamDirty = true, rawOCode = -31, rawDCode = 9)
        val result = LegacyContractRenewalRuntimeRule.execute(
            state = before,
            remainingContractDays = 59,
            termIndex = 0,
            offeredSalary = 9_900,
        )

        assertTrue(result.state.mainTeamDirty)
        assertEquals(-31, result.state.rawOCode)
        assertEquals(-31, result.state.rawDCode)
    }

    private fun state() = LegacyContractRenewalRuntimeState(
        salaryCode = 10_000,
        rawSaleFlag = true,
        rawOCode = 77,
        rawDCode = 12,
        mainTeamDirty = false,
    )
}

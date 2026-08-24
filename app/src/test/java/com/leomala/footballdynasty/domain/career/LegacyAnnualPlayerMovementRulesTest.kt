package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyAnnualPlayerMovementRulesTest {
    @Test
    fun `unmanaged annual movement performs structural move without ledger calls`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = false,
            targetManaged = false,
            amount = 75,
        )

        assertFalse(plan.activityMainTeamDirty)
        assertTrue(plan.relinkToTarget)
        assertTrue(plan.resetX)
        assertTrue(plan.resetZ)
        assertTrue(plan.leaveYUnchanged)
        assertEquals(0, plan.secondaryCalculatedAmount)
        assertNull(plan.sourceBCode1Amount)
        assertNull(plan.targetDCode1Amount)
        assertEquals(180L, plan.legacyDurationArgument)
        assertTrue(plan.callQ1)
        assertTrue(plan.clearSourceSpecialReferences)
        assertTrue(plan.removeFromSource)
        assertTrue(plan.addToTarget)
        assertFalse(plan.setS1True)
        assertFalse(plan.clearSourceE1)
    }

    @Test
    fun `managed source receives only structural B code one amount`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = true,
            targetManaged = false,
            amount = 55,
        )

        assertTrue(plan.activityMainTeamDirty)
        assertEquals(55, plan.sourceBCode1Amount)
        assertNull(plan.targetDCode1Amount)
        assertEquals(0, plan.secondaryCalculatedAmount)
    }

    @Test
    fun `managed target receives only structural D code one amount`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = false,
            targetManaged = true,
            amount = 55,
        )

        assertTrue(plan.activityMainTeamDirty)
        assertNull(plan.sourceBCode1Amount)
        assertEquals(55, plan.targetDCode1Amount)
        assertEquals(0, plan.secondaryCalculatedAmount)
    }

    @Test
    fun `both managed clubs reproduce shared flag side effects`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = true,
            targetManaged = true,
            amount = 100,
        )

        assertEquals(100, plan.sourceBCode1Amount)
        assertEquals(100, plan.targetDCode1Amount)
        assertTrue(plan.setS1True)
        assertTrue(plan.clearSourceE1)
    }

    @Test
    fun `zero amount suppresses both ledger calls but still moves membership`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = true,
            targetManaged = true,
            amount = 0,
        )

        assertNull(plan.sourceBCode1Amount)
        assertNull(plan.targetDCode1Amount)
        assertTrue(plan.relinkToTarget)
        assertTrue(plan.removeFromSource)
        assertTrue(plan.addToTarget)
        assertTrue(plan.callQ1)
    }

    @Test
    fun `missing source still adds to target and skips source cleanup`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = false,
            sourceManaged = false,
            targetManaged = true,
            amount = 25,
        )

        assertFalse(plan.removeFromSource)
        assertFalse(plan.clearSourceSpecialReferences)
        assertTrue(plan.relinkToTarget)
        assertTrue(plan.addToTarget)
        assertTrue(plan.callQ1)
        assertEquals(25, plan.targetDCode1Amount)
        assertFalse(plan.setS1True)
        assertFalse(plan.clearSourceE1)
    }

    @Test
    fun `negative amount skips ledger calls but preserves legacy structural movement`() {
        val plan = LegacyAnnualPlayerMovementRules.annualT1Plan(
            sourceExists = true,
            sourceManaged = true,
            targetManaged = true,
            amount = -1,
        )

        assertNull(plan.sourceBCode1Amount)
        assertNull(plan.targetDCode1Amount)
        assertTrue(plan.relinkToTarget)
        assertTrue(plan.removeFromSource)
        assertTrue(plan.addToTarget)
        assertTrue(plan.callQ1)
        assertTrue(plan.setS1True)
        assertTrue(plan.clearSourceE1)
    }
}

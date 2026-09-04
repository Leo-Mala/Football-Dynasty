package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyJuniorDraftFieldRulesTest {
    @Test
    fun `legacy h reproduces ordinary non-star valuation branch`() {
        val context = LegacyJuniorDraftFieldRules.ValueContext(
            targetF0 = 21,
            targetV0 = false,
            targetO = 1,
            globalV1 = false,
            countryGroup = 1,
            legacyB = false,
            legacyC = 18,
            legacyE = 3,
            legacyF = 10,
            legacyN = 6,
        )
        assertEquals(81216, LegacyJuniorDraftFieldRules.legacyH(context))
    }

    @Test
    fun `legacy h preserves star country-group and position multipliers`() {
        val context = LegacyJuniorDraftFieldRules.ValueContext(
            targetF0 = 22,
            targetV0 = false,
            targetO = 1,
            globalV1 = false,
            countryGroup = 0,
            legacyB = true,
            legacyC = 16,
            legacyE = 4,
            legacyF = 8,
            legacyN = 10,
        )
        assertEquals(386730, LegacyJuniorDraftFieldRules.legacyH(context))
    }

    @Test
    fun `legacy i reproduces target and position adjustments`() {
        val context = LegacyJuniorDraftFieldRules.ValueContext(
            targetF0 = 21,
            targetV0 = false,
            targetO = 1,
            globalV1 = false,
            countryGroup = 1,
            legacyB = false,
            legacyC = 18,
            legacyE = 3,
            legacyF = 10,
            legacyN = 6,
        )
        assertEquals(650, LegacyJuniorDraftFieldRules.legacyI(context))
    }

    @Test
    fun `legacy i preserves age penalty star bonus minimum and global multiplier`() {
        val context = LegacyJuniorDraftFieldRules.ValueContext(
            targetF0 = 10,
            targetV0 = true,
            targetO = 2,
            globalV1 = true,
            countryGroup = 0,
            legacyB = true,
            legacyC = 35,
            legacyE = 0,
            legacyF = 4,
            legacyN = 5,
        )
        assertEquals(808, LegacyJuniorDraftFieldRules.legacyI(context))
    }

    @Test
    fun `new p draft starts with zero development remainder`() {
        val context = LegacyJuniorDraftFieldRules.ValueContext(
            targetF0 = 10,
            targetV0 = false,
            targetO = 0,
            globalV1 = false,
            countryGroup = 0,
            legacyB = false,
            legacyC = 16,
            legacyE = 0,
            legacyF = 10,
            legacyN = 1,
        )
        assertEquals(0.0, LegacyJuniorDraftFieldRules.missingFields(context).developmentRemainder, 0.0)
    }

    @Test
    fun `manual false promotion removes club draft and stages final in D0`() {
        assertEquals(
            LegacyJuniorDraftFieldRules.PromotionListEffects(
                removeDraftFromClubImmediately = true,
                stageDraftInLegacyL1 = false,
                stageMaterializedPlayerInLegacyD0 = true,
                stageMaterializedPlayerInLegacyJ1 = false,
            ),
            LegacyJuniorDraftFieldRules.promotionListEffects(
                LegacyJuniorDraftFieldRules.PromotionRoute.MANUAL_FALSE,
            ),
        )
    }

    @Test
    fun `annual true promotion stages draft and final without immediate club removal`() {
        assertEquals(
            LegacyJuniorDraftFieldRules.PromotionListEffects(
                removeDraftFromClubImmediately = false,
                stageDraftInLegacyL1 = true,
                stageMaterializedPlayerInLegacyD0 = false,
                stageMaterializedPlayerInLegacyJ1 = true,
            ),
            LegacyJuniorDraftFieldRules.promotionListEffects(
                LegacyJuniorDraftFieldRules.PromotionRoute.ANNUAL_TRUE,
            ),
        )
    }
}

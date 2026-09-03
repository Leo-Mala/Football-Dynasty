package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class LegacyClubCommercialStateTest {
    @Test
    fun `investment replacement preserves sponsor reference exactly`() {
        val originalInvestment = Any()
        val sponsor = Any()
        val replacementInvestment = Any()
        val state = LegacyClubCommercialState.fromRaw(
            investmentRaw = originalInvestment,
            sponsorRaw = sponsor,
        )

        val updated = state.withInvestmentRaw(replacementInvestment)

        assertSame(replacementInvestment, updated.investmentRaw)
        assertSame(sponsor, updated.sponsorRaw)
    }

    @Test
    fun `sponsor replacement preserves investment reference exactly`() {
        val investment = Any()
        val originalSponsor = Any()
        val replacementSponsor = Any()
        val state = LegacyClubCommercialState.fromRaw(
            investmentRaw = investment,
            sponsorRaw = originalSponsor,
        )

        val updated = state.withSponsorRaw(replacementSponsor)

        assertSame(investment, updated.investmentRaw)
        assertSame(replacementSponsor, updated.sponsorRaw)
    }

    @Test
    fun `single-field replacements preserve explicit null without defaults`() {
        val sponsor = Any()
        val investment = Any()

        val withoutInvestment = LegacyClubCommercialState.fromRaw(
            investmentRaw = investment,
            sponsorRaw = sponsor,
        ).withInvestmentRaw(null)
        val withoutSponsor = LegacyClubCommercialState.fromRaw(
            investmentRaw = investment,
            sponsorRaw = sponsor,
        ).withSponsorRaw(null)

        assertNull(withoutInvestment.investmentRaw)
        assertSame(sponsor, withoutInvestment.sponsorRaw)
        assertSame(investment, withoutSponsor.investmentRaw)
        assertNull(withoutSponsor.sponsorRaw)
    }
}

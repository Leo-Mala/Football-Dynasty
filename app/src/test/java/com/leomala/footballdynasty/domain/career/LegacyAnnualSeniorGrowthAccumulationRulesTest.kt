package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualSeniorGrowthAccumulationRulesTest {
    private fun calculate(
        clubF0: Int = 19,
        clubR0: Boolean = true,
        clubP0: Int = 0,
        clubJ: Int = 9,
        clubJ0: Int = 0,
        playerE: Int = 19,
        playerJ: Int = 0,
        playerD0: Int = 0,
        playerM: Int = 0,
        playerMFlag: Boolean = false,
        playerW0: Boolean = false,
        playerO0: Boolean = false,
    ) = LegacyAnnualSeniorGrowthAccumulationRules.calculate(
        club = LegacyAnnualSeniorGrowthAccumulationRules.ClubState(
            legacyF0 = clubF0,
            legacyR0 = clubR0,
            legacyP0 = clubP0,
            legacyJ = clubJ,
            legacyJ0 = clubJ0,
        ),
        player = LegacyAnnualSeniorGrowthAccumulationRules.PlayerState(
            legacyE = playerE,
            legacyJ = playerJ,
            legacyD0 = playerD0,
            legacyM = playerM,
            legacyMFlag = playerMFlag,
            legacyW0 = playerW0,
            legacyO0 = playerO0,
        ),
    )

    @Test
    fun `base rate matrix preserves executable band boundaries`() {
        assertEquals(0.16, calculate(clubF0 = 19, playerE = 19).increment, 1e-12)
        assertEquals(0.12, calculate(clubF0 = 19, playerE = 20).increment, 1e-12)
        assertEquals(0.10, calculate(clubF0 = 19, playerE = 23).increment, 1e-12)
        assertEquals(0.08, calculate(clubF0 = 19, playerE = 29).increment, 1e-12)

        assertEquals(0.12, calculate(clubF0 = 15, playerE = 17).increment, 1e-12)
        assertEquals(0.10, calculate(clubF0 = 15, playerE = 18).increment, 1e-12)
        assertEquals(0.08, calculate(clubF0 = 15, playerE = 21).increment, 1e-12)
        assertEquals(0.06, calculate(clubF0 = 15, playerE = 29).increment, 1e-12)
    }

    @Test
    fun `non R0 club rewrites band and adds legacy point zero three bonus`() {
        val result = calculate(
            clubF0 = 1,
            clubR0 = false,
            clubP0 = 4,
            playerE = 19,
        )
        assertEquals(20, result.effectiveClubBand)
        assertEquals(0.03, result.clubBandBonus, 1e-12)
        assertEquals(0.19, result.increment, 1e-12)
    }

    @Test
    fun `M flag and player raw modifiers preserve SMALI ordering`() {
        val result = calculate(
            playerJ = 35,
            playerD0 = 40,
            playerM = 9,
            playerMFlag = true,
            playerW0 = true,
            playerO0 = true,
        )
        assertEquals(0.22, result.increment, 1e-12)
    }

    @Test
    fun `club J zero special ids preserve exact plus and subtraction branches`() {
        assertEquals(0.17, calculate(clubJ = 0, clubJ0 = 3).increment, 1e-12)
        assertEquals(0.15, calculate(clubJ = 0, clubJ0 = 154).increment, 1e-12)
        assertEquals(0.14, calculate(clubJ = 0, clubJ0 = 999).increment, 1e-12)
    }

    @Test
    fun `club J one impossible exemption chain has effective normal subtraction`() {
        assertEquals(0.14, calculate(clubJ = 1, clubJ0 = 29).increment, 1e-12)
        assertEquals(0.14, calculate(clubJ = 1, clubJ0 = 11).increment, 1e-12)
    }

    @Test
    fun `strict greater than point zero six adjustment leaves exact threshold unchanged`() {
        assertEquals(0.06, calculate(clubF0 = 15, playerE = 29, clubJ = 2).increment, 1e-12)
    }

    @Test
    fun `negative rate maps to point zero one while exact zero stays zero`() {
        assertEquals(
            0.01,
            calculate(
                clubF0 = 1,
                playerE = 29,
                playerJ = 80,
                playerD0 = 40,
                playerM = 0,
                clubJ = 5,
            ).increment,
            1e-12,
        )

        assertEquals(
            0.0,
            calculate(
                clubF0 = 1,
                playerE = 29,
                playerJ = 30,
                clubJ = 9,
            ).increment,
            1e-12,
        )
    }
}

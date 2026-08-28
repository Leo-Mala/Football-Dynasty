package com.leomala.footballdynasty.domain.competition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyGroupFixtureRouterTest {
    @Test
    fun `konrent t X group mode maps exact legacy V codes`() {
        assertEquals(
            LegacyGroupFixtureRouter.Strategy.LEGACY_K_FOUR_BY_FIVE,
            LegacyGroupFixtureRouter.strategyFor(1),
        )
        assertEquals(
            LegacyGroupFixtureRouter.Strategy.LEGACY_L_FOUR_BY_FOUR,
            LegacyGroupFixtureRouter.strategyFor(11),
        )
        assertEquals(
            LegacyGroupFixtureRouter.Strategy.LEGACY_C_TWO_BY_SIX,
            LegacyGroupFixtureRouter.strategyFor(6),
        )
        assertEquals(
            LegacyGroupFixtureRouter.Strategy.LEGACY_C_TWO_BY_SIX,
            LegacyGroupFixtureRouter.strategyFor(0x70B),
        )
        assertEquals(
            LegacyGroupFixtureRouter.Strategy.LEGACY_D_TWO_BY_EIGHT,
            LegacyGroupFixtureRouter.strategyFor(10),
        )
    }

    @Test
    fun `unknown group format preserves legacy null route`() {
        assertNull(LegacyGroupFixtureRouter.strategyFor(7))
        assertNull(LegacyGroupFixtureRouter.generate(7, emptyList()))
    }

    @Test
    fun `group router delegates to exact fixed matrix`() {
        val groups = List(2) { group -> List(6) { club -> "g${group + 1}c${club + 1}" } }
        val expected = LegacyGroupFixtureRules.legacyCTwoGroupsOfSix(groups)
        assertEquals(expected, LegacyGroupFixtureRouter.generate(6, groups))
        assertEquals(expected, LegacyGroupFixtureRouter.generate(0x70B, groups))
    }
}

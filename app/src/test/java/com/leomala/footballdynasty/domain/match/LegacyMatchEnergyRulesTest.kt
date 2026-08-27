package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchEnergyRulesTest {
    @Test
    fun `age buckets preserve exact best o u decrements`() {
        val expected = listOf(
            16 to 1,
            20 to 1,
            21 to 2,
            25 to 2,
            26 to 3,
            32 to 3,
            33 to 4,
            36 to 4,
            37 to 5,
            48 to 5,
        )
        expected.forEach { (age, decrement) ->
            assertEquals(decrement, LegacyMatchEnergyRules.decrementForAge(age))
        }
    }

    @Test
    fun `energy clamp preserves zero and only repairs negative result to one`() {
        assertEquals(0, LegacyMatchEnergyRules.updatedEnergy(age = 20, energy = 1))
        assertEquals(1, LegacyMatchEnergyRules.updatedEnergy(age = 20, energy = 0))
        assertEquals(1, LegacyMatchEnergyRules.updatedEnergy(age = 37, energy = 3))
        assertEquals(5, LegacyMatchEnergyRules.updatedEnergy(age = 25, energy = 7))
    }

    @Test
    fun `first period skips legacy g0 one while refreshing both active sides`() {
        val homeG0One = player("home-g0-1", g0 = 1, age = 25, energy = 50)
        val homeOutfield = player("home-outfield", g0 = 2, age = 32, energy = 50)
        val awayOutfield = player("away-outfield", g0 = 18, age = 37, energy = 50)
        val bench = player("bench", g0 = 18, age = 37, energy = 50)
        val state = state(homeG0One, homeOutfield, awayOutfield, bench)

        LegacyMatchEnergyRules.refreshActivePlayers(state, legacyPeriod = 1)

        assertEquals(50, homeG0One.energy)
        assertEquals(47, homeOutfield.energy)
        assertEquals(45, awayOutfield.energy)
        assertEquals(50, bench.energy)
    }

    @Test
    fun `second period includes legacy g0 one and still excludes bench`() {
        val homeG0One = player("home-g0-1", g0 = 1, age = 25, energy = 50)
        val homeOutfield = player("home-outfield", g0 = 2, age = 36, energy = 50)
        val awayOutfield = player("away-outfield", g0 = 18, age = 20, energy = 50)
        val bench = player("bench", g0 = 18, age = 20, energy = 50)
        val state = state(homeG0One, homeOutfield, awayOutfield, bench)

        LegacyMatchEnergyRules.refreshActivePlayers(state, legacyPeriod = 2)

        assertEquals(48, homeG0One.energy)
        assertEquals(46, homeOutfield.energy)
        assertEquals(49, awayOutfield.energy)
        assertEquals(50, bench.energy)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unsupported period is rejected instead of inferred`() {
        val state = state(
            player("h1", 2, 20, 50),
            player("h2", 3, 20, 50),
            player("a1", 4, 20, 50),
            player("bench", 5, 20, 50),
        )
        LegacyMatchEnergyRules.refreshActivePlayers(state, legacyPeriod = 3)
    }

    private fun state(
        homeFirst: LegacyMatchTransientRuntime.Player<String>,
        homeSecond: LegacyMatchTransientRuntime.Player<String>,
        awayFirst: LegacyMatchTransientRuntime.Player<String>,
        bench: LegacyMatchTransientRuntime.Player<String>,
    ): LegacyMatchTransientRuntime.State<String, String> {
        val home = LegacyMatchTransientRuntime.Club(
            value = "home",
            legacyClubId = 101,
            active = mutableListOf(homeFirst, homeSecond),
            bench = mutableListOf(bench),
            substitutionsRemaining = 3,
        )
        val away = LegacyMatchTransientRuntime.Club(
            value = "away",
            legacyClubId = 202,
            active = mutableListOf(awayFirst),
            bench = mutableListOf(),
            substitutionsRemaining = 3,
        )
        return LegacyMatchTransientRuntime.State(1, home, away)
    }

    private fun player(
        id: String,
        g0: Int,
        age: Int,
        energy: Int,
    ) = LegacyMatchTransientRuntime.Player(
        value = id,
        legacyG0 = g0,
        legacyL0 = 1,
        legacyF0 = 0,
        legacyR = 0,
        age = age,
        energy = energy,
        skill = 80,
    )
}

package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyMatchR3EventRoutingRulesTest {
    @Test
    fun `difference E switches from divisor eight to ten at five`() {
        assertEquals(2.0, LegacyMatchR3EventRoutingRules.differenceE(16.0, 0.0, 4), 0.0)
        assertEquals(1.6, LegacyMatchR3EventRoutingRules.differenceE(16.0, 0.0, 5), 0.0)
        assertEquals(1.6, LegacyMatchR3EventRoutingRules.differenceE(16.0, 0.0, 9), 0.0)
    }

    @Test
    fun `B table thresholds preserve b0 c0 d0 e0 and gap override`() {
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.B0, resolveB(h = 0).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.C0, resolveB(h = 3).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.D0, resolveB(h = 5).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.E0, resolveB(h = 6).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.D0, resolveB(h = 2, p0Gap = 2).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.D0, resolveB(h = 6, p0Gap = 2).weightTable)
    }

    @Test
    fun `B zero U with opposite Q0 rounds scaled second before clamp`() {
        val result = resolveB(
            metricU = 0.0,
            metricZ = 0.0,
            metricE = 16.0,
            metricD = 0.0,
            oppositeQ0 = true,
        )

        assertEquals(1.0, result.multipliers[1], 0.0)
    }

    @Test
    fun `B non neutral side zero overwrites third from adjusted second`() {
        val result = resolveB(
            currentSide = 0,
            metricU = 80.0,
            metricZ = 0.0,
            metricE = 0.0,
            metricD = 0.0,
            neutral = false,
        )

        assertEquals(1.1, result.multipliers[1], 1e-12)
        assertEquals(1.2, result.multipliers[2], 1e-12)
    }

    @Test
    fun `B non neutral side one overwrites third from decreased second`() {
        val result = resolveB(currentSide = 1, neutral = false)

        assertEquals(0.9, result.multipliers[1], 1e-12)
        assertEquals(0.8, result.multipliers[2], 1e-12)
    }

    @Test
    fun `B clamps second and third independently to point two`() {
        val result = resolveB(
            metricU = -100.0,
            metricZ = 100.0,
            metricE = -100.0,
            metricD = 100.0,
        )

        assertEquals(0.2, result.multipliers[1], 0.0)
        assertEquals(0.2, result.multipliers[2], 0.0)
    }

    @Test
    fun `B increments S after weighted draw while C increments it before`() {
        assertEquals(
            LegacyMatchR3EventRoutingRules.SIncrementTiming.AFTER_WEIGHTED_DRAW,
            resolveB().sIncrementTiming,
        )
        assertEquals(
            LegacyMatchR3EventRoutingRules.SIncrementTiming.BEFORE_WEIGHTED_DRAW,
            resolveC().sIncrementTiming,
        )
    }

    @Test
    fun `C minute tables switch at thirty and seventy`() {
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.A, resolveC(minute = 29).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.B, resolveC(minute = 30).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.B, resolveC(minute = 69).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.C, resolveC(minute = 70).weightTable)
    }

    @Test
    fun `C h overrides and P0 gap override preserve D through H tables`() {
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.D, resolveC(h = 3).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.E, resolveC(h = 4).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.F, resolveC(h = 5).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.G, resolveC(h = 6).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.H, resolveC(h = 3, p0Gap = 2).weightTable)
        assertEquals(LegacyMatchR3EventRoutingRules.WeightTable.H, resolveC(h = 8, p0Gap = 2).weightTable)
    }

    @Test
    fun `C non neutral side adjustments preserve asymmetric order`() {
        val side0 = resolveC(currentSide = 0, neutral = false, storedG = 1.0)
        assertEquals(0.9, side0.multipliers[1], 1e-12)
        assertEquals(0.9, side0.multipliers[2], 1e-12)

        val side1 = resolveC(currentSide = 1, neutral = false, storedG = 1.0)
        assertEquals(0.9, side1.multipliers[0], 1e-12)
        assertEquals(1.0, side1.multipliers[1], 1e-12)
        assertEquals(1.0, side1.multipliers[2], 1e-12)
    }

    @Test
    fun `C stored G zero forces first multiplier to twenty after side adjustment`() {
        val result = resolveC(currentSide = 1, neutral = false, storedG = 0.0)
        assertEquals(20.0, result.multipliers[0], 0.0)
    }

    @Test
    fun `selected zero materializes goal then increments Y`() {
        val result = resolveB(draw = 0.0)
        assertEquals(0, result.selectedIndex)
        assertTrue(result.materializesGoal)
        assertEquals(
            listOf(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.MATERIALIZE_GOAL_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
            ),
            result.mutations,
        )
    }

    @Test
    fun `selected one increments Y and primary counter only when primary exists`() {
        val withPrimary = resolveB(draw = 0.2, primaryPresent = true)
        assertEquals(1, withPrimary.selectedIndex)
        assertEquals(
            listOf(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_PRIMARY_R0_P,
            ),
            withPrimary.mutations,
        )

        val withoutPrimary = resolveB(draw = 0.2, primaryPresent = false)
        assertEquals(
            listOf(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Y_CURRENT,
            ),
            withoutPrimary.mutations,
        )
    }

    @Test
    fun `selected two increments Z without goal`() {
        val result = resolveB(draw = 0.9)
        assertEquals(2, result.selectedIndex)
        assertFalse(result.materializesGoal)
        assertEquals(
            listOf(
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_S_CURRENT,
                LegacyMatchR3EventRoutingRules.Mutation.INCREMENT_Z_CURRENT,
            ),
            result.mutations,
        )
    }

    @Test
    fun `B and C each consume exactly one weighted nextDouble`() {
        val bRandom = FixedDoubleRandomSource(0.0)
        resolveB(random = bRandom)
        assertEquals(1L, bRandom.draws)
        assertEquals(1, bRandom.doubleCalls)
        assertEquals(0, bRandom.intCalls)

        val cRandom = FixedDoubleRandomSource(0.0)
        resolveC(random = cRandom)
        assertEquals(1L, cRandom.draws)
        assertEquals(1, cRandom.doubleCalls)
        assertEquals(0, cRandom.intCalls)
    }

    private fun resolveB(
        currentSide: Int = 0,
        metricU: Double = 0.0,
        metricZ: Double = 0.0,
        metricE: Double = 0.0,
        metricD: Double = 0.0,
        oppositeQ0: Boolean = false,
        neutral: Boolean = true,
        h: Int = 0,
        p0Gap: Int = 0,
        primaryPresent: Boolean = true,
        globalJ: Int = 0,
        draw: Double = 0.0,
        random: FixedDoubleRandomSource = FixedDoubleRandomSource(draw),
    ) = LegacyMatchR3EventRoutingRules.resolveB(
        currentSide = currentSide,
        metricUOpposite = metricU,
        metricZCurrent = metricZ,
        metricEOpposite = metricE,
        metricDCurrent = metricD,
        oppositeClubQ0Flag = oppositeQ0,
        legacyNeutralFlag = neutral,
        legacyHCurrent = h,
        oppositeMinusCurrentP0 = p0Gap,
        primaryPlayerPresent = primaryPresent,
        legacyGlobalJValue = globalJ,
        random = random,
    )

    private fun resolveC(
        currentSide: Int = 0,
        minute: Int = 0,
        metricD: Double = 0.0,
        metricE: Double = 0.0,
        storedG: Double = 1.0,
        neutral: Boolean = true,
        h: Int = 0,
        p0Gap: Int = 0,
        primaryPresent: Boolean = true,
        globalJ: Int = 0,
        draw: Double = 0.0,
        random: FixedDoubleRandomSource = FixedDoubleRandomSource(draw),
    ) = LegacyMatchR3EventRoutingRules.resolveC(
        currentSide = currentSide,
        currentMinute = minute,
        metricDCurrent = metricD,
        metricEOpposite = metricE,
        storedLegacyG = storedG,
        legacyNeutralFlag = neutral,
        legacyHCurrent = h,
        oppositeMinusCurrentP0 = p0Gap,
        primaryPlayerPresent = primaryPresent,
        legacyGlobalJValue = globalJ,
        random = random,
    )

    private class FixedDoubleRandomSource(private val value: Double) : RandomSource {
        var doubleCalls = 0
            private set
        var intCalls = 0
            private set
        override var draws: Long = 0
            private set

        override fun nextDouble(): Double {
            doubleCalls++
            draws++
            return value
        }

        override fun nextInt(bound: Int): Int {
            intCalls++
            draws++
            error("not expected")
        }

        override fun nextBoolean(): Boolean = error("not used")
    }
}

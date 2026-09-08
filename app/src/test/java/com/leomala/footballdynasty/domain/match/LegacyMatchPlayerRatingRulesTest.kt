package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchPlayerRatingRulesTest {
    @Test
    fun `participation traversal preserves default primary secondary formulas and last match wins`() {
        val marker = LegacyMatchPlayerRatingRules.resolveParticipationMarker(
            listOf(
                LegacyMatchPlayerRatingRules.ParticipationEvent(
                    primaryIsPlayer = true,
                    secondaryIsPlayer = false,
                    legacyPeriod = 1,
                    legacyMinute = 12,
                ),
                LegacyMatchPlayerRatingRules.ParticipationEvent(
                    primaryIsPlayer = false,
                    secondaryIsPlayer = true,
                    legacyPeriod = 2,
                    legacyMinute = 31,
                ),
            ),
        )

        assertEquals(19, marker)
        assertEquals(90, LegacyMatchPlayerRatingRules.resolveParticipationMarker(emptyList()))
    }

    @Test
    fun `secondary player overwrites primary marker in the same legacy event`() {
        val marker = LegacyMatchPlayerRatingRules.resolveParticipationMarker(
            listOf(
                LegacyMatchPlayerRatingRules.ParticipationEvent(
                    primaryIsPlayer = true,
                    secondaryIsPlayer = true,
                    legacyPeriod = 1,
                    legacyMinute = 12,
                ),
            ),
        )

        assertEquals(86, marker)
    }

    @Test
    fun `draw baseline and unresolved S preserve exact legacy initialization`() {
        val random = QueueRandomSource()
        val result = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyPlayerJ = 30,
                legacyS = 0,
                legacyGCategory = 4,
                currentGoals = 1,
                opponentGoals = 1,
            ),
            implicitRandom = random,
        )

        assertEquals(23, result.resolvedLegacyS)
        assertEquals(5.5, result.ratingY0, 0.0)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `loss baseline preserves the legacy sixty one through ninety plateau`() {
        val withinPlateau = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyPlayerJ = 61,
                legacyS = 23,
                currentGoals = 0,
                opponentGoals = 1,
            ),
            implicitRandom = QueueRandomSource(),
        )
        val afterPlateau = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyPlayerJ = 91,
                legacyS = 23,
                currentGoals = 0,
                opponentGoals = 1,
            ),
            implicitRandom = QueueRandomSource(),
        )

        assertEquals(5.5, withinPlateau.ratingY0, 0.0)
        assertEquals(6.0, afterPlateau.ratingY0, 0.0)
    }

    @Test
    fun `n2 getter contributions preserve k condition with i subtraction quirk`() {
        val n2 = LegacyMatchN2CounterRules.GetterSnapshot(
            legacyA = 1,
            legacyB = 1,
            legacyC = 1,
            legacyD = 0,
            legacyE = 1,
            legacyF = 0,
            legacyG = 0,
            legacyH = 2,
            legacyI = 1,
            legacyJ = 0,
            legacyK = 1,
        )
        val result = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyS = 14,
                currentGoals = 1,
                opponentGoals = 1,
                star = true,
                worldTop = true,
                n2 = n2,
            ),
            implicitRandom = QueueRandomSource(),
        )

        assertEquals(5.3, result.ratingY0, 0.0000001)
    }

    @Test
    fun `legacy S one applies opponent Y W and score adjustments without invented draws`() {
        val random = QueueRandomSource()
        val result = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyS = 1,
                currentGoals = 1,
                opponentGoals = 1,
                opponentLegacyW = 11,
                opponentLegacyY = 2,
            ),
            implicitRandom = random,
        )

        assertEquals(4.8, result.ratingY0, 0.0000001)
        assertEquals(emptyList<Int>(), random.bounds)
    }

    @Test
    fun `S twelve preserves exact implicit random draw order across E q0 and final penalty`() {
        val random = QueueRandomSource(1, 1, 1, 1)
        val result = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyS = 12,
                legacyF = 0,
                legacyGCode = 11,
                currentGoals = 1,
                opponentGoals = 1,
                currentLegacyE = 2,
                opponentLegacyE = 1,
                currentLegacyQ0 = 2,
                opponentLegacyQ0 = 1,
            ),
            implicitRandom = random,
        )

        assertEquals(7.7, result.ratingY0, 0.0000001)
        assertEquals(listOf(3, 3, 3, 3), random.bounds)
    }

    @Test
    fun `very short participation converts the clamped floor two to legacy zero`() {
        val result = LegacyMatchPlayerRatingRules.resolve(
            input = input(
                legacyS = 23,
                legacyP0Flag = true,
                legacyG0 = 1,
                currentGoals = 1,
                opponentGoals = 1,
                participationMarker = 10,
            ),
            implicitRandom = QueueRandomSource(),
        )

        assertEquals(0.0, result.ratingY0, 0.0)
    }

    private fun input(
        legacyPlayerJ: Int = 30,
        legacyP0Flag: Boolean = false,
        legacyG0: Int = 2,
        legacyS: Int = 23,
        legacyGCategory: Int = 4,
        legacyF: Int = 1,
        legacyGCode: Int = 0,
        currentGoals: Int = 0,
        opponentGoals: Int = 0,
        currentLegacyE: Int = 0,
        opponentLegacyE: Int = 0,
        currentLegacyQ0: Int = 0,
        opponentLegacyQ0: Int = 0,
        opponentLegacyW: Int = 0,
        opponentLegacyY: Int = 1,
        star: Boolean = false,
        worldTop: Boolean = false,
        n2: LegacyMatchN2CounterRules.GetterSnapshot = LegacyMatchN2CounterRules.snapshot(
            LegacyMatchN2CounterRules.State(),
        ),
        participationMarker: Int = 90,
    ) = LegacyMatchPlayerRatingRules.Input(
        legacyPlayerJ = legacyPlayerJ,
        legacyP0Flag = legacyP0Flag,
        legacyG0 = legacyG0,
        legacyS = legacyS,
        legacyGCategory = legacyGCategory,
        legacyF = legacyF,
        legacyGCode = legacyGCode,
        currentGoals = currentGoals,
        opponentGoals = opponentGoals,
        currentLegacyE = currentLegacyE,
        opponentLegacyE = opponentLegacyE,
        currentLegacyQ0 = currentLegacyQ0,
        opponentLegacyQ0 = opponentLegacyQ0,
        opponentLegacyW = opponentLegacyW,
        opponentLegacyY = opponentLegacyY,
        star = star,
        worldTop = worldTop,
        n2 = n2,
        participationMarker = participationMarker,
    )

    private class QueueRandomSource(vararg values: Int) : RandomSource {
        private val queue = values.toMutableList()
        val bounds = mutableListOf<Int>()
        override var draws: Long = 0
            private set

        override fun nextInt(bound: Int): Int {
            check(queue.isNotEmpty()) { "No queued implicit RNG value for bound=$bound" }
            val value = queue.removeAt(0)
            require(value in 0 until bound) { "value=$value bound=$bound" }
            bounds += bound
            draws++
            return value
        }

        override fun nextBoolean(): Boolean = error("not used")
        override fun nextDouble(): Double = error("not used")
    }
}

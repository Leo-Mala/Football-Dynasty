package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for legacy match scheduling recovered from `best.s.m()` and `best.s.Q0()`. */
object LegacyMatchScheduleRules {
    data class Pools(
        val coreMinutes: MutableList<Int>,
        val earlyMinutes: MutableList<Int>,
        val middleMinutes: MutableList<Int>,
        val lateMinutes: MutableList<Int>,
        val endMinutes: MutableList<Int>,
    ) {
        companion object {
            fun initial(): Pools = Pools(
                coreMinutes = (19..38).toMutableList(),
                earlyMinutes = (5..15).toMutableList(),
                middleMinutes = (16..35).toMutableList(),
                lateMinutes = (36..42).toMutableList(),
                endMinutes = (43..47).toMutableList(),
            )
        }
    }

    data class Schedule(
        val core: List<List<Int>>,
        val auxiliary: List<List<Int>>,
    )

    fun initialize(random: RandomSource, pools: Pools): Schedule {
        LegacyMatchRandomRules.shuffleInPlace(pools.coreMinutes, random)
        val bucketDraw = random.nextInt(100)

        val core = Array(2) { IntArray(3) { -1 } }
        core[0][0] = pools.coreMinutes[0]
        core[0][1] = pools.coreMinutes[1]
        core[1][0] = pools.coreMinutes[2]
        core[1][1] = pools.coreMinutes[3]
        if (random.nextInt(100) > 30) {
            core[0][2] = pools.coreMinutes[4]
        }
        if (random.nextInt(100) > 30) {
            core[1][2] = pools.coreMinutes[5]
        }

        val selectedPool = when {
            bucketDraw > 90 -> pools.earlyMinutes
            bucketDraw > 50 -> pools.middleMinutes
            else -> pools.lateMinutes
        }
        LegacyMatchRandomRules.shuffleInPlace(selectedPool, random)
        val auxiliary = Array(2) { IntArray(4) { -1 } }
        auxiliary[0][0] = selectedPool[0]
        auxiliary[0][1] = selectedPool[1]
        auxiliary[1][0] = selectedPool[2]
        auxiliary[1][1] = selectedPool[3]

        LegacyMatchRandomRules.shuffleInPlace(pools.endMinutes, random)
        if (random.nextInt(100) > 20) auxiliary[0][2] = pools.endMinutes[0]
        if (random.nextInt(100) > 50) auxiliary[0][3] = pools.endMinutes[1]
        if (random.nextInt(100) > 20) auxiliary[1][2] = pools.endMinutes[2]
        if (random.nextInt(100) > 50) auxiliary[1][3] = pools.endMinutes[3]

        return Schedule(
            core = core.map { it.toList() },
            auxiliary = auxiliary.map { it.toList() },
        )
    }

    /**
     * Recovered `best.s.Q0()` first-half added-time draw.
     *
     * This remains separate from the second-half draw intentionally: Q0 performs minute simulation
     * between them, so drawing both values up front would change the legacy RNG consumption order.
     */
    fun drawAutomaticFirstHalfAddedMinutes(random: RandomSource): Int = random.nextInt(3)

    /** Recovered `best.s.Q0()` second-half added-time draw: the legacy range is 1..5 inclusive. */
    fun drawAutomaticSecondHalfAddedMinutes(random: RandomSource): Int = random.nextInt(5) + 1

    /**
     * Structural `Q0()` landmark order proven by SMALI without inventing minute-loop boundaries.
     *
     * The callbacks deliberately own the still-unrecovered minute ranges. This method only freezes the
     * proven ordering: first added-time draw -> first-half simulation -> `j(2, 0)` halftime transition
     * -> second added-time draw -> second-half simulation. RNG consumed by either simulation callback
     * therefore remains between the two added-time draws exactly where the legacy method consumes it.
     */
    fun runAutomaticFlowLandmarks(
        random: RandomSource,
        simulateFirstHalf: (addedMinutes: Int) -> Unit,
        halftimeTransition: (half: Int, minute: Int) -> Unit,
        simulateSecondHalf: (addedMinutes: Int) -> Unit,
    ) {
        val firstHalfAdded = drawAutomaticFirstHalfAddedMinutes(random)
        simulateFirstHalf(firstHalfAdded)
        halftimeTransition(2, 0)
        val secondHalfAdded = drawAutomaticSecondHalfAddedMinutes(random)
        simulateSecondHalf(secondHalfAdded)
    }
}

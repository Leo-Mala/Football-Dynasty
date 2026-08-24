package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Pure structural reconstruction of the RNG-relevant annual path through legacy `best.p`. */
object LegacyProceduralPlayerRules {
    data class TargetContext(
        val legacyF0: Int,
        val legacyP0: Int,
        val legacyR0: Boolean,
        val legacyO: Int,
        val legacyD: Int,
        val requestedLegacyE: Int,
    )

    data class LegacyPair(val first: Int, val second: Int)
    data class LegacyESelection(val rawRoll1To100: Int, val legacyE: Int)
    data class LegacyDExecution(
        val legacyF: Int,
        val intermediateLegacyO: Int,
        val intermediateLegacyM: Int,
    )

    data class Draft(
        val legacyN: Int,
        val legacyB: Boolean,
        val legacyC: Int,
        val legacyE: Int,
        val legacyJ: Int,
        val legacyL: Int,
        val legacyD: Int,
        val name: String,
        val legacyG: Int,
        val legacyF: Int,
        val legacyO: Int,
        val legacyM: Int,
    )

    private val positionPairs = arrayOf(
        arrayOf(0 to 3, 0 to 1, 2 to 0, 1 to 2, 3 to 1),
        arrayOf(6 to 10, 6 to 13, 10 to 11, 10 to 13),
        arrayOf(7 to 10, 7 to 12, 7 to 5, 10 to 13),
        arrayOf(4 to 11, 4 to 9, 9 to 11, 11 to 9, 4 to 8, 4 to 13, 7 to 10, 7 to 11, 7 to 5, 7 to 13, 10 to 13, 10 to 11),
        arrayOf(9 to 5, 13 to 9, 8 to 9, 9 to 13, 9 to 8, 5 to 13),
    )

    /** Exact `p.n` bucket selected from the first `nextInt(100)+1` draw in `best.p.d`. */
    fun initialLegacyN(targetF0: Int, targetP0: Int, roll1To100: Int): Int {
        require(roll1To100 in 1..100)
        val lowBand = targetF0 < 19 && targetP0 <= 3
        if (!lowBand) {
            return when (roll1To100) {
                in 1..2 -> 1
                in 3..5 -> 2
                in 6..10 -> 4
                in 11..25 -> 5
                in 26..60 -> 6
                in 61..80 -> 7
                in 81..90 -> 8
                in 91..98 -> 9
                else -> 10
            }
        }
        return if (targetF0 >= 15) {
            when (roll1To100) {
                in 1..2 -> 1
                in 3..5 -> 2
                in 6..10 -> 4
                in 11..30 -> 5
                in 31..65 -> 6
                in 66..90 -> 7
                in 91..95 -> 8
                in 96..98 -> 9
                else -> 10
            }
        } else {
            when (roll1To100) {
                in 1..4 -> 1
                in 5..8 -> 2
                in 9..15 -> 3
                in 16..25 -> 4
                in 26..50 -> 5
                in 51..75 -> 6
                in 76..95 -> 7
                in 96..99 -> 8
                else -> 9
            }
        }
    }

    fun initialLegacyB(roll1To100: Int): Boolean = roll1To100 == 1

    fun rollLegacyC(random: RandomSource): Int = random.nextInt(4) + 16

    /** Consumes the position draw before applying the caller override, exactly like SMALI. */
    fun rollLegacyE(random: RandomSource, requestedLegacyE: Int): LegacyESelection {
        val roll = random.nextInt(100) + 1
        val generated = when (roll) {
            in 1..10 -> 0
            in 11..30 -> 1
            in 31..50 -> 2
            in 51..80 -> 3
            else -> 4
        }
        return LegacyESelection(
            rawRoll1To100 = roll,
            legacyE = if (requestedLegacyE >= 0) requestedLegacyE else generated,
        )
    }

    /** Exact random pair table from legacy `best.o.z(int)`. */
    fun rollLegacyPair(random: RandomSource, legacyE: Int): LegacyPair {
        require(legacyE in positionPairs.indices) { "legacyE must be 0..4" }
        val candidates = positionPairs[legacyE]
        val selected = candidates[random.nextInt(candidates.size)]
        return LegacyPair(selected.first, selected.second)
    }

    /** Special `p.d` rewrite of legacy `d`; consumes no RNG unless the bytecode branch is reached. */
    fun rewriteLegacyD(
        random: RandomSource,
        targetD: Int,
        targetF0: Int,
        rawPositionRoll1To100: Int,
    ): Int {
        if (rawPositionRoll1To100 != 1 || targetF0 < 18) return targetD
        val selector = random.nextInt(6)
        if (targetD == 29) {
            return when (selector) {
                0 -> 11
                1 -> 43
                2 -> 195
                3 -> 150
                4 -> 84
                else -> random.nextInt(200)
            }
        }
        if (targetD in setOf(3, 154, 85, 104, 72)) {
            return when (selector) {
                0 -> 3
                1 -> 154
                2 -> 85
                3 -> 104
                4 -> 174
                else -> 72
            }
        }
        return targetD
    }

    fun maybeRewriteLegacyNForChangedD(
        random: RandomSource,
        currentN: Int,
        targetD: Int,
        generatedD: Int,
    ): Int = if (generatedD != targetD) random.nextInt(4) + 7 else currentN

    fun rollLegacyG(random: RandomSource): Int = random.nextInt(2)

    /** Exact RNG/value transform of legacy `best.p.h()`. */
    fun legacyH(random: RandomSource, legacyC: Int, legacyN: Int): Int {
        val base = when (legacyC) {
            16 -> 15
            17 -> 35
            18 -> 55
            19 -> 70
            20 -> 75
            else -> 0
        }
        val value = base + random.nextInt(5) + 1 + legacyN
        val atLeastOne = value.coerceAtLeast(1)
        return if (atLeastOne > 100) 95 else atLeastOne
    }

    /** Exact RNG/value transform of legacy `best.p.g()`. */
    fun legacyG(random: RandomSource, legacyN: Int): Int {
        val roll = random.nextInt(100) + 1
        val value = when {
            roll <= 15 -> legacyN
            roll <= 60 -> legacyN - 1
            else -> legacyN + 1
        }
        return value.coerceIn(1, 10)
    }

    /**
     * RNG-relevant part of legacy `best.p.D(c0)`. Calls to h()/g() happen before B/f/e and their
     * results are later overwritten by `p.d`, but their draws are still observable and preserved.
     */
    fun executeLegacyD(
        random: RandomSource,
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
        targetF0: Int,
        legacyB: Boolean,
        legacyC: Int,
        legacyN: Int,
    ): LegacyDExecution {
        val tierOffset = if (targetR0) {
            when (targetO) {
                1 -> 20
                2 -> 15
                3 -> 5
                else -> 1
            }
        } else {
            when (targetP0) {
                1, 2, 3 -> 5
                4 -> 15
                5 -> 22
                else -> 1
            }
        }
        val f0Contribution = when (targetF0) {
            in Int.MIN_VALUE..15 -> targetF0
            16 -> 17
            17 -> 18
            18 -> 19
            19 -> 21
            20 -> 25
            21 -> 26
            22 -> 27
            23 -> 28
            24 -> 29
            25 -> 30
            else -> 0
        }
        var legacyF = f0Contribution + tierOffset + random.nextInt(3)
        if (legacyB) legacyF += 9 + random.nextInt(3)
        legacyF -= 23
        if (legacyF < 5) legacyF = 10

        val intermediateO = legacyH(random, legacyC, legacyN)
        val intermediateM = legacyG(random, legacyN)
        legacyF = legacyF.coerceAtMost(100)
        return LegacyDExecution(legacyF, intermediateO, intermediateM)
    }

    /** Annual `p.d(target, requestedE, null, 0, null, FALSE)` structural path, including RNG order. */
    fun generateAnnualDraft(
        random: RandomSource,
        target: TargetContext,
        resolveName: (legacyD: Int, random: RandomSource) -> String,
    ): Draft {
        val firstRoll = random.nextInt(100) + 1
        var legacyN = initialLegacyN(target.legacyF0, target.legacyP0, firstRoll)
        val legacyB = initialLegacyB(firstRoll)
        val legacyC = rollLegacyC(random)
        val eSelection = rollLegacyE(random, target.requestedLegacyE)
        val pair = rollLegacyPair(random, eSelection.legacyE)
        val legacyD = rewriteLegacyD(random, target.legacyD, target.legacyF0, eSelection.rawRoll1To100)
        legacyN = maybeRewriteLegacyNForChangedD(random, legacyN, target.legacyD, legacyD)
        val name = resolveName(legacyD, random)
        val legacyG = rollLegacyG(random)
        val dExecution = executeLegacyD(
            random = random,
            targetR0 = target.legacyR0,
            targetO = target.legacyO,
            targetP0 = target.legacyP0,
            targetF0 = target.legacyF0,
            legacyB = legacyB,
            legacyC = legacyC,
            legacyN = legacyN,
        )
        val finalO = legacyH(random, legacyC, legacyN)
        val finalM = legacyG(random, legacyN)
        return Draft(
            legacyN = legacyN,
            legacyB = legacyB,
            legacyC = legacyC,
            legacyE = eSelection.legacyE,
            legacyJ = pair.first,
            legacyL = pair.second,
            legacyD = legacyD,
            name = name,
            legacyG = legacyG,
            legacyF = dExecution.legacyF,
            legacyO = finalO,
            legacyM = finalM,
        )
    }
}

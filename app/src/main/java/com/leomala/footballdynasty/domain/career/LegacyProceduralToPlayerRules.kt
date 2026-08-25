package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Pure reconstruction of the RNG/control portion of legacy `best.t.e(boolean, p, c0)`. */
object LegacyProceduralToPlayerRules {
    data class LegacyFlags(
        val setO0: Boolean,
        val setM: Boolean,
    )

    fun clubBand(
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
        targetJ: Int,
    ): Int {
        val base = if (targetR0) {
            when {
                targetO < 1 -> 5
                targetO == 1 -> 22
                targetO == 2 -> 17
                targetO == 3 -> 14
                else -> 7
            }
        } else {
            when (targetP0) {
                5 -> 20
                4 -> 15
                3 -> 12
                2 -> 7
                else -> 5
            }
        }
        return if (targetJ == 0) base + 5 else base
    }

    /**
     * Reproduces `round(p.p()/100 * (clubBand + nextInt(5))) + p.v()` and its optional
     * `nextInt(10)` when legacy `p.v() >= 9`.
     */
    fun convertedLegacyN(
        random: RandomSource,
        targetR0: Boolean,
        targetO: Int,
        targetP0: Int,
        targetJ: Int,
        draftLegacyN: Int,
        draftLegacyO: Int,
    ): Int {
        val band = clubBand(targetR0, targetO, targetP0, targetJ) + random.nextInt(5)
        val scaled = Math.round((draftLegacyO.toDouble() / 100.0) * band.toDouble()).toInt()
        var result = draftLegacyN + scaled
        if (draftLegacyN >= 9) result += random.nextInt(10)
        return result
    }

    /** Exact mutually-exclusive flag RNG at the end of `best.t.e`. */
    fun flags(random: RandomSource, draftLegacyB: Boolean): LegacyFlags {
        if (draftLegacyB) {
            return LegacyFlags(
                setO0 = random.nextInt(3) == 1,
                setM = false,
            )
        }
        if (random.nextInt(200) == 1) {
            return LegacyFlags(setO0 = true, setM = false)
        }
        return if (random.nextInt(300) == 1) {
            LegacyFlags(setO0 = true, setM = true)
        } else {
            LegacyFlags(setO0 = false, setM = false)
        }
    }

    /** Final `O0 && t0() < 8 -> J1(8)` effect after the flag block. */
    fun finalLegacyNAfterFlags(currentLegacyN: Int, setO0: Boolean): Int =
        if (setO0 && currentLegacyN < 8) 8 else currentLegacyN
}

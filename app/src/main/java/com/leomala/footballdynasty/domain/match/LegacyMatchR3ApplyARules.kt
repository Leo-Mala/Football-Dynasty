package com.leomala.footballdynasty.domain.match

/** Exact state update performed by reachable legacy `components.r3.a(side)`. */
object LegacyMatchR3ApplyARules {
    data class Result(
        val updatedLegacyB0: List<Int>,
        val updatedLegacyE: List<Int>,
    )

    fun apply(
        legacyB0: List<Int>,
        side: Int,
    ): Result {
        val updated = legacyB0.toMutableList()
        updated[side] = updated[side] + 1
        val total = updated[0] + updated[1]
        return Result(
            updatedLegacyB0 = updated,
            updatedLegacyE = listOf(
                legacyPercent(updated[0], total),
                legacyPercent(updated[1], total),
            ),
        )
    }

    /** SMALI for `best.j0.e(II)` converts both ints to float before division, then Math.round(float). */
    fun legacyPercent(value: Int, total: Int): Int =
        Math.round((value.toFloat() / total.toFloat()) * 100.0f)
}

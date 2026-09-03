package com.leomala.footballdynasty.domain.manager

/**
 * Exact reconstruction of legacy `best.k.a(int)` used by `new best.k(name, capacity, club)`.
 *
 * Source capacity outside 1,000..120,000 is replaced by 10,000. The four sectors are then derived
 * in legacy order (15%, remainder, 9%, 0.9%) and individually capped without redistributing any
 * amount removed by a cap.
 */
object LegacyStadiumInitialSectorRule {
    private val caps = intArrayOf(18_000, 80_000, 9_000, 700)

    fun fromAggregateCapacity(rawCapacity: Int): List<Int> {
        val capacity = if (rawCapacity < 1_000 || rawCapacity > 120_000) 10_000 else rawCapacity
        val asDouble = capacity.toDouble()
        val sectors = IntArray(4)
        sectors[0] = Math.round(0.15 * asDouble).toInt()
        sectors[2] = Math.round(0.09 * asDouble).toInt()
        sectors[3] = Math.round(0.009 * asDouble).toInt()
        sectors[1] = capacity - (sectors[0] + sectors[2] + sectors[3])
        for (index in sectors.indices) {
            if (sectors[index] > caps[index]) sectors[index] = caps[index]
        }
        return sectors.toList()
    }
}

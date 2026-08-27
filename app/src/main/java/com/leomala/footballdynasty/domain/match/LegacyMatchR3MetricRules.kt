package com.leomala.footballdynasty.domain.match

/** Pure aggregation helpers recovered from reachable `components.r3.y/u/z`. */
object LegacyMatchR3MetricRules {
    data class PlayerMetric(
        val legacyPositionIndex: Int,
        val legacyGValue: Double,
    )

    fun metricY(
        players: List<PlayerMetric>,
        legacyClubI0Index2: Int,
    ): Double {
        val bucket = if (legacyClubI0Index2 < 3) legacyClubI0Index2 else 2
        val bonus = doubleArrayOf(0.0, 0.04, 0.08)[bucket]
        var sum = bonus
        var count = 0
        for (player in players) {
            if (count < 5 && player.legacyPositionIndex in 10..17) {
                sum += player.legacyGValue
                count++
            }
        }
        val averageOverFive = sum / 5.0
        return if (count < 3) 0.01 else averageOverFive
    }

    fun metricU(players: List<PlayerMetric>): Double {
        var sum = 0.0
        var count = 0
        for (player in players) {
            if (count < 5 && player.legacyPositionIndex in 2..9) {
                sum += player.legacyGValue
                count++
            }
        }
        val averageOverFive = sum / 5.0
        return if (count < 3) 0.1 else averageOverFive
    }

    fun metricZ(players: List<PlayerMetric>): Double {
        var sum = 0.0
        var count = 0
        for (player in players) {
            if (count < 3 && player.legacyPositionIndex in 19..25) {
                sum += player.legacyGValue
                count++
            }
        }
        val averageOverThree = sum / 3.0
        return if (count < 1) 0.0 else averageOverThree
    }
}

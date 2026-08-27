package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchR3MetricRulesTest {
    @Test
    fun `y returns point zero one when fewer than three eligible players exist`() {
        val players = listOf(metric(10, 9.0), metric(17, 7.0))

        val value = LegacyMatchR3MetricRules.metricY(players, legacyClubI0Index2 = 2)

        assertEquals(0.01, value, 0.0)
    }

    @Test
    fun `y applies club bonus then divides by five once three players exist`() {
        val players = listOf(metric(10, 1.0), metric(11, 2.0), metric(17, 3.0))

        assertEquals(1.208, LegacyMatchR3MetricRules.metricY(players, 1), 1e-12)
        assertEquals(1.216, LegacyMatchR3MetricRules.metricY(players, 2), 1e-12)
        assertEquals(1.216, LegacyMatchR3MetricRules.metricY(players, 9), 1e-12)
    }

    @Test
    fun `y consumes only first five eligible positions in list order`() {
        val players = listOf(
            metric(9, 100.0),
            metric(10, 1.0), metric(11, 2.0), metric(12, 3.0), metric(13, 4.0), metric(14, 5.0),
            metric(15, 100.0),
        )

        assertEquals(3.0, LegacyMatchR3MetricRules.metricY(players, 0), 1e-12)
    }

    @Test(expected = ArrayIndexOutOfBoundsException::class)
    fun `negative y club bucket preserves legacy array failure instead of normalizing`() {
        LegacyMatchR3MetricRules.metricY(emptyList(), -1)
    }

    @Test
    fun `u returns point one below three defenders`() {
        assertEquals(
            0.1,
            LegacyMatchR3MetricRules.metricU(listOf(metric(2, 50.0), metric(9, 50.0))),
            0.0,
        )
    }

    @Test
    fun `u uses first five positions two through nine and divides by five`() {
        val players = listOf(
            metric(1, 100.0),
            metric(2, 1.0), metric(3, 2.0), metric(4, 3.0), metric(5, 4.0), metric(9, 5.0),
            metric(8, 100.0),
        )

        assertEquals(3.0, LegacyMatchR3MetricRules.metricU(players), 1e-12)
    }

    @Test
    fun `z returns zero with no position nineteen through twenty five`() {
        assertEquals(0.0, LegacyMatchR3MetricRules.metricZ(listOf(metric(18, 9.0))), 0.0)
    }

    @Test
    fun `z divides one or two eligible values by three rather than by count`() {
        assertEquals(2.0, LegacyMatchR3MetricRules.metricZ(listOf(metric(19, 6.0))), 1e-12)
        assertEquals(3.0, LegacyMatchR3MetricRules.metricZ(listOf(metric(19, 3.0), metric(25, 6.0))), 1e-12)
    }

    @Test
    fun `z stops after first three eligible players`() {
        val players = listOf(metric(19, 3.0), metric(20, 6.0), metric(25, 9.0), metric(21, 300.0))

        assertEquals(6.0, LegacyMatchR3MetricRules.metricZ(players), 1e-12)
    }

    private fun metric(position: Int, value: Double) =
        LegacyMatchR3MetricRules.PlayerMetric(position, value)
}

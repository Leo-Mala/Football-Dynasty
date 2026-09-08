package com.leomala.footballdynasty.domain.competition

import com.leomala.footballdynasty.foundation.random.SeededRandomSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LegacyLeagueTransientRatingCaptureRulesTest {
    @Test
    fun `capture preserves z2 fields and consumes one implicit random draw`() {
        val random = SeededRandomSource(123L)

        val capture = LegacyLeagueTransientRatingCaptureRules.capture(
            legacyCompetitionType = 1,
            isLegacyKonrentT = true,
            ratingY0 = 8.25,
            legacyG0 = 14,
            implicitRandom = random,
        )

        assertNotNull(capture)
        assertEquals(8.25, requireNotNull(capture).ratingY0, 0.0)
        assertEquals(14, capture.legacyG0)
        assertEquals(1L, random.draws)
        assertEquals(capture.randomOrder, capture.randomOrder.coerceIn(0, 99))
    }

    @Test
    fun `caller gates do not construct z2 or consume implicit random`() {
        val zeroRatingRandom = SeededRandomSource(1L)
        val wrongTypeRandom = SeededRandomSource(2L)
        val nonLeagueRuntimeRandom = SeededRandomSource(3L)

        assertNull(
            LegacyLeagueTransientRatingCaptureRules.capture(
                legacyCompetitionType = 1,
                isLegacyKonrentT = true,
                ratingY0 = 0.0,
                legacyG0 = 1,
                implicitRandom = zeroRatingRandom,
            )
        )
        assertNull(
            LegacyLeagueTransientRatingCaptureRules.capture(
                legacyCompetitionType = 2,
                isLegacyKonrentT = true,
                ratingY0 = 7.0,
                legacyG0 = 1,
                implicitRandom = wrongTypeRandom,
            )
        )
        assertNull(
            LegacyLeagueTransientRatingCaptureRules.capture(
                legacyCompetitionType = 1,
                isLegacyKonrentT = false,
                ratingY0 = 7.0,
                legacyG0 = 1,
                implicitRandom = nonLeagueRuntimeRandom,
            )
        )

        assertEquals(0L, zeroRatingRandom.draws)
        assertEquals(0L, wrongTypeRandom.draws)
        assertEquals(0L, nonLeagueRuntimeRandom.draws)
    }
}

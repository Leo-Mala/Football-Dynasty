package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.error.CareerIntegrityException
import com.leomala.footballdynasty.foundation.error.SeasonBoundaryRequiredException
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerIntegrityTest {
    @Test
    fun `invalid season year is rejected instead of repaired silently`() {
        val invalid = CareerStateFactory.create("invalid", 1L).copy(
            season = SeasonState(number = 1, year = 2030),
        )
        val error = runCatching { CareerIntegrityValidator.validate(invalid) }.exceptionOrNull()
        assertTrue(error is CareerIntegrityException)
    }

    @Test
    fun `unknown managed club is rejected when reference set is supplied`() {
        val state = CareerStateFactory.create("refs", 1L, managedClubId = "missing-club")
        val error = runCatching {
            CareerIntegrityValidator.validate(state, knownClubIds = setOf("other-club"))
        }.exceptionOrNull()
        assertTrue(error is CareerIntegrityException)
    }

    @Test
    fun `day cursor cannot silently cross legacy season boundary`() {
        val initial = CareerStateFactory.create("boundary", 1L)
        val atLastDay = initial.copy(
            calendar = initial.calendar.copy(currentDayIndex = initial.calendar.dayCount - 1),
        )
        val error = runCatching {
            CareerSimulationEngine().apply(atLastDay, CareerCommand.AdvanceOneDay)
        }.exceptionOrNull()
        assertTrue(error is SeasonBoundaryRequiredException)
    }
}

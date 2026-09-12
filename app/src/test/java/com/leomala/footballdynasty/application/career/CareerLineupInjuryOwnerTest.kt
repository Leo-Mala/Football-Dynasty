package com.leomala.footballdynasty.application.career

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerLineupInjuryOwnerTest {
    @Test
    fun `legacy M0 owner keeps exact strict injury deadline comparison`() {
        val current = 20_000L
        assertFalse(CareerLineupInputCatalogStore.blockedByLegacyM0(0L, current))
        assertFalse(CareerLineupInputCatalogStore.blockedByLegacyM0(current - 1L, current))
        assertFalse(CareerLineupInputCatalogStore.blockedByLegacyM0(current, current))
        assertTrue(CareerLineupInputCatalogStore.blockedByLegacyM0(current + 1L, current))
    }

    @Test
    fun `negative modern injury deadline fails closed`() {
        assertThrows(IllegalArgumentException::class.java) {
            CareerLineupInputCatalogStore.blockedByLegacyM0(-1L, 20_000L)
        }
    }
}

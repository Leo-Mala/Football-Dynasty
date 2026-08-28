package com.leomala.footballdynasty.domain.match

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyMatchPersistedPlayerRulesTest {
    @Test
    fun `legacy l0 keeps persisted position exactly`() {
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyL0(0))
        assertEquals(4, LegacyMatchPersistedPlayerRules.legacyL0(4))
        assertEquals(17, LegacyMatchPersistedPlayerRules.legacyL0(17))
    }

    @Test
    fun `legacy f0 normalizes every nonzero side to one`() {
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyF0(0))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyF0(1))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyF0(2))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyF0(-1))
    }

    @Test
    fun `legacy R position zero and two always resolve zero`() {
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 0, cr1 = 13, cr2 = 13))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 2, cr1 = 8, cr2 = 8))
    }

    @Test
    fun `legacy R position one preserves exact primary and secondary branches`() {
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 13, cr2 = 0))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 6, cr2 = 0))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 7, cr2 = 13))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 10, cr2 = 13))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 5, cr2 = 13))
        // The official SMALI repeats CR1==6; CR2==6 alone must not be promoted to R=1.
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 5, cr2 = 6))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 5, cr2 = 7))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = 5, cr2 = 10))
        for (cr1 in listOf(8, 9, 11, 4)) {
            assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 1, cr1 = cr1, cr2 = 0))
        }
    }

    @Test
    fun `legacy R position three follows recovered attack and fallback routing`() {
        for (cr1 in listOf(11, 9, 8, 4)) {
            assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = cr1, cr2 = 0))
        }
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 7, cr2 = 11))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 10, cr2 = 11))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 5, cr2 = 11))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 5, cr2 = 7))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 5, cr2 = 10))
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 3, cr1 = 5, cr2 = 5))
    }

    @Test
    fun `legacy R position four keeps zero two and one buckets`() {
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 4, cr1 = 7, cr2 = 0))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 4, cr1 = 10, cr2 = 0))
        for (cr1 in listOf(8, 13, 6)) {
            assertEquals(2, LegacyMatchPersistedPlayerRules.legacyR(position = 4, cr1 = cr1, cr2 = 0))
        }
        assertEquals(1, LegacyMatchPersistedPlayerRules.legacyR(position = 4, cr1 = 11, cr2 = 0))
    }

    @Test
    fun `legacy R unknown position retains constructor default zero`() {
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = -1, cr1 = 13, cr2 = 13))
        assertEquals(0, LegacyMatchPersistedPlayerRules.legacyR(position = 5, cr1 = 13, cr2 = 13))
    }
}

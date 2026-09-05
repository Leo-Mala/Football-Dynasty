package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyAnnualPlayerD0RulesTest {
    @Test
    fun `counter increments even when latch is already true`() {
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(legacyJ0 = 8, legacyW0 = true),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(
                    legacyJ0 = 7,
                    legacyW0 = true,
                    hasClub = true,
                    age = 20,
                    clubJ = 0,
                    clubJ0 = 1,
                ),
            ),
        )
    }

    @Test
    fun `global guards preserve false latch after increment`() {
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(1, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(0, false, true, 20, 0, 1),
            ),
        )
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(2, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(1, false, false, 20, 0, 1),
            ),
        )
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(2, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(1, false, true, 35, 0, 1),
            ),
        )
    }

    @Test
    fun `club J zero codes 1 65 and 97 latch from counter two`() {
        listOf(1, 65, 97).forEach { clubJ0 ->
            assertEquals(
                LegacyAnnualPlayerD0Rules.Result(2, true),
                LegacyAnnualPlayerD0Rules.apply(
                    LegacyAnnualPlayerD0Rules.Input(1, false, true, 34, 0, clubJ0),
                ),
            )
        }
    }

    @Test
    fun `club J zero codes 104 72 and 154 require counter three`() {
        listOf(104, 72, 154).forEach { clubJ0 ->
            assertEquals(
                LegacyAnnualPlayerD0Rules.Result(2, false),
                LegacyAnnualPlayerD0Rules.apply(
                    LegacyAnnualPlayerD0Rules.Input(1, false, true, 34, 0, clubJ0),
                ),
            )
            assertEquals(
                LegacyAnnualPlayerD0Rules.Result(3, true),
                LegacyAnnualPlayerD0Rules.apply(
                    LegacyAnnualPlayerD0Rules.Input(2, false, true, 34, 0, clubJ0),
                ),
            )
        }
    }

    @Test
    fun `nonzero club J only code 29 latches and only from counter four`() {
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(3, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(2, false, true, 34, 1, 29),
            ),
        )
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(4, true),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(3, false, true, 34, 1, 29),
            ),
        )
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(4, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(3, false, true, 34, 1, 65),
            ),
        )
    }

    @Test
    fun `club J zero does not use the nonzero code 29 branch`() {
        assertEquals(
            LegacyAnnualPlayerD0Rules.Result(10, false),
            LegacyAnnualPlayerD0Rules.apply(
                LegacyAnnualPlayerD0Rules.Input(9, false, true, 20, 0, 29),
            ),
        )
    }
}

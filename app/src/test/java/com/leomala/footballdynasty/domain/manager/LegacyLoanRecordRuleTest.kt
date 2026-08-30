package com.leomala.footballdynasty.domain.manager

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyLoanRecordRuleTest {
    @Test
    fun `copies game date onto creation calendar then adds exactly 319 calendar days`() {
        val zone = TimeZone.getTimeZone("America/New_York")
        val gameCalendar = GregorianCalendar(zone).apply {
            clear()
            set(2026, Calendar.MARCH, 7, 4, 5, 6)
            set(Calendar.MILLISECOND, 7)
        }
        val creationCalendar = GregorianCalendar(zone).apply {
            clear()
            set(2035, Calendar.JANUARY, 2, 17, 41, 29)
            set(Calendar.MILLISECOND, 321)
        }

        val expected = creationCalendar.clone() as Calendar
        expected.set(2026, Calendar.MARCH, 7)
        expected.add(Calendar.DAY_OF_MONTH, 319)

        val record = LegacyLoanRecordRule.create(
            playerCode = 77,
            sourceClubCode = 11,
            gameCalendar = gameCalendar,
            creationCalendar = creationCalendar,
        )

        assertEquals(77, record.playerCode)
        assertEquals(11, record.sourceClubCode)
        assertEquals(expected.timeInMillis, record.expiryMillis)

        val actual = GregorianCalendar(zone).apply { timeInMillis = record.expiryMillis }
        assertEquals(17, actual.get(Calendar.HOUR_OF_DAY))
        assertEquals(41, actual.get(Calendar.MINUTE))
        assertEquals(29, actual.get(Calendar.SECOND))
        assertEquals(321, actual.get(Calendar.MILLISECOND))
    }

    @Test
    fun `append preserves existing order and allows duplicate raw player records like ArrayList add`() {
        val zone = TimeZone.getTimeZone("UTC")
        val gameCalendar = GregorianCalendar(zone).apply {
            clear()
            set(2026, Calendar.AUGUST, 30)
        }
        val creationCalendar = GregorianCalendar(zone).apply {
            clear()
            set(2026, Calendar.AUGUST, 30, 12, 34, 56)
        }
        val existing = listOf(
            LegacyLoanRecord(playerCode = 1, sourceClubCode = 2, expiryMillis = 3L),
            LegacyLoanRecord(playerCode = 77, sourceClubCode = 4, expiryMillis = 5L),
        )

        val after = LegacyLoanRecordRule.append(
            existing = existing,
            playerCode = 77,
            sourceClubCode = 11,
            gameCalendar = gameCalendar,
            creationCalendar = creationCalendar,
        )

        assertEquals(existing[0], after[0])
        assertEquals(existing[1], after[1])
        assertEquals(77, after[2].playerCode)
        assertEquals(11, after[2].sourceClubCode)
        assertEquals(3, after.size)
        assertEquals(2, after.count { it.playerCode == 77 })
    }
}

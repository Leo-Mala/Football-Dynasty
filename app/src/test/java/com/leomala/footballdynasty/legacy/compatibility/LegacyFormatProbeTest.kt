package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.foundation.error.UnsupportedLegacySaveException
import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class LegacyFormatProbeTest {
    @Test
    fun `recognizes java serialized ban fixture`() {
        val bytes = LegacyFixtureLoader.bytes("/legacy/12deoctubre_par.ban.b64", javaClass)
        val result = LegacyFormatProbe.probe("12deoctubre_par.ban", ByteArrayInputStream(bytes))

        assertEquals(LegacyFormatKind.BAN_JAVA_SERIALIZATION, result.kind)
        assertEquals("ban", result.extension)
        assertTrue(result.javaSerializationMagic)
    }

    @Test
    fun `does not claim malformed ban is compatible`() {
        val result = LegacyFormatProbe.probe("broken.ban", ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        assertEquals(LegacyFormatKind.UNKNOWN, result.kind)
        assertFalse(result.javaSerializationMagic)
    }

    @Test
    fun `career extension is identified without claiming successful decode`() {
        val result = LegacyFormatProbe.probe("career.s21", ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        assertEquals(LegacyFormatKind.CAREER_KRYO_OR_LEGACY, result.kind)
        assertFalse(result.javaSerializationMagic)
    }

    @Test(expected = UnsupportedLegacySaveException::class)
    fun `career reader remains blocked until real fixture exists`() {
        LegacySaveReader().readCareer(ByteArrayInputStream(byteArrayOf()))
    }
}

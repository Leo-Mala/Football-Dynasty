package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.foundation.error.UnsupportedLegacySaveException
import com.leomala.footballdynasty.testsupport.LegacyFixtureLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream

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
    fun `recognizes active Brasfoot 26 metadata extension only with java serialization magic`() {
        val bytes = ByteArrayOutputStream().also { output ->
            ObjectOutputStream(output).use { it.writeObject("metadata-probe") }
        }.toByteArray()

        val valid = LegacyFormatProbe.probe("bf123.a26", ByteArrayInputStream(bytes))
        val malformed = LegacyFormatProbe.probe("bf123.a26", ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        assertEquals(LegacyFormatKind.SAVE_METADATA_JAVA_SERIALIZATION, valid.kind)
        assertTrue(valid.javaSerializationMagic)
        assertEquals(LegacyFormatKind.UNKNOWN, malformed.kind)
    }

    @Test
    fun `Brasfoot 26 career extension is identified without claiming successful decode`() {
        val result = LegacyFormatProbe.probe("bf123.s26", ByteArrayInputStream(byteArrayOf(1, 2, 3)))

        assertEquals(LegacyFormatKind.CAREER_KRYO_OR_LEGACY, result.kind)
        assertFalse(result.javaSerializationMagic)
    }

    @Test
    fun `superseded 21 extensions remain recognized only for historical regression`() {
        val metadataBytes = ByteArrayOutputStream().also { output ->
            ObjectOutputStream(output).use { it.writeObject("metadata-probe") }
        }.toByteArray()

        assertEquals(
            LegacyFormatKind.SAVE_METADATA_JAVA_SERIALIZATION,
            LegacyFormatProbe.probe("career.ai21", ByteArrayInputStream(metadataBytes)).kind,
        )
        assertEquals(
            LegacyFormatKind.CAREER_KRYO_OR_LEGACY,
            LegacyFormatProbe.probe("career.s21", ByteArrayInputStream(byteArrayOf())).kind,
        )
        assertEquals(
            LegacyFormatKind.CAREER_KRYO_OR_LEGACY,
            LegacyFormatProbe.probe("career.s121", ByteArrayInputStream(byteArrayOf())).kind,
        )
    }

    @Test(expected = UnsupportedLegacySaveException::class)
    fun `career reader remains blocked until real Brasfoot 26 fixture exists`() {
        LegacySaveReader().readCareer(ByteArrayInputStream(byteArrayOf()))
    }
}

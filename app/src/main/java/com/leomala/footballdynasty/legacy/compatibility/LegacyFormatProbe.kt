package com.leomala.footballdynasty.legacy.compatibility

import java.io.BufferedInputStream
import java.io.InputStream

enum class LegacyFormatKind {
    BAN_JAVA_SERIALIZATION,
    OPTIONS_JAVA_SERIALIZATION,
    SAVE_METADATA_JAVA_SERIALIZATION,
    CAREER_KRYO_OR_LEGACY,
    SAVE_BACKUP,
    UNKNOWN,
}

data class LegacyFormatProbeResult(
    val kind: LegacyFormatKind,
    val extension: String,
    val javaSerializationMagic: Boolean,
)

/**
 * Structural, read-only identification of known legacy persistence artifacts.
 *
 * This probe deliberately does not claim that a career save is decodable. For
 * .s21/.s121 the extension identifies the known career container while the
 * full Kryo graph remains gated by a real save fixture and characterization.
 */
object LegacyFormatProbe {
    private const val JAVA_MAGIC_0 = 0xAC
    private const val JAVA_MAGIC_1 = 0xED

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult {
        val buffered = if (input is BufferedInputStream) input else BufferedInputStream(input)
        buffered.mark(4)
        val first = buffered.read()
        val second = buffered.read()
        buffered.reset()

        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        val javaMagic = first == JAVA_MAGIC_0 && second == JAVA_MAGIC_1
        val kind = when (extension) {
            "ban" -> if (javaMagic) LegacyFormatKind.BAN_JAVA_SERIALIZATION else LegacyFormatKind.UNKNOWN
            "bcf" -> if (javaMagic) LegacyFormatKind.OPTIONS_JAVA_SERIALIZATION else LegacyFormatKind.UNKNOWN
            "ai21" -> if (javaMagic) LegacyFormatKind.SAVE_METADATA_JAVA_SERIALIZATION else LegacyFormatKind.UNKNOWN
            "s21", "s121" -> LegacyFormatKind.CAREER_KRYO_OR_LEGACY
            "sbck" -> LegacyFormatKind.SAVE_BACKUP
            else -> LegacyFormatKind.UNKNOWN
        }

        return LegacyFormatProbeResult(
            kind = kind,
            extension = extension,
            javaSerializationMagic = javaMagic,
        )
    }
}

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
 * The official Phase 4R baseline is Brasfoot 2026/27, which uses .a26 metadata
 * and .s26 Kryo career containers. Historical .ai21/.s21/.s121 recognition is
 * retained only as a superseded-regression aid; it is not the active baseline.
 *
 * This probe deliberately does not claim that a career graph is decodable.
 */
object LegacyFormatProbe {
    private const val JAVA_MAGIC_0 = 0xAC
    private const val JAVA_MAGIC_1 = 0xED

    private val metadataExtensions = setOf("a26", "ai21")
    private val careerExtensions = setOf("s26", "s21", "s121")

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult {
        val buffered = if (input is BufferedInputStream) input else BufferedInputStream(input)
        buffered.mark(4)
        val first = buffered.read()
        val second = buffered.read()
        buffered.reset()

        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        val javaMagic = first == JAVA_MAGIC_0 && second == JAVA_MAGIC_1
        val kind = when {
            extension == "ban" && javaMagic -> LegacyFormatKind.BAN_JAVA_SERIALIZATION
            extension == "bcf" && javaMagic -> LegacyFormatKind.OPTIONS_JAVA_SERIALIZATION
            extension in metadataExtensions && javaMagic -> LegacyFormatKind.SAVE_METADATA_JAVA_SERIALIZATION
            extension in careerExtensions -> LegacyFormatKind.CAREER_KRYO_OR_LEGACY
            extension == "sbck" -> LegacyFormatKind.SAVE_BACKUP
            else -> LegacyFormatKind.UNKNOWN
        }

        return LegacyFormatProbeResult(
            kind = kind,
            extension = extension,
            javaSerializationMagic = javaMagic,
        )
    }
}

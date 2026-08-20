package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.model.LegacySaveMetadataSnapshot
import com.leomala.footballdynasty.foundation.error.UnsupportedLegacySaveException
import java.io.InputStream

/**
 * Read-only boundary for legacy save artifacts.
 *
 * Metadata is supported. The main career graph remains blocked until a real
 * .s21/.s121 fixture can prove the Kryo registration and post-load reference
 * reconstruction path documented during reverse engineering.
 */
class LegacySaveReader {
    fun readMetadata(input: InputStream): LegacySaveMetadataSnapshot =
        LegacySerialization.readSaveMetadata(input)

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult =
        LegacyFormatProbe.probe(fileName, input)

    fun readCareer(@Suppress("UNUSED_PARAMETER") input: InputStream): Nothing =
        throw UnsupportedLegacySaveException(
            "Legacy career graph reading is intentionally disabled until a real .ai21 + .s21/.s121 fixture is characterized"
        )
}

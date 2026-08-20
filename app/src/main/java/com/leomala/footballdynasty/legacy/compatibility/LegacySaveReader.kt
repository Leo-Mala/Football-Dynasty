package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.model.LegacySaveMetadataSnapshot
import java.io.InputStream

/**
 * Read-only boundary for legacy save artifacts.
 *
 * Phase 2 intentionally supports metadata only. The main career graph remains
 * blocked until a real .s21/.s121 fixture can prove the Kryo registration and
 * post-load reference reconstruction path documented during reverse engineering.
 */
class LegacySaveReader {
    fun readMetadata(input: InputStream): LegacySaveMetadataSnapshot =
        LegacySerialization.readSaveMetadata(input)

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult =
        LegacyFormatProbe.probe(fileName, input)

    fun readCareer(@Suppress("UNUSED_PARAMETER") input: InputStream): Nothing =
        throw UnsupportedOperationException(
            "Legacy career graph reading is intentionally disabled until a real .s21/.s121 fixture is characterized"
        )
}

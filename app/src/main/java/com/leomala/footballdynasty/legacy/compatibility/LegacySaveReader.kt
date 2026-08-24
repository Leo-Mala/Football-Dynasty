package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.model.LegacySaveMetadataSnapshot
import com.leomala.footballdynasty.foundation.error.UnsupportedLegacySaveException
import java.io.InputStream

/**
 * Read-only boundary for legacy save artifacts.
 *
 * Metadata uses Java Object Serialization and is structurally supported. The
 * official Brasfoot 2026/27 baseline stores metadata in .a26 and the main
 * career graph in .s26 using Kryo. The career reader remains blocked until a
 * real .a26 + .s26 fixture proves the registration-free graph and post-load
 * reconstruction path recovered from ActivityLoad Java/SMALI.
 */
class LegacySaveReader {
    fun readMetadata(input: InputStream): LegacySaveMetadataSnapshot =
        LegacySerialization.readSaveMetadata(input)

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult =
        LegacyFormatProbe.probe(fileName, input)

    fun readCareer(@Suppress("UNUSED_PARAMETER") input: InputStream): Nothing =
        throw UnsupportedLegacySaveException(
            "Brasfoot 2026/27 career graph reading is intentionally disabled until a real .a26 + .s26 fixture is characterized"
        )
}

package com.leomala.footballdynasty.data

import com.leomala.footballdynasty.domain.model.LegacySaveMetadataSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import com.leomala.footballdynasty.legacy.compatibility.LegacyFormatProbeResult
import com.leomala.footballdynasty.legacy.compatibility.LegacySaveReader
import com.leomala.footballdynasty.legacy.compatibility.LegacySerialization
import java.io.InputStream

/** Read-only application boundary over legacy persistence formats. */
class LegacyDataGateway(
    private val saveReader: LegacySaveReader = LegacySaveReader(),
) {
    fun readTeamBan(input: InputStream): LegacyTeamSnapshot = LegacySerialization.readBan(input)

    fun readSaveMetadata(input: InputStream): LegacySaveMetadataSnapshot =
        saveReader.readMetadata(input)

    fun probe(fileName: String, input: InputStream): LegacyFormatProbeResult =
        saveReader.probe(fileName, input)
}

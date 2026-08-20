package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacySaveMetadataSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import e.g
import e.t
import est.InfoArquivoSalvoType
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.ObjectInputStream

object LegacySerialization {
    private const val STREAM_MAGIC_0 = 0xAC
    private const val STREAM_MAGIC_1 = 0xED

    fun readBan(input: InputStream): LegacyTeamSnapshot =
        readJavaSerialized(input, t::class.java).toSnapshot()

    fun readSaveMetadata(input: InputStream): LegacySaveMetadataSnapshot =
        readJavaSerialized(input, InfoArquivoSalvoType::class.java).let {
            LegacySaveMetadataSnapshot(
                a = it.legacyA(),
                n = it.legacyN(),
                tc = it.legacyTc(),
                i = it.legacyI(),
                path = it.legacyPath(),
            )
        }

    private fun <T : Any> readJavaSerialized(input: InputStream, expected: Class<T>): T {
        val buffered = if (input is BufferedInputStream) input else BufferedInputStream(input)
        buffered.mark(4)
        val first = buffered.read()
        val second = buffered.read()
        buffered.reset()
        require(first == STREAM_MAGIC_0 && second == STREAM_MAGIC_1) {
            "Not a Java Object Serialization stream"
        }
        val value = ObjectInputStream(buffered).use { it.readObject() }
        require(expected.isInstance(value)) {
            "Expected ${expected.name}, found ${value.javaClass.name}"
        }
        return expected.cast(value)
    }

    private fun t.toSnapshot() = LegacyTeamSnapshot(
        name = legacyName().orEmpty(),
        fileRef = legacyFileRef().orEmpty(),
        country = legacyCountry(),
        state = legacyState(),
        level = legacyLevel(),
        stadium = legacyStadium().orEmpty(),
        capacity = legacyCapacity(),
        reputation = legacyReputation(),
        players = legacyPlayers().map { it.toSnapshot() },
        juniors = legacyJuniors().map { it.toSnapshot() },
        primaryColor = legacyPrimaryColor().orEmpty(),
        secondaryColor = legacySecondaryColor().orEmpty(),
        coach = legacyCoach().orEmpty(),
        coachCountry = legacyCoachCountry(),
        baseColor = legacyBaseColor(),
        legacyAid = legacyAid(),
        legacySid = legacySid(),
        legacyTid = legacyTid(),
        legacyVid = legacyVid(),
        legacyId = legacyId(),
        legacyValid = legacyValid(),
    )

    private fun g.toSnapshot() = LegacyPlayerSnapshot(
        name = legacyName().orEmpty(),
        age = legacyAge(),
        country = legacyCountry(),
        position = legacyPosition(),
        status = legacyStatus(),
        side = legacySide(),
        cr1 = legacyCr1(),
        cr2 = legacyCr2(),
        star = legacyStar(),
        worldTop = legacyWorldTop(),
        legacyAid = legacyAid(),
        legacySid = legacySid(),
        legacyTid = legacyTid(),
        legacyHash = legacyHash(),
    )
}

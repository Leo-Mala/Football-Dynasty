package com.leomala.footballdynasty.migration.v1

import java.security.MessageDigest

object V1Fingerprint {
    fun club(data: ClubDataV1): String = sha256(canonicalClub(data).toByteArray(Charsets.UTF_8))

    fun corpus(clubs: List<ClubDataV1>): String {
        val canonical = clubs
            .sortedWith(compareBy<ClubDataV1>({ it.sourceFileRef }, { it.id }))
            .joinToString("\n") { canonicalClub(it) }
        return sha256(canonical.toByteArray(Charsets.UTF_8))
    }

    fun sourceManifest(entries: List<SourceManifestEntryV1>): String {
        val canonical = entries
            .sortedBy { it.logicalPath }
            .joinToString("\n") { "${escape(it.logicalPath)}|${it.sha256}" }
        return sha256(canonical.toByteArray(Charsets.UTF_8))
    }

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private fun canonicalClub(data: ClubDataV1): String = buildString {
        append("C|").append(data.schemaVersion)
        append('|').append(escape(data.id))
        append('|').append(escape(data.sourceFileRef))
        append('|').append(escape(data.name))
        append('|').append(data.country)
        append('|').append(data.state)
        append('|').append(data.level)
        append('|').append(escape(data.stadium))
        append('|').append(data.capacity)
        append('|').append(data.reputation)
        append('|').append(escape(data.primaryColor))
        append('|').append(escape(data.secondaryColor))
        append('|').append(escape(data.coach))
        append('|').append(data.coachCountry)
        append('|').append(data.baseColor)
        append('|').append(data.legacyAid)
        append('|').append(data.legacySid)
        append('|').append(data.legacyTid)
        append('|').append(data.legacyVid)
        append('|').append(data.legacyId)
        append('|').append(data.legacyValid)

        data.players
            .sortedWith(compareBy<PlayerDataV1>({ it.rosterKind.ordinal }, { it.sourceOrdinal }, { it.id }))
            .forEach { player ->
                append("\nP|").append(player.schemaVersion)
                append('|').append(escape(player.id))
                append('|').append(escape(player.sourceClubId))
                append('|').append(player.rosterKind.name)
                append('|').append(player.sourceOrdinal)
                append('|').append(escape(player.name))
                append('|').append(player.age)
                append('|').append(player.country)
                append('|').append(player.position)
                append('|').append(player.status)
                append('|').append(player.side)
                append('|').append(player.cr1)
                append('|').append(player.cr2)
                append('|').append(player.star)
                append('|').append(player.worldTop)
                append('|').append(player.legacyAid)
                append('|').append(player.legacySid)
                append('|').append(player.legacyTid)
                append('|').append(player.legacyHash)
            }
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '|' -> append("\\|")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(char)
            }
        }
    }
}

data class SourceManifestEntryV1(
    val logicalPath: String,
    val sha256: String,
)

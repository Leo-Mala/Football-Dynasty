package com.leomala.footballdynasty.foundation.identity

import com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot
import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import com.leomala.footballdynasty.migration.v1.RosterKindV1
import java.security.MessageDigest

/**
 * Deterministic IDs for immutable legacy source data.
 *
 * Names are deliberately not used as the sole identity. Every component is
 * length-prefixed before hashing, avoiding delimiter ambiguity. The roster
 * ordinal is the stable source-list position from the immutable .ban stream;
 * it is not a database row/order identifier.
 */
object StableLegacyIdentity {
    fun club(snapshot: LegacyTeamSnapshot): String =
        "fd-club-v1-${digest(
            "club",
            snapshot.country,
            snapshot.state,
            snapshot.fileRef,
            snapshot.legacyId,
            snapshot.legacyAid,
            snapshot.legacySid,
            snapshot.legacyTid,
            snapshot.legacyVid,
        )}"

    fun player(
        clubId: String,
        rosterKind: RosterKindV1,
        sourceOrdinal: Int,
        snapshot: LegacyPlayerSnapshot,
    ): String =
        "fd-player-v1-${digest(
            "player",
            clubId,
            rosterKind.name,
            sourceOrdinal,
            snapshot.legacyAid,
            snapshot.legacySid,
            snapshot.legacyTid,
            snapshot.legacyHash,
        )}"

    fun competition(legacyType: String, legacyStableKey: String): String =
        "fd-competition-v1-${digest("competition", legacyType, legacyStableKey)}"

    fun career(sourceStableKey: String): String =
        "fd-career-v1-${digest("career", sourceStableKey)}"

    private fun digest(vararg parts: Any?): String {
        val canonical = buildString {
            parts.forEach { value ->
                val text = value?.toString() ?: "<null>"
                append(text.length).append(':').append(text)
            }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

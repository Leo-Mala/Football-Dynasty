package com.leomala.footballdynasty.legacy.compatibility

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import java.security.MessageDigest

object DeterministicFingerprint {
    fun team(snapshot: LegacyTeamSnapshot): String {
        val canonical = buildString {
            append(snapshot.name).append('|').append(snapshot.fileRef)
            append('|').append(snapshot.country).append('|').append(snapshot.state)
            append('|').append(snapshot.level).append('|').append(snapshot.stadium)
            append('|').append(snapshot.capacity).append('|').append(snapshot.reputation)
            snapshot.players.forEach { p ->
                append("|P:").append(p.name).append(',').append(p.age)
                    .append(',').append(p.country).append(',').append(p.position)
                    .append(',').append(p.status).append(',').append(p.side)
                    .append(',').append(p.cr1).append(',').append(p.cr2)
                    .append(',').append(p.star).append(',').append(p.worldTop)
            }
            snapshot.juniors.forEach { p ->
                append("|J:").append(p.name).append(',').append(p.age)
                    .append(',').append(p.country).append(',').append(p.position)
                    .append(',').append(p.status).append(',').append(p.side)
                    .append(',').append(p.cr1).append(',').append(p.cr2)
                    .append(',').append(p.star).append(',').append(p.worldTop)
            }
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}

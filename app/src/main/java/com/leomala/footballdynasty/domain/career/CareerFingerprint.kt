package com.leomala.footballdynasty.domain.career

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object CareerFingerprint {
    fun of(state: CareerState): String {
        val canonical = buildString {
            append("career-state-v1\n")
            append("id=").append(state.id).append('\n')
            append("stateVersion=").append(state.stateVersion).append('\n')
            append("season.number=").append(state.season.number).append('\n')
            append("season.year=").append(state.season.year).append('\n')
            append("calendar.year=").append(state.calendar.year).append('\n')
            append("calendar.currentDayIndex=").append(state.calendar.currentDayIndex).append('\n')
            append("calendar.startDayIndex=").append(state.calendar.startDayIndex).append('\n')
            append("calendar.dayCount=").append(state.calendar.dayCount).append('\n')
            append("managedClubId=").append(state.managedClub?.clubId.orEmpty()).append('\n')
            append("rng.initialSeed=").append(state.random.initialSeed).append('\n')
            append("rng.internalState=").append(state.random.internalState).append('\n')
            append("rng.draws=").append(state.random.draws).append('\n')
            append("transitionCount=").append(state.transitionCount).append('\n')
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
    }
}

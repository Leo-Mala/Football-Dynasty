package com.leomala.footballdynasty.domain.career

/** Exact arithmetic of legacy `konrent.t.D0()`. */
object LegacyCompetitionRoundThresholdRules {
    fun resolve(
        participantCount: Int,
        legacyGroupCountA0: Int,
        legacyRoundU: Int,
    ): Int {
        val rawScheduleLength = if (legacyGroupCountA0 == 0) {
            (participantCount - 1) * 2
        } else {
            // Preserve the legacy integer division before conversion to float/Math.round.
            val participantsPerGroup = participantCount / legacyGroupCountA0
            Math.round(participantsPerGroup.toFloat()) * 2 - 1
        }

        // The bytecode divides as integers before both float conversions and Math.round calls.
        val halfway = Math.round((rawScheduleLength / 2).toFloat())
        if (legacyRoundU <= halfway) return 0
        return Math.round((legacyRoundU / 2).toFloat())
    }
}

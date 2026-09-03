package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player

/**
 * Status-code view of the legacy senior squad.
 *
 * Legacy evidence preserves `status` as an opaque integer on both
 * [com.leomala.footballdynasty.domain.model.LegacyPlayerSnapshot] and [Player].
 * The code is intentionally not interpreted here as injury, suspension or any
 * other eligibility state until Java/SMALI evidence proves those semantics.
 * Players keep the same source order produced by [LegacySeniorSquad].
 */
object LegacySeniorSquadStatuses {
    fun playersByStatus(club: Club): Map<Int, List<Player>> =
        LegacySeniorSquad.players(club).groupBy { player -> player.status }
}

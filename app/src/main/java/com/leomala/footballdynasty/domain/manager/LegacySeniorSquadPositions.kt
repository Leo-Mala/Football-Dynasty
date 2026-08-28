package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player

/**
 * Position-code view of the legacy senior squad.
 *
 * Legacy evidence: `a.p` persists a numeric `posicao` field. The numeric code is
 * intentionally kept opaque here: no position labels, formation slots or
 * eligibility rules are inferred without stronger Java/SMALI evidence.
 * Players keep the same source order produced by [LegacySeniorSquad].
 */
object LegacySeniorSquadPositions {
    fun playersByPosition(club: Club): Map<Int, List<Player>> =
        LegacySeniorSquad.players(club).groupBy { player -> player.position }
}

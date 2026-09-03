package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player

/**
 * Side-code view of the legacy senior squad.
 *
 * The legacy player snapshot preserves `side` as an opaque integer and the
 * modern [Player] carries the same raw value. The code is deliberately not
 * interpreted as left/right/centre or any tactical eligibility until the
 * Java/SMALI corpus proves those semantics. Players retain the source order
 * produced by [LegacySeniorSquad].
 */
object LegacySeniorSquadSides {
    fun playersBySide(club: Club): Map<Int, List<Player>> =
        LegacySeniorSquad.players(club).groupBy { player -> player.side }
}

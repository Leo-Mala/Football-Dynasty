package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.Club
import com.leomala.footballdynasty.domain.model.Player
import com.leomala.footballdynasty.domain.model.RosterKind

/**
 * Persistence-independent projection of the club's legacy senior squad.
 *
 * Legacy evidence: `a.ac` owns the senior-player `ArrayList<p> xp`.  The modern
 * import model can also carry junior players, so manager runtime must not mix
 * those rosters.  Source order is preserved deliberately: no strength-, age-
 * or position-based selection rule is inferred here.
 */
object LegacySeniorSquad {
    fun players(club: Club): List<Player> = club.players.filter { player ->
        player.clubId == club.id && player.rosterKind == RosterKind.SENIOR
    }
}

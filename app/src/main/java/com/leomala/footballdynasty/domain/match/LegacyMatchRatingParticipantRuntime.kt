package com.leomala.footballdynasty.domain.match

/**
 * Match-local identity snapshot reproducing the four lists traversed by legacy `best.s.e()`.
 *
 * `u0/v0` retain the original starters even after substitutions mutate the live active lists.
 * `F/G` contain only players who entered through substitutions; the modern `Club.used` list already
 * has exactly that ownership and insertion order. No part of this snapshot is durable career state.
 */
object LegacyMatchRatingParticipantRuntime {
    data class Snapshot<TPlayer>(
        val homeOriginalStarters: List<LegacyMatchTransientRuntime.Player<TPlayer>>,
        val awayOriginalStarters: List<LegacyMatchTransientRuntime.Player<TPlayer>>,
    )

    data class Entry<TPlayer>(
        val side: Int,
        val player: LegacyMatchTransientRuntime.Player<TPlayer>,
    )

    fun <TClub, TPlayer> capture(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
    ): Snapshot<TPlayer> = Snapshot(
        homeOriginalStarters = state.home.active.toList(),
        awayOriginalStarters = state.away.active.toList(),
    )

    /** Exact `best.s.e()` traversal order: `u0`, `v0`, `F`, then `G`. */
    fun <TClub, TPlayer> entriesInLegacyOrder(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        snapshot: Snapshot<TPlayer>,
    ): List<Entry<TPlayer>> = buildList {
        snapshot.homeOriginalStarters.forEach { add(Entry(side = 0, player = it)) }
        snapshot.awayOriginalStarters.forEach { add(Entry(side = 1, player = it)) }
        state.home.used.forEach { add(Entry(side = 0, player = it)) }
        state.away.used.forEach { add(Entry(side = 1, player = it)) }
    }

    fun <TClub, TPlayer> playersInLegacyOrder(
        state: LegacyMatchTransientRuntime.State<TClub, TPlayer>,
        snapshot: Snapshot<TPlayer>,
        side: Int,
    ): List<LegacyMatchTransientRuntime.Player<TPlayer>> = when (side) {
        0 -> snapshot.homeOriginalStarters + state.home.used
        1 -> snapshot.awayOriginalStarters + state.away.used
        else -> throw IllegalArgumentException("Legacy match side must be 0 or 1: $side")
    }
}

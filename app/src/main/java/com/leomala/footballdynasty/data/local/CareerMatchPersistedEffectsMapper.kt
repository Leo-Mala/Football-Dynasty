package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.GameDate
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import java.time.LocalDate

/** Maps only already-proven persisted player effects from the certified match runtime back to Room. */
object CareerMatchPersistedEffectsMapper {
    fun playerRuntimeUpdates(
        state: LegacyMatchTransientRuntime.State<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >,
        matchDate: GameDate,
    ): List<CareerMatchPlayerRuntimeUpdate> {
        val matchEpochDay = LocalDate.of(matchDate.year, matchDate.month, matchDate.day).toEpochDay()
        val observed = mutableListOf<LegacyMatchTransientRuntime.Player<CareerMatchPersistedRuntimeResolver.PersistedPlayer>>()

        fun addClub(
            club: LegacyMatchTransientRuntime.Club<
                CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
                CareerMatchPersistedRuntimeResolver.PersistedPlayer,
            >,
        ) {
            observed += club.active
            observed += club.bench
            observed += club.used
        }

        addClub(state.home)
        addClub(state.away)
        state.events.forEach { event ->
            event.primaryPlayer?.let(observed::add)
            event.secondaryPlayer?.let(observed::add)
        }

        return observed
            .groupBy { it.value.playerId }
            .map { (playerId, wrappers) ->
                val updates = wrappers.map { player ->
                    val injury = player.lastInjuryResult
                    val injuryUntil = if (injury?.shouldSetInjuryUntil == true) {
                        matchEpochDay + injury.durationDays
                    } else {
                        player.value.injuryUntilEpochDay
                    }
                    CareerMatchPlayerRuntimeUpdate(
                        playerId = playerId,
                        energy = player.energy,
                        overall = player.skill,
                        injuryUntilEpochDay = injuryUntil,
                    )
                }.distinct()
                require(updates.size == 1) {
                    "Transient match contains divergent persisted state for player $playerId: $updates"
                }
                updates.single()
            }
            .sortedBy { it.playerId }
    }
}

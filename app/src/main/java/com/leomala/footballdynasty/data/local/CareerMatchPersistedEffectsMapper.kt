package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime

/** Maps only already-proven persisted player effects from the certified match runtime back to Room. */
object CareerMatchPersistedEffectsMapper {
    fun energyUpdates(
        state: LegacyMatchTransientRuntime.State<
            CareerMatchPersistedRuntimeResolver.PersistedClubRoster,
            CareerMatchPersistedRuntimeResolver.PersistedPlayer,
        >,
    ): List<CareerMatchPlayerEnergyUpdate> {
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
                val energies = wrappers.map { it.energy }.distinct()
                require(energies.size == 1) {
                    "Transient match contains divergent energy for player $playerId: $energies"
                }
                CareerMatchPlayerEnergyUpdate(playerId, energies.single())
            }
            .sortedBy { it.playerId }
    }
}

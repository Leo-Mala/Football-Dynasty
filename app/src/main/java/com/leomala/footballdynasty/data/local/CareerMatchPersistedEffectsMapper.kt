package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.GameDate
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import java.time.LocalDate

data class CareerMatchPlayerClubSeasonStatUpdate(
    val playerId: String,
    val legacySeasonId: Int,
    val legacyClubId: Int,
    val legacyC: Int,
    val legacyD: Int,
    val legacyE: Int,
    val legacyF: Int,
    val legacyG: Int,
    val legacyH: Int,
)

/** Maps only already-proven persisted player effects from the certified match runtime back to Room. */
object CareerMatchPersistedEffectsMapper {
    fun playerRuntimeUpdates(
        state: PersistedState,
        matchDate: GameDate,
    ): List<CareerMatchPlayerRuntimeUpdate> {
        val matchEpochDay = LocalDate.of(matchDate.year, matchDate.month, matchDate.day).toEpochDay()
        return observedPlayers(state)
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

    /**
     * Snapshots only the serializable `best.e` entries already carried and mutated by Phase 8.
     * The transient legacy `best.e.i` field is deliberately absent from the persisted update.
     */
    fun playerClubSeasonStatUpdates(
        state: PersistedState,
    ): List<CareerMatchPlayerClubSeasonStatUpdate> = observedPlayers(state)
        .groupBy { it.value.playerId }
        .flatMap { (playerId, wrappers) ->
            val snapshots = wrappers.map { player ->
                player.clubSeasonStats.orEmpty().map { entry ->
                    CareerMatchPlayerClubSeasonStatUpdate(
                        playerId = playerId,
                        legacySeasonId = entry.legacySeasonId,
                        legacyClubId = entry.legacyClubId,
                        legacyC = entry.legacyC,
                        legacyD = entry.legacyD,
                        legacyE = entry.legacyE,
                        legacyF = entry.legacyF,
                        legacyG = entry.legacyG,
                        legacyH = entry.legacyH,
                    )
                }.sortedWith(compareBy({ it.legacySeasonId }, { it.legacyClubId }))
            }.distinct()
            require(snapshots.size == 1) {
                "Transient match contains divergent club-season stats for player $playerId"
            }
            snapshots.single()
        }
        .sortedWith(compareBy({ it.playerId }, { it.legacySeasonId }, { it.legacyClubId }))

    private fun observedPlayers(
        state: PersistedState,
    ): List<PersistedPlayerWrapper> {
        val observed = mutableListOf<PersistedPlayerWrapper>()

        fun addClub(club: PersistedClub) {
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
    }

    private typealias PersistedPlayer = CareerMatchPersistedRuntimeResolver.PersistedPlayer
    private typealias PersistedRoster = CareerMatchPersistedRuntimeResolver.PersistedClubRoster
    private typealias PersistedPlayerWrapper = LegacyMatchTransientRuntime.Player<PersistedPlayer>
    private typealias PersistedClub = LegacyMatchTransientRuntime.Club<PersistedRoster, PersistedPlayer>
    private typealias PersistedState = LegacyMatchTransientRuntime.State<PersistedRoster, PersistedPlayer>
}

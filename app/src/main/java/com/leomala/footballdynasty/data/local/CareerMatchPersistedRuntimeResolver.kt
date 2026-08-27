package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.domain.career.ScheduledCareerMatch
import com.leomala.footballdynasty.domain.match.LegacyMatchTransientRuntime
import com.leomala.footballdynasty.domain.match.LegacyPlayerClubSeasonStatsRules

/**
 * Evidence-preserving Phase 9 boundary between career persistence and the certified Phase 8 match
 * runtime.
 *
 * Room is authoritative here only for facts that are actually persisted: career/club/player
 * identity, career-local squad ownership, age and the legacy `best.o.j` value exposed by `O()` as
 * career overall, plus the canonical/procedural static player facts. Match-only legacy fields
 * (`g0/l0/f0/R`, energy and current disciplinary/stat counters) are deliberately supplied by
 * [TransientPlayerEvidence] instead of being inferred from unrelated persisted columns.
 */
class CareerMatchPersistedRuntimeResolver(
    private val database: FootballDynastyDatabase,
) {
    data class StaticPlayerFacts(
        val name: String,
        val country: Int,
        val position: Int,
        val status: Int,
        val side: Int,
        val cr1: Int,
        val cr2: Int,
    )

    data class PersistedPlayer(
        val playerId: String,
        val sourceType: String,
        val age: Int,
        val overall: Int,
        val legacyHash: Int,
        val rosterKind: String,
        val sourceOrdinal: Int,
        val facts: StaticPlayerFacts,
    )

    data class PersistedClubRoster(
        val clubId: String,
        val legacyClubId: Int,
        val players: List<PersistedPlayer>,
    )

    data class PersistedMatchRoster(
        val careerId: String,
        val scheduled: ScheduledCareerMatch,
        val home: PersistedClubRoster,
        val away: PersistedClubRoster,
    )

    suspend fun resolve(
        careerId: String,
        scheduled: ScheduledCareerMatch,
    ): PersistedMatchRoster {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        require(scheduled.matchId.isNotBlank()) { "Match id must not be blank" }
        require(scheduled.homeClubId.isNotBlank() && scheduled.awayClubId.isNotBlank()) {
            "Scheduled match clubs must not be blank"
        }
        require(scheduled.homeClubId != scheduled.awayClubId) {
            "Scheduled match clubs must be distinct"
        }

        return database.withTransaction {
            requireNotNull(database.careerMetadataDao().findById(careerId)) {
                "Missing career metadata for $careerId"
            }
            val homeClub = requireNotNull(database.clubDao().findById(scheduled.homeClubId)) {
                "Missing scheduled home club ${scheduled.homeClubId}"
            }
            val awayClub = requireNotNull(database.clubDao().findById(scheduled.awayClubId)) {
                "Missing scheduled away club ${scheduled.awayClubId}"
            }

            val dao = database.careerPlayerRuntimeDao()
            val runtimes = dao.runtimeForCareer(careerId).associateBy { it.playerId }
            val procedural = dao.proceduralPlayersForCareer(careerId).associateBy { it.playerId }
            val memberships = dao.membershipsForCareer(careerId)

            suspend fun resolveClub(clubId: String, legacyClubId: Int): PersistedClubRoster {
                val orderedMemberships = memberships
                    .filter { it.clubId == clubId }
                    .sortedWith(compareBy({ it.rosterKind }, { it.sourceOrdinal }, { it.playerId }))
                val players = mutableListOf<PersistedPlayer>()
                for (membership in orderedMemberships) {
                    val runtime = requireNotNull(runtimes[membership.playerId]) {
                        "Missing career runtime for ${membership.playerId}"
                    }
                    require(runtime.careerId == careerId) { "Runtime belongs to another career" }
                    val facts = when (runtime.sourceType) {
                        CareerPlayerRuntimeStore.SOURCE_CANONICAL -> {
                            require(procedural[membership.playerId] == null) {
                                "Canonical player ${membership.playerId} has ambiguous procedural facts"
                            }
                            val player = requireNotNull(database.playerDao().findById(membership.playerId)) {
                                "Missing canonical player ${membership.playerId}"
                            }
                            StaticPlayerFacts(
                                name = player.name,
                                country = player.country,
                                position = player.position,
                                status = player.status,
                                side = player.side,
                                cr1 = player.cr1,
                                cr2 = player.cr2,
                            )
                        }

                        CareerPlayerRuntimeStore.SOURCE_PROCEDURAL -> {
                            val player = requireNotNull(procedural[membership.playerId]) {
                                "Missing procedural facts for ${membership.playerId}"
                            }
                            require(player.careerId == careerId) {
                                "Procedural player belongs to another career"
                            }
                            StaticPlayerFacts(
                                name = player.name,
                                country = player.country,
                                position = player.position,
                                status = player.status,
                                side = player.side,
                                cr1 = player.cr1,
                                cr2 = player.cr2,
                            )
                        }

                        else -> error(
                            "Unsupported career player source ${runtime.sourceType} for ${membership.playerId}"
                        )
                    }
                    players += PersistedPlayer(
                        playerId = membership.playerId,
                        sourceType = runtime.sourceType,
                        age = runtime.age,
                        overall = runtime.overall,
                        legacyHash = runtime.legacyHash,
                        rosterKind = membership.rosterKind,
                        sourceOrdinal = membership.sourceOrdinal,
                        facts = facts,
                    )
                }
                return PersistedClubRoster(clubId, legacyClubId, players)
            }

            PersistedMatchRoster(
                careerId = careerId,
                scheduled = scheduled,
                home = resolveClub(homeClub.id, homeClub.legacyId),
                away = resolveClub(awayClub.id, awayClub.legacyId),
            )
        }
    }

    data class TransientPlayerEvidence(
        val playerId: String,
        val legacyG0: Int,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyR: Int,
        val energy: Int,
        val legacyYellowCount: Int = 0,
        val legacyStatM: Int = 0,
        val legacyStatN: Int = 0,
        val selectedOrUsed: Boolean = false,
        val clubSeasonStats: List<LegacyPlayerClubSeasonStatsRules.Entry>? = emptyList(),
    )

    data class TransientClubEvidence(
        val active: List<TransientPlayerEvidence>,
        val bench: List<TransientPlayerEvidence>,
        val substitutionsRemaining: Int,
        val legacyModeFlag: Boolean,
    )

    data class TransientMatchEvidence(
        val currentSeasonId: Int,
        val home: TransientClubEvidence,
        val away: TransientClubEvidence,
    )

    /**
     * Builds the certified Phase 8 transient state without guessing any match-only legacy field.
     * Persisted age, overall and club/player identity are retained from [roster]; every genuinely
     * non-persisted value is carried explicitly by [evidence]. Unselected squad members do not need
     * evidence entries.
     *
     * The Phase 8 injury `skill` is initialized from persisted [PersistedPlayer.overall] because the
     * official Java+SMALI proves both `best.o.O()` and `best.o.m(c0)` read/mutate the same field `j`.
     */
    fun hydratePhase8State(
        roster: PersistedMatchRoster,
        evidence: TransientMatchEvidence,
    ): LegacyMatchTransientRuntime.State<PersistedClubRoster, PersistedPlayer> {
        fun hydrateClub(
            persisted: PersistedClubRoster,
            transient: TransientClubEvidence,
        ): LegacyMatchTransientRuntime.Club<PersistedClubRoster, PersistedPlayer> {
            val persistedById = persisted.players.associateBy { it.playerId }
            val selectedIds = (transient.active + transient.bench).map { it.playerId }
            require(selectedIds.size == selectedIds.toSet().size) {
                "A player cannot be both active/bench or repeated for club ${persisted.clubId}"
            }

            fun hydratePlayer(seed: TransientPlayerEvidence): LegacyMatchTransientRuntime.Player<PersistedPlayer> {
                val player = requireNotNull(persistedById[seed.playerId]) {
                    "Player ${seed.playerId} does not belong to scheduled club ${persisted.clubId}"
                }
                return LegacyMatchTransientRuntime.Player(
                    value = player,
                    legacyG0 = seed.legacyG0,
                    legacyL0 = seed.legacyL0,
                    legacyF0 = seed.legacyF0,
                    legacyR = seed.legacyR,
                    age = player.age,
                    energy = seed.energy,
                    skill = player.overall,
                    legacyYellowCount = seed.legacyYellowCount,
                    legacyStatM = seed.legacyStatM,
                    legacyStatN = seed.legacyStatN,
                    selectedOrUsed = seed.selectedOrUsed,
                    clubSeasonStats = seed.clubSeasonStats,
                )
            }

            return LegacyMatchTransientRuntime.Club(
                value = persisted,
                legacyClubId = persisted.legacyClubId,
                active = transient.active.map(::hydratePlayer).toMutableList(),
                bench = transient.bench.map(::hydratePlayer).toMutableList(),
                substitutionsRemaining = transient.substitutionsRemaining,
                legacyModeFlag = transient.legacyModeFlag,
            )
        }

        return LegacyMatchTransientRuntime.State(
            currentSeasonId = evidence.currentSeasonId,
            home = hydrateClub(roster.home, evidence.home),
            away = hydrateClub(roster.away, evidence.away),
        )
    }
}

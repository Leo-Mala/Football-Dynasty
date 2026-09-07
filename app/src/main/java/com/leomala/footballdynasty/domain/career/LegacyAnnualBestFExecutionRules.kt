package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/** End-to-end transient execution of reachable legacy `best.f.n(...)` / `q(...)` / `p()`. */
object LegacyAnnualBestFExecutionRules {
    data class QAttempt<GroupId, ClubId>(
        val groups: List<LegacyAnnualBestFSourceRules.Group<GroupId, ClubId>>,
        val markedLegacyD0True: List<ClubId>,
        val shuffledCandidates: List<LegacyAnnualBestFSourceRules.Club<ClubId>>,
        val selected: LegacyAnnualBestFSourceRules.Club<ClubId>?,
    )

    data class FallbackAttempt<ClubId>(
        val shuffledCandidates: List<LegacyAnnualBestFSourceRules.Club<ClubId>>,
        val selected: LegacyAnnualBestFSourceRules.Club<ClubId>?,
    )

    data class Result<GroupId, ClubId>(
        val route: LegacyAnnualSelectionRules.BestFNRoute,
        val qAttempts: List<QAttempt<GroupId, ClubId>>,
        val fallbackAttempt: FallbackAttempt<ClubId>?,
        val selected: LegacyAnnualBestFSourceRules.Club<ClubId>?,
    )

    fun <GroupId, ClubId> select(
        random: RandomSource,
        mode: Int,
        subjectOverall: Int,
        subjectPosition: Int,
        subjectO0: Boolean,
        currentClubId: ClubId,
        currentLegacyO: Int,
        currentLegacyJ: Int,
        currentLegacyP0: Int,
        currentQ0: Boolean,
        pools: LegacyAnnualBestFSourceRules.GroupPools<GroupId, ClubId>,
        allClubs: List<LegacyAnnualBestFSourceRules.Club<ClubId>>,
        positionCounts: (LegacyAnnualBestFSourceRules.Club<ClubId>) -> IntArray,
    ): Result<GroupId, ClubId> {
        val route =
            LegacyAnnualSelectionRules.bestFNRoute(
                random = random,
                subjectO = subjectOverall,
                subjectO0 = subjectO0,
                currentQ0 = currentQ0,
            )
        val attempts = mutableListOf<QAttempt<GroupId, ClubId>>()
        var selected: LegacyAnnualBestFSourceRules.Club<ClubId>? = null

        fun runQ(groups: List<LegacyAnnualBestFSourceRules.Group<GroupId, ClubId>>) {
            if (selected != null) return
            val collected =
                LegacyAnnualBestFSourceRules.collectQCandidates(
                    groups = groups,
                    currentClubId = currentClubId,
                    currentLegacyO = currentLegacyO,
                    currentLegacyJ = currentLegacyJ,
                    currentLegacyP0 = currentLegacyP0,
                    subjectOverall = subjectOverall,
                    mode = mode,
                )
            val shuffled = collected.map { it.club }.toMutableList()
            if (shuffled.isNotEmpty()) {
                LegacyAnnualRandomRules.shuffleInPlace(shuffled, random)
            }

            val chosen =
                shuffled.firstOrNull { club ->
                    when (mode) {
                        1 ->
                            LegacyAnnualSelectionRules.bestC0A1(
                                rosterSize = club.rosterSize,
                                targetR0 = club.legacyR0,
                                targetO = club.legacyO,
                                targetP0 = club.legacyP0,
                                subjectOverall = subjectOverall,
                            )

                        2 ->
                            LegacyAnnualSelectionRules.bestFMode2CandidateEligible(
                                rosterSize = club.rosterSize,
                                candidateP0 = club.legacyP0,
                            )

                        else ->
                            LegacyAnnualSelectionRules.bestC0Z0(
                                rosterSize = club.rosterSize,
                                targetR0 = club.legacyR0,
                                targetO = club.legacyO,
                                targetP0 = club.legacyP0,
                                subjectOverall = subjectOverall,
                                subjectPosition = subjectPosition,
                                enforcePositionCaps = !currentQ0,
                                positionCounts = positionCounts(club),
                            )
                    }
                }

            attempts +=
                QAttempt(
                    groups = groups,
                    markedLegacyD0True = collected.map { it.club.id },
                    shuffledCandidates = shuffled,
                    selected = chosen,
                )
            selected = chosen
        }

        when (route) {
            LegacyAnnualSelectionRules.BestFNRoute.G_THEN_OPTIONAL_H -> {
                runQ(pools.primary)
                if (selected == null && currentLegacyP0 > 2) runQ(pools.secondary)
            }

            LegacyAnnualSelectionRules.BestFNRoute.OPTIONAL_I_THEN_OPTIONAL_H_THEN_G -> {
                if (subjectO0) runQ(pools.tertiary)
                if (selected == null && currentLegacyP0 > 2) runQ(pools.secondary)
                if (selected == null) runQ(pools.primary)
            }
        }

        var fallbackAttempt: FallbackAttempt<ClubId>? = null
        if (selected == null) {
            val fallback = LegacyAnnualBestFSourceRules.collectFallbackCandidates(allClubs).toMutableList()
            if (fallback.isNotEmpty()) {
                LegacyAnnualRandomRules.shuffleInPlace(fallback, random)
            }
            val chosen =
                fallback.firstOrNull { club ->
                    LegacyAnnualSelectionRules.bestC0Z0(
                        rosterSize = club.rosterSize,
                        targetR0 = club.legacyR0,
                        targetO = club.legacyO,
                        targetP0 = club.legacyP0,
                        subjectOverall = subjectOverall,
                        subjectPosition = subjectPosition,
                        enforcePositionCaps = !currentQ0,
                        positionCounts = positionCounts(club),
                    )
                }
            fallbackAttempt = FallbackAttempt(shuffledCandidates = fallback, selected = chosen)
            selected = chosen
        }

        return Result(
            route = route,
            qAttempts = attempts,
            fallbackAttempt = fallbackAttempt,
            selected = selected,
        )
    }
}

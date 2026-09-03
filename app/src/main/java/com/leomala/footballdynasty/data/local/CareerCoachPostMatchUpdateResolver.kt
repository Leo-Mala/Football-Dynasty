package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyCoachAdjustmentSide
import com.leomala.footballdynasty.domain.manager.LegacyCoachAssociatedClub
import com.leomala.footballdynasty.domain.manager.LegacyCoachLeagueStandingInput
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchClubManagerRef
import com.leomala.footballdynasty.domain.manager.LegacyCoachMatchManagerResolutionRule
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchAdjustmentContext
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchAdjustmentRule
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchAdjustmentState
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchStatisticsContext
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchStatisticsRule
import com.leomala.footballdynasty.domain.manager.LegacyCoachPostMatchStatisticsState
import com.leomala.footballdynasty.domain.manager.LegacyManagerIdentityRef

/**
 * Exact legacy-only values consumed by `best.f0.i(best.s)` that are not yet derivable from the
 * generic persisted match/competition projections.
 *
 * A present map entry with a null standing is meaningful: it represents a characterized legacy
 * `c0.I(...)` miss. An absent map entry means the caller has not supplied that evidence and this
 * resolver fails closed before producing a partial coach mutation.
 */
data class CareerCoachPostMatchAdjustmentEvidence(
    val homeStrength: Int,
    val awayStrength: Int,
    val isLegacyLeagueCompetition: Boolean,
    val standingByClubId: Map<String, LegacyCoachLeagueStandingInput?> = emptyMap(),
    val cashByClubId: Map<String, Long> = emptyMap(),
)

/** Fully characterized input needed to compose reachable `best.s.f() -> f0.j() -> f0.i()`. */
data class CareerCoachPostMatchLegacyEvidence(
    val seasonId: Int,
    val rawCompetitionType: Int,
    /** Exact `konrent.t.x0()` value; null means the legacy competition is not that concrete class. */
    val leagueCompetitionSubtype: Int?,
    val homeClub: LegacyCoachAssociatedClub,
    val awayClub: LegacyCoachAssociatedClub,
    val homeStoredManagerId: Int,
    val awayStoredManagerId: Int,
    val homeGoals: Int,
    val awayGoals: Int,
    /** Exact legacy ids for every current/alternative club referenced by a resolved manager. */
    val associatedClubsById: Map<String, LegacyCoachAssociatedClub>,
    /** Required only when the normal `best.s.f()` caller reaches `f0.i()`. */
    val adjustment: CareerCoachPostMatchAdjustmentEvidence? = null,
)

/**
 * Pure composition boundary for the already-characterized coach post-match path.
 *
 * This object intentionally does not infer `konrent.t.x0()`, relegation count or raw club strength
 * from modern fields. Callers must provide those legacy-only values explicitly. The returned list
 * is ready for [CareerMatchAtomicCommitter] and preserves the exact home-then-away `best.s.f()`
 * manager resolution order.
 */
object CareerCoachPostMatchUpdateResolver {
    /** Convenience path for fully materialized fixtures/tests. */
    fun resolve(
        managersInWorldOrder: List<CareerCoachRuntimeState>,
        evidence: CareerCoachPostMatchLegacyEvidence,
    ): List<CareerMatchCoachUpdate> = resolve(
        managersInWorldOrder = managersInWorldOrder.map {
            LegacyManagerIdentityRef(it.sourceOrdinal, it.legacyManagerId)
        },
        coachStateBySourceOrdinal = managersInWorldOrder.associateBy { it.sourceOrdinal },
        evidence = evidence,
    )

    /**
     * Production path: V9 owns the complete ordered manager identity list while V11 is required
     * only for managers actually resolved by `c0.y0()`. Unrelated managers therefore do not need a
     * synthesized coach row, while a resolved manager with missing V11 state still fails closed.
     */
    fun resolve(
        managersInWorldOrder: List<LegacyManagerIdentityRef>,
        coachStateBySourceOrdinal: Map<Int, CareerCoachRuntimeState>,
        evidence: CareerCoachPostMatchLegacyEvidence,
    ): List<CareerMatchCoachUpdate> {
        require(evidence.homeClub.clubId != evidence.awayClub.clubId) {
            "Coach post-match evidence requires distinct match clubs"
        }
        require(evidence.associatedClubsById[evidence.homeClub.clubId] == evidence.homeClub) {
            "Home club legacy identity must be explicit in associated club evidence"
        }
        require(evidence.associatedClubsById[evidence.awayClub.clubId] == evidence.awayClub) {
            "Away club legacy identity must be explicit in associated club evidence"
        }
        require(managersInWorldOrder.map { it.sourceOrdinal } == managersInWorldOrder.indices.toList()) {
            "Manager identities must preserve contiguous legacy ArrayList order"
        }
        coachStateBySourceOrdinal.forEach { (ordinal, state) ->
            require(state.sourceOrdinal == ordinal) {
                "V11 coach state key $ordinal diverges from source ordinal ${state.sourceOrdinal}"
            }
            val identity = requireNotNull(managersInWorldOrder.getOrNull(ordinal)) {
                "V11 coach state $ordinal has no ordered V9 manager identity"
            }
            require(identity.legacyManagerId == state.legacyManagerId) {
                "V11 coach manager id ${state.legacyManagerId} diverges from V9 id ${identity.legacyManagerId} at $ordinal"
            }
        }

        val resolvedManagers = LegacyCoachMatchManagerResolutionRule.orderedForMatch(
            home = LegacyCoachMatchClubManagerRef(
                clubId = evidence.homeClub.clubId,
                storedManagerId = evidence.homeStoredManagerId,
            ),
            away = LegacyCoachMatchClubManagerRef(
                clubId = evidence.awayClub.clubId,
                storedManagerId = evidence.awayStoredManagerId,
            ),
            managersInWorldOrder = managersInWorldOrder,
        )
        val stateByOrdinal = coachStateBySourceOrdinal.toMutableMap()

        return buildList {
            resolvedManagers.forEach { resolved ->
                val before = requireNotNull(stateByOrdinal[resolved.manager.sourceOrdinal]) {
                    "Resolved legacy manager ${resolved.manager.sourceOrdinal} has no V11 coach state"
                }
                require(before.legacyManagerId == resolved.manager.legacyManagerId) {
                    "Resolved V9/V11 manager identity diverged at ${resolved.manager.sourceOrdinal}"
                }
                val currentClub = resolveAssociatedClub(before.currentClubId, evidence.associatedClubsById)
                val alternativeClub = resolveAssociatedClub(before.alternativeClubId, evidence.associatedClubsById)

                val statistics = LegacyCoachPostMatchStatisticsRule.apply(
                    before = LegacyCoachPostMatchStatisticsState(
                        rawD = before.rawD,
                        rawE = before.rawE,
                        rawF = before.rawF,
                        rawO = before.rawO,
                        records = before.records,
                    ),
                    context = LegacyCoachPostMatchStatisticsContext(
                        seasonId = evidence.seasonId,
                        currentClub = currentClub,
                        alternativeClub = alternativeClub,
                        homeClubId = evidence.homeClub.clubId,
                        awayClubId = evidence.awayClub.clubId,
                        homeGoals = evidence.homeGoals,
                        awayGoals = evidence.awayGoals,
                        rawCompetitionType = evidence.rawCompetitionType,
                        leagueCompetitionSubtype = evidence.leagueCompetitionSubtype,
                    ),
                )

                var after = before.copy(
                    rawD = statistics.state.rawD,
                    rawE = statistics.state.rawE,
                    rawF = statistics.state.rawF,
                    rawO = statistics.state.rawO,
                    records = statistics.state.records,
                )

                if (evidence.rawCompetitionType in LegacyCoachPostMatchAdjustmentRule.callerCompetitionTypes) {
                    val adjustmentEvidence = requireNotNull(evidence.adjustment) {
                        "Competition ${evidence.rawCompetitionType} requires exact legacy f0.i evidence"
                    }
                    val managerSide = when (before.currentClubId) {
                        evidence.homeClub.clubId -> LegacyCoachAdjustmentSide.HOME
                        evidence.awayClub.clubId -> LegacyCoachAdjustmentSide.AWAY
                        else -> LegacyCoachAdjustmentSide.NONE
                    }
                    val standing = resolveStanding(
                        currentClubId = before.currentClubId,
                        managerSide = managerSide,
                        rawCompetitionType = evidence.rawCompetitionType,
                        adjustment = adjustmentEvidence,
                    )
                    val adjustment = LegacyCoachPostMatchAdjustmentRule.apply(
                        before = LegacyCoachPostMatchAdjustmentState(
                            rawG = after.rawG,
                            rawH = after.rawH,
                        ),
                        context = LegacyCoachPostMatchAdjustmentContext(
                            rawCompetitionType = evidence.rawCompetitionType,
                            managerSide = managerSide,
                            homeGoals = evidence.homeGoals,
                            awayGoals = evidence.awayGoals,
                            homeStrength = adjustmentEvidence.homeStrength,
                            awayStrength = adjustmentEvidence.awayStrength,
                            isLegacyLeagueCompetition = adjustmentEvidence.isLegacyLeagueCompetition,
                            managerStanding = standing,
                            managerIsUserControlled = before.isUserControlled,
                            currentClubCash = before.currentClubId?.let(adjustmentEvidence.cashByClubId::get),
                        ),
                    )
                    after = after.copy(
                        rawG = adjustment.state.rawG,
                        rawH = adjustment.state.rawH,
                    )
                }

                add(
                    CareerMatchCoachUpdate(
                        resolvedClubId = resolved.clubId,
                        expectedBefore = before,
                        after = after,
                    )
                )
                // Both club ids may resolve to the same first manager ArrayList entry. Legacy calls
                // j()/i() twice in that case, so the away call must observe the home call's writes.
                stateByOrdinal[before.sourceOrdinal] = after
            }
        }
    }

    private fun resolveAssociatedClub(
        clubId: String?,
        associatedClubsById: Map<String, LegacyCoachAssociatedClub>,
    ): LegacyCoachAssociatedClub? {
        if (clubId == null) return null
        return requireNotNull(associatedClubsById[clubId]) {
            "Missing exact legacy club identity for manager association $clubId"
        }
    }

    private fun resolveStanding(
        currentClubId: String?,
        managerSide: LegacyCoachAdjustmentSide,
        rawCompetitionType: Int,
        adjustment: CareerCoachPostMatchAdjustmentEvidence,
    ): LegacyCoachLeagueStandingInput? {
        if (managerSide == LegacyCoachAdjustmentSide.NONE || rawCompetitionType !in setOf(1, 3)) {
            return null
        }
        val clubId = requireNotNull(currentClubId) {
            "Match-side manager must retain its current club"
        }
        require(adjustment.standingByClubId.containsKey(clubId)) {
            "Missing exact legacy c0.I standing evidence for $clubId"
        }
        return adjustment.standingByClubId[clubId]
    }
}

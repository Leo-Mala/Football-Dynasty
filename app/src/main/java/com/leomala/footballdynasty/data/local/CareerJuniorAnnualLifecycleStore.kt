package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyProceduralMaterializationRules
import com.leomala.footballdynasty.domain.career.LegacyProceduralPlayerRules
import com.leomala.footballdynasty.domain.manager.LegacyJuniorDraftFieldRules
import com.leomala.footballdynasty.domain.manager.LegacyJuniorRuntimeRules
import com.leomala.footballdynasty.foundation.random.RandomSource
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource

/** Source-resolved club facts required by the characterized annual `best.p.c(c0)` lifecycle. */
data class CareerJuniorAnnualClubTarget(
    val clubId: String,
    val rosterKind: String,
    val legacyR0: Boolean,
    val legacyO: Int,
    val legacyP0: Int,
    val legacyJ: Int,
    val clubLevel: Int,
    val currentYear: Int,
    val currentGameEpochMillis: Long,
    val clubP0: Int,
    val clubB0: Int,
    val clubQ0: Boolean,
    val seniorPositionCounts: List<Int>,
)

data class CareerJuniorAnnualLifecycleResult(
    val promoted: List<CareerPlayerRuntimeStore.PlayerSnapshot>,
    val stateAfter: CareerState,
)

/**
 * Atomic persisted equivalent of the characterized annual junior lifecycle `best.b.q()`.
 *
 * All clubs are processed inside the same persisted RNG/Room transaction. Each club iterates only
 * its original draft snapshot. Promoted drafts are removed and replacement drafts are appended only
 * after that snapshot is exhausted; replacement generation itself happens immediately at the
 * proven `best.p.d(..., null, ..., TRUE)` call site, preserving RNG order. Materialized players are
 * written inside the transaction when the legacy club exposure occurs; because Room does not expose
 * the transaction's intermediate state externally, the commit is observationally equivalent to the
 * legacy D1 global append after all clubs.
 */
class CareerJuniorAnnualLifecycleStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun run(
        expectedBefore: CareerState,
        clubs: List<CareerJuniorAnnualClubTarget>,
        refreshDraft: (current: CareerJuniorDraftState, random: RandomSource) -> CareerJuniorDraftState,
        generateReplacement: (clubId: String, random: RandomSource) -> CareerJuniorDraftState,
    ): CareerJuniorAnnualLifecycleResult {
        require(clubs.map { it.clubId }.distinct().size == clubs.size) { "Duplicate junior annual club target" }
        clubs.forEach {
            require(it.clubId.isNotBlank())
            require(it.rosterKind.isNotBlank())
            require(it.seniorPositionCounts.size >= 5)
        }

        val randomResult = CareerManagerProgressionRandomStore(database, clockMillis).run(expectedBefore) { random ->
            val draftDao = database.careerJuniorDraftDao()
            val playerDao = database.careerPlayerRuntimeDao()
            val playerStore = CareerPlayerRuntimeStore(database)
            val promoted = mutableListOf<CareerPlayerRuntimeStore.PlayerSnapshot>()

            for (target in clubs) {
                val original = draftDao.listForClub(expectedBefore.id, target.clubId)
                val stagedRemovals = mutableListOf<Int>()
                val stagedReplacements = mutableListOf<CareerJuniorDraftState>()
                val immediateUpdates = mutableListOf<CareerJuniorDraftEntity>()
                val positionCounts = target.seniorPositionCounts.toMutableList()
                var seniorCount = playerDao.membershipsForClub(
                    expectedBefore.id,
                    target.clubId,
                    target.rosterKind,
                ).size

                for (entity in original) {
                    val aged = entity.toJuniorState().copy(legacyC = entity.legacyC + 1)
                    val decision = LegacyJuniorRuntimeRules.annualDecision(
                        age = entity.legacyC,
                        legacyN = entity.legacyN,
                        legacyE = entity.legacyE,
                        context = LegacyJuniorRuntimeRules.AnnualContext(
                            clubP0 = target.clubP0,
                            seniorPositionCounts = positionCounts,
                            seniorCount = seniorCount,
                            clubB0 = target.clubB0,
                            clubQ0 = target.clubQ0,
                        ),
                    )
                    check(decision.ageAfterIncrement == aged.legacyC)

                    when (decision.action) {
                        LegacyJuniorRuntimeRules.AnnualAction.NONE -> {
                            immediateUpdates += aged.toEntity(expectedBefore.id, target.clubId, entity.sourceOrdinal)
                        }
                        LegacyJuniorRuntimeRules.AnnualAction.REFRESH_DRAFT -> {
                            val refreshed = refreshDraft(aged, random)
                            check(refreshed.legacyC == aged.legacyC) {
                                "Annual junior refresh must preserve the already incremented age"
                            }
                            immediateUpdates += refreshed.toEntity(expectedBefore.id, target.clubId, entity.sourceOrdinal)
                        }
                        LegacyJuniorRuntimeRules.AnnualAction.PROMOTE,
                        LegacyJuniorRuntimeRules.AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT -> {
                            val effects = LegacyJuniorDraftFieldRules.promotionListEffects(
                                LegacyJuniorDraftFieldRules.PromotionRoute.ANNUAL_TRUE,
                            )
                            check(!effects.removeDraftFromClubImmediately)
                            check(effects.stageDraftInLegacyL1 && effects.stageMaterializedPlayerInLegacyJ1)

                            val materialized = LegacyProceduralMaterializationRules.materialize(
                                random = random,
                                draft = aged.toProceduralDraft(),
                                target = LegacyProceduralMaterializationRules.TargetContext(
                                    legacyR0 = target.legacyR0,
                                    legacyO = target.legacyO,
                                    legacyP0 = target.legacyP0,
                                    legacyJ = target.legacyJ,
                                    clubLevel = target.clubLevel,
                                    currentYear = target.currentYear,
                                ),
                            )
                            val stateful = random as? StatefulJavaRandomSource
                                ?: error("Career annual junior lifecycle requires persisted stateful Java RNG")
                            val playerId = LegacyProceduralMaterializationRules.deterministicPlayerId(
                                expectedBefore.id,
                                stateful.snapshot().draws,
                            )
                            val membershipOrdinal = (
                                playerDao.maxMembershipOrdinal(
                                    expectedBefore.id,
                                    target.clubId,
                                    target.rosterKind,
                                ) ?: -1
                            ) + 1
                            val bundle = CareerPlayerRuntimeMapper.procedural(
                                careerId = expectedBefore.id,
                                playerId = playerId,
                                materialized = materialized,
                                target = CareerPlayerRuntimeMapper.TargetContext(
                                    clubId = target.clubId,
                                    legacyR0 = target.legacyR0,
                                    legacyO = target.legacyO,
                                    legacyP0 = target.legacyP0,
                                    legacyF0 = target.clubLevel,
                                    currentYear = target.currentYear,
                                    currentGameEpochMillis = target.currentGameEpochMillis,
                                    rosterKind = target.rosterKind,
                                    sourceOrdinal = membershipOrdinal,
                                ),
                            )
                            promoted += playerStore.saveProceduralPlayer(
                                bundle.runtime,
                                bundle.procedural,
                                bundle.membership,
                            )
                            stagedRemovals += entity.sourceOrdinal
                            seniorCount++
                            positionCounts[aged.legacyE] = positionCounts[aged.legacyE] + 1

                            if (decision.action == LegacyJuniorRuntimeRules.AnnualAction.PROMOTE_AND_STAGE_REPLACEMENT) {
                                stagedReplacements += generateReplacement(target.clubId, random)
                            }
                        }
                    }
                }

                draftDao.upsertAll(immediateUpdates)
                stagedRemovals.forEach { ordinal ->
                    check(draftDao.delete(expectedBefore.id, target.clubId, ordinal) == 1) {
                        "Annual junior draft delete lost race ${expectedBefore.id}/${target.clubId}/$ordinal"
                    }
                }
                var nextOrdinal = (original.maxOfOrNull { it.sourceOrdinal } ?: -1) + 1
                draftDao.upsertAll(
                    stagedReplacements.map { replacement ->
                        replacement.toEntity(expectedBefore.id, target.clubId, nextOrdinal++)
                    }
                )
            }
            promoted.toList()
        }

        return CareerJuniorAnnualLifecycleResult(
            promoted = randomResult.value,
            stateAfter = randomResult.stateAfter,
        )
    }

    private fun CareerJuniorDraftState.toProceduralDraft() = LegacyProceduralPlayerRules.Draft(
        legacyN = legacyN,
        legacyB = legacyB,
        legacyC = legacyC,
        legacyE = legacyE,
        legacyJ = legacyJ,
        legacyL = legacyL,
        legacyD = legacyD,
        name = name,
        legacyG = legacyG,
        legacyF = legacyF,
        legacyO = legacyO,
        legacyM = legacyM,
    )

    private fun CareerJuniorDraftState.toEntity(careerId: String, clubId: String, sourceOrdinal: Int) =
        CareerJuniorDraftEntity(
            careerId = careerId,
            clubId = clubId,
            sourceOrdinal = sourceOrdinal,
            legacyN = legacyN,
            legacyB = legacyB,
            legacyC = legacyC,
            legacyE = legacyE,
            legacyJ = legacyJ,
            legacyL = legacyL,
            legacyD = legacyD,
            name = name,
            legacyG = legacyG,
            legacyF = legacyF,
            legacyO = legacyO,
            legacyM = legacyM,
            legacyH = legacyH,
            legacyI = legacyI,
            developmentRemainder = developmentRemainder,
        )

    private fun CareerJuniorDraftEntity.toJuniorState() = CareerJuniorDraftState(
        legacyN = legacyN,
        legacyB = legacyB,
        legacyC = legacyC,
        legacyE = legacyE,
        legacyJ = legacyJ,
        legacyL = legacyL,
        legacyD = legacyD,
        name = name,
        legacyG = legacyG,
        legacyF = legacyF,
        legacyO = legacyO,
        legacyM = legacyM,
        legacyH = legacyH,
        legacyI = legacyI,
        developmentRemainder = developmentRemainder,
    )
}

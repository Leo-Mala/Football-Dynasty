package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.LegacyProceduralMaterializationRules
import com.leomala.footballdynasty.domain.career.LegacyProceduralPlayerRules
import com.leomala.footballdynasty.domain.manager.LegacyJuniorDraftFieldRules
import com.leomala.footballdynasty.domain.manager.LegacyJuniorRuntimeRules
import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource

/** Persisted target facts required by the proven manual `best.t.e(FALSE, p, c0)` path. */
data class CareerJuniorManualPromotionTarget(
    val legacyR0: Boolean,
    val legacyO: Int,
    val legacyP0: Int,
    val legacyJ: Int,
    val clubLevel: Int,
    val currentYear: Int,
    val currentGameEpochMillis: Long,
    val rosterKind: String,
)

data class CareerJuniorManualPromotionResult(
    val promoted: CareerPlayerRuntimeStore.PlayerSnapshot?,
    val blockedBySeniorLimit: Boolean,
    val stateAfter: CareerState,
)

/**
 * Atomic persistence boundary for reachable manual promotion from `ActivityJuniores`.
 *
 * The draft remains a durable `best.p` until this method reaches the characterized
 * `best.t.e(FALSE, p, c0)` call. Only then are promotion RNG draws consumed and the final
 * procedural player materialized. Draft removal, final player rows/membership and the advanced
 * career RNG commit in the same Room transaction.
 *
 * `rosterKind` is deliberately explicit: the legacy D0 list effect is proven, while translating
 * that opaque legacy list name into a modern roster label is a caller/source-resolution concern
 * and is not invented here.
 */
class CareerJuniorManualPromotionStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun promote(
        expectedBefore: CareerState,
        clubId: String,
        draftSourceOrdinal: Int,
        expectedDraft: CareerJuniorDraftState,
        target: CareerJuniorManualPromotionTarget,
    ): CareerJuniorManualPromotionResult {
        require(clubId.isNotBlank()) { "Club id must not be blank" }
        require(draftSourceOrdinal >= 0) { "Draft ordinal must be non-negative" }
        require(target.rosterKind.isNotBlank()) { "Promotion roster kind must not be blank" }

        val randomResult = CareerManagerProgressionRandomStore(database, clockMillis).run(expectedBefore) { random ->
            val draftDao = database.careerJuniorDraftDao()
            val playerDao = database.careerPlayerRuntimeDao()
            val current = requireNotNull(
                draftDao.listForClub(expectedBefore.id, clubId)
                    .singleOrNull { it.sourceOrdinal == draftSourceOrdinal }
            ) { "Missing junior draft ${expectedBefore.id}/$clubId/$draftSourceOrdinal" }
            require(current.toJuniorState() == expectedDraft) {
                "Stale junior draft ${expectedBefore.id}/$clubId/$draftSourceOrdinal"
            }

            val seniorCount = playerDao.membershipsForClub(
                expectedBefore.id,
                clubId,
                target.rosterKind,
            ).size
            if (!LegacyJuniorRuntimeRules.canPromoteManually(seniorCount)) {
                ManualPromotionMutation(blockedBySeniorLimit = true, promoted = null)
            } else {
                val effects = LegacyJuniorDraftFieldRules.promotionListEffects(
                    LegacyJuniorDraftFieldRules.PromotionRoute.MANUAL_FALSE,
                )
                check(effects.removeDraftFromClubImmediately)
                check(effects.stageMaterializedPlayerInLegacyD0)
                check(!effects.stageDraftInLegacyL1 && !effects.stageMaterializedPlayerInLegacyJ1)

                val materialized = LegacyProceduralMaterializationRules.materialize(
                    random = random,
                    draft = expectedDraft.toProceduralDraft(),
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
                    ?: error("Career promotion requires the persisted stateful Java RNG")
                val playerId = LegacyProceduralMaterializationRules.deterministicPlayerId(
                    expectedBefore.id,
                    stateful.snapshot().draws,
                )
                val membershipOrdinal = (
                    playerDao.maxMembershipOrdinal(expectedBefore.id, clubId, target.rosterKind) ?: -1
                ) + 1
                val bundle = CareerPlayerRuntimeMapper.procedural(
                    careerId = expectedBefore.id,
                    playerId = playerId,
                    materialized = materialized,
                    target = CareerPlayerRuntimeMapper.TargetContext(
                        clubId = clubId,
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
                val promoted = CareerPlayerRuntimeStore(database).saveProceduralPlayer(
                    bundle.runtime,
                    bundle.procedural,
                    bundle.membership,
                )
                check(draftDao.delete(expectedBefore.id, clubId, draftSourceOrdinal) == 1) {
                    "Junior draft delete lost race ${expectedBefore.id}/$clubId/$draftSourceOrdinal"
                }
                ManualPromotionMutation(blockedBySeniorLimit = false, promoted = promoted)
            }
        }

        return CareerJuniorManualPromotionResult(
            promoted = randomResult.value.promoted,
            blockedBySeniorLimit = randomResult.value.blockedBySeniorLimit,
            stateAfter = randomResult.stateAfter,
        )
    }

    private data class ManualPromotionMutation(
        val blockedBySeniorLimit: Boolean,
        val promoted: CareerPlayerRuntimeStore.PlayerSnapshot?,
    )

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

    private fun com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity.toJuniorState() =
        CareerJuniorDraftState(
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
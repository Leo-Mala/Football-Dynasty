package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.manager.LegacyFinanceLedgerRule
import com.leomala.footballdynasty.domain.manager.LegacyJuniorRuntimeRules
import com.leomala.footballdynasty.foundation.random.RandomSource

/** Lossless persisted projection of pre-promotion legacy `best.p` state. */
data class CareerJuniorDraftState(
    val legacyN: Int,
    val legacyB: Boolean,
    val legacyC: Int,
    val legacyE: Int,
    val legacyJ: Int,
    val legacyL: Int,
    val legacyD: Int,
    val name: String,
    val legacyG: Int,
    val legacyF: Int,
    val legacyO: Int,
    val legacyM: Int,
    val legacyH: Int,
    val legacyI: Int,
    val developmentRemainder: Double,
)

data class CareerJuniorTrialResult(
    val availability: LegacyJuniorRuntimeRules.TrialAvailability,
    val generated: List<CareerJuniorDraftState>,
    val stateAfter: CareerState,
)

/**
 * Transactional persistence boundary for the characterized junior tryout/dismissal flow.
 *
 * The tryout intentionally delegates all draft-field generation to [generateDraft]. That callback
 * receives the exact shared career [RandomSource] at the legacy `best.p.d(...)` call site, so no
 * promotion-only player materialization or RNG is pulled forward. The Room transaction commits
 * finance code 9, new pre-promotion drafts and the advanced career RNG together or rolls all three
 * back together.
 */
class CareerJuniorRuntimeStore(
    private val database: FootballDynastyDatabase,
    private val clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val draftDao = database.careerJuniorDraftDao()
    private val managerStore = CareerManagerRuntimeStore(database)

    suspend fun listForClub(careerId: String, clubId: String): List<CareerJuniorDraftState> =
        draftDao.listForClub(careerId, clubId).map { it.toState() }

    suspend fun runTrial(
        expectedBefore: CareerState,
        clubId: String,
        cost: Int,
        generateDraft: (requestedLegacyE: Int, random: RandomSource) -> CareerJuniorDraftState,
    ): CareerJuniorTrialResult {
        require(cost >= 0)
        val randomResult = CareerManagerProgressionRandomStore(database, clockMillis).run(expectedBefore) { random ->
            val financeBefore = requireNotNull(managerStore.clubFinanceState(expectedBefore.id, clubId)) {
                "Missing materialized finance state for junior tryout ${expectedBefore.id}/$clubId"
            }
            require(financeBefore.cash in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
                "Legacy junior cash is outside Int range: ${financeBefore.cash}"
            }
            val existing = draftDao.listForClub(expectedBefore.id, clubId)
            val availability = LegacyJuniorRuntimeRules.trialAvailability(
                cash = financeBefore.cash.toInt(),
                cost = cost,
                juniorCount = existing.size,
            )
            if (availability != LegacyJuniorRuntimeRules.TrialAvailability.READY) {
                TrialMutation(availability, emptyList())
            } else {
                val generated = LegacyJuniorRuntimeRules.executeTrial(
                    random = random,
                    currentJuniorCount = existing.size,
                    generate = generateDraft,
                )
                var nextOrdinal = (existing.maxOfOrNull { it.sourceOrdinal } ?: -1) + 1
                draftDao.upsertAll(
                    generated.map { draft ->
                        draft.toEntity(expectedBefore.id, clubId, nextOrdinal++)
                    }
                )
                val financeAfter = financeBefore.copy(
                    cash = financeBefore.cash - cost.toLong(),
                    ledger = LegacyFinanceLedgerRule.addExpense(
                        state = financeBefore.ledger,
                        amount = cost,
                        rawCategoryCode = LegacyJuniorRuntimeRules.TRIAL_EXPENSE_RAW_CODE,
                    ),
                )
                managerStore.commitFinanceState(
                    careerId = expectedBefore.id,
                    clubId = clubId,
                    expectedBefore = financeBefore,
                    after = financeAfter,
                )
                TrialMutation(availability, generated)
            }
        }
        return CareerJuniorTrialResult(
            availability = randomResult.value.availability,
            generated = randomResult.value.generated,
            stateAfter = randomResult.stateAfter,
        )
    }

    /** `ActivityJuniores` dismissal removes only the selected pre-promotion draft and consumes no RNG. */
    suspend fun dismiss(
        careerId: String,
        clubId: String,
        sourceOrdinal: Int,
        expectedDraft: CareerJuniorDraftState,
    ) = database.withTransaction {
        val current = draftDao.listForClub(careerId, clubId)
            .singleOrNull { it.sourceOrdinal == sourceOrdinal }
        requireNotNull(current) { "Missing junior draft $careerId/$clubId/$sourceOrdinal" }
        require(current.toState() == expectedDraft) {
            "Stale junior draft $careerId/$clubId/$sourceOrdinal"
        }
        check(draftDao.delete(careerId, clubId, sourceOrdinal) == 1) {
            "Junior draft delete lost race $careerId/$clubId/$sourceOrdinal"
        }
    }

    private data class TrialMutation(
        val availability: LegacyJuniorRuntimeRules.TrialAvailability,
        val generated: List<CareerJuniorDraftState>,
    )

    private fun CareerJuniorDraftState.toEntity(
        careerId: String,
        clubId: String,
        sourceOrdinal: Int,
    ) = CareerJuniorDraftEntity(
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

    private fun CareerJuniorDraftEntity.toState() = CareerJuniorDraftState(
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

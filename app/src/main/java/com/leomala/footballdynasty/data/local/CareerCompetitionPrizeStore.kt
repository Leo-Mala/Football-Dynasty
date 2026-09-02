package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyCompetitionPrizeRule
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState

/**
 * Persisted winner-credit boundary for the characterized `konrent.f0.e -> l -> d` prize path.
 *
 * Calculation, persisted legacy `Q0()` eligibility, ledger category and cash mutation are evaluated
 * before a single stale-checked Room finance commit. No competition or prize policy is introduced
 * here. The caller supplies only competition-owned fields; club eligibility is resolved from the
 * already-materialized career runtime instead of accepting an injectable raw flag.
 */
class CareerCompetitionPrizeStore(
    private val database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)
    private val managerDao = database.careerManagerRuntimeDao()

    suspend fun applyResolvedWinnerPrize(
        careerId: String,
        winnerClubId: String,
        rawCompetitionType: Int,
        rawStageIndex: Int,
        rawCompetitionI0: Int,
        rawCompetitionPCode: Int,
    ): LegacyFinanceRuntimeState {
        val clubRuntime = requireNotNull(managerDao.findClubRuntime(careerId, winnerClubId)) {
            "Missing materialized club manager runtime $careerId/$winnerClubId"
        }
        val before = clubRuntime.toFinanceState()
        val prize = LegacyCompetitionPrizeRule.prizeAmount(
            rawCompetitionType = rawCompetitionType,
            rawStageIndex = rawStageIndex,
            rawCompetitionI0 = rawCompetitionI0,
            rawCompetitionPCode = rawCompetitionPCode,
        )
        val after = LegacyCompetitionPrizeRule.applyWinnerPrize(
            state = before,
            prizeAmount = prize,
            winnerLegacyQ0 = clubRuntime.active,
        )
        if (after == before) return before

        managerStore.commitFinanceState(
            careerId = careerId,
            clubId = winnerClubId,
            expectedBefore = before,
            after = after,
        )
        return after
    }
}

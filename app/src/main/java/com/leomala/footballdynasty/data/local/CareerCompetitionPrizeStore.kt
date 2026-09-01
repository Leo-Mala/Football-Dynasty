package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.manager.LegacyCompetitionPrizeRule
import com.leomala.footballdynasty.domain.manager.LegacyFinanceRuntimeState

/**
 * Persisted winner-credit boundary for the characterized `konrent.f0.e -> l -> d` prize path.
 *
 * Calculation, legacy `Q0()` eligibility, ledger category and cash mutation are evaluated before a
 * single stale-checked Room finance commit. No competition or prize policy is introduced here.
 */
class CareerCompetitionPrizeStore(
    database: FootballDynastyDatabase,
) {
    private val managerStore = CareerManagerRuntimeStore(database)

    suspend fun applyResolvedWinnerPrize(
        careerId: String,
        winnerClubId: String,
        rawCompetitionType: Int,
        rawStageIndex: Int,
        rawCompetitionI0: Int,
        rawCompetitionPCode: Int,
        winnerLegacyQ0: Boolean,
    ): LegacyFinanceRuntimeState {
        val before = requireNotNull(managerStore.clubFinanceState(careerId, winnerClubId)) {
            "Missing materialized club finance state $careerId/$winnerClubId"
        }
        val prize = LegacyCompetitionPrizeRule.prizeAmount(
            rawCompetitionType = rawCompetitionType,
            rawStageIndex = rawStageIndex,
            rawCompetitionI0 = rawCompetitionI0,
            rawCompetitionPCode = rawCompetitionPCode,
        )
        val after = LegacyCompetitionPrizeRule.applyWinnerPrize(
            state = before,
            prizeAmount = prize,
            winnerLegacyQ0 = winnerLegacyQ0,
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

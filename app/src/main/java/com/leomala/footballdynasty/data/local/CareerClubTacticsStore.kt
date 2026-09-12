package com.leomala.footballdynasty.data.local

import androidx.room.withTransaction
import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.domain.manager.LegacyTacticsRawState

/**
 * Durable owner for the serialized legacy club tactics slice `best.c0.S/T`.
 *
 * Values are persisted and restored raw. This store deliberately does not normalize option codes,
 * apply match-engine fallbacks or infer missing club rows.
 */
class CareerClubTacticsStore(
    private val database: FootballDynastyDatabase,
) {
    suspend fun load(careerId: String, clubId: String): LegacyTacticsRawState? =
        database.withTransaction {
            requireIds(careerId, clubId)
            database.careerManagerRuntimeDao()
                .findClubRuntime(careerId, clubId)
                ?.let(::toRawState)
        }

    suspend fun save(
        careerId: String,
        clubId: String,
        state: LegacyTacticsRawState,
    ): LegacyTacticsRawState = database.withTransaction {
        requireIds(careerId, clubId)
        require(state.optionSlots.size == OPTION_SLOT_COUNT) {
            "Legacy best.c0.S must contain exactly $OPTION_SLOT_COUNT option slots"
        }
        val dao = database.careerManagerRuntimeDao()
        val current = requireNotNull(dao.findClubRuntime(careerId, clubId)) {
            "Missing persisted club runtime for career=$careerId club=$clubId"
        }
        dao.upsertClubRuntime(
            current.copy(
                legacyTacticOption0 = state.optionSlots[0],
                legacyTacticOption1 = state.optionSlots[1],
                legacyTacticOption2 = state.optionSlots[2],
                legacyTacticOption3 = state.optionSlots[3],
                legacyTacticCheckboxT = state.checkboxT,
            )
        )
        state
    }

    companion object {
        const val OPTION_SLOT_COUNT: Int = 4

        internal fun toRawState(entity: CareerClubManagerRuntimeEntity): LegacyTacticsRawState =
            LegacyTacticsRawState(
                optionSlots = listOf(
                    entity.legacyTacticOption0,
                    entity.legacyTacticOption1,
                    entity.legacyTacticOption2,
                    entity.legacyTacticOption3,
                ),
                checkboxT = entity.legacyTacticCheckboxT,
            )

        private fun requireIds(careerId: String, clubId: String) {
            require(careerId.isNotBlank()) { "Career id must not be blank" }
            require(clubId.isNotBlank()) { "Club id must not be blank" }
        }
    }
}

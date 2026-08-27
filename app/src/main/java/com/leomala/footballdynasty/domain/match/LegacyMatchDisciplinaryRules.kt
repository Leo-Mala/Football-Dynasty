package com.leomala.footballdynasty.domain.match

/** Direct event-type routing from legacy `best.s.c(...)` and `best.s.d(...)`. */
object LegacyMatchDisciplinaryRules {
    data class LegacyCResult(
        val updatedLegacyCount: Int,
        val eventType: LegacyMatchEventType,
    )

    fun applyLegacyC(previousLegacyCount: Int): LegacyCResult {
        val updated = previousLegacyCount + 1
        val type = if (updated == 2) {
            LegacyMatchEventType.SECOND_YELLOW_RED
        } else {
            LegacyMatchEventType.YELLOW_CARD
        }
        return LegacyCResult(updated, type)
    }

    fun legacyDType(): LegacyMatchEventType = LegacyMatchEventType.RED_CARD
}

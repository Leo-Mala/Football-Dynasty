package com.leomala.footballdynasty.domain.manager

enum class LegacySavedTacticsSaveStatus {
    EMPTY_NAME,
    NAME_TOO_LONG,
    SAVED,
}

data class LegacySavedTacticSnapshot(
    val displayName: String,
    val formationIndex: Int,
    val legacyRuntimePlayerIds: List<Int>,
    val slotCodes: List<Int>,
)

data class LegacySavedTacticsSaveResult(
    val status: LegacySavedTacticsSaveStatus,
    val savedTactics: List<LegacySavedTacticSnapshot>,
    val feedbackVisible: Boolean,
    val inputTextAfter: String,
    val refreshSavedTacticsList: Boolean,
)

/**
 * Exact mutation plan from the fully recovered `ActivitySavedTatics.g()` method.
 *
 * The legacy method validates only exact empty text and a 30-code-unit maximum; it does not trim.
 * On success it copies the current tactic's parallel player/slot lists, stores null player slots as
 * `-1`, prefixes the typed name with the current formation label, appends the snapshot, clears the
 * input, shows feedback and refreshes the saved-tactics list.
 */
object LegacySavedTacticsRule {
    const val maximumNameLength: Int = 30
    const val nullPlayerSentinel: Int = -1

    fun save(
        inputName: String,
        formationIndex: Int,
        formationNames: List<String>,
        currentLegacyRuntimePlayerIds: List<Int?>,
        currentSlotCodes: List<Int>,
        existingSavedTactics: List<LegacySavedTacticSnapshot>,
    ): LegacySavedTacticsSaveResult {
        if (inputName == "") {
            return rejected(
                status = LegacySavedTacticsSaveStatus.EMPTY_NAME,
                inputName = inputName,
                existingSavedTactics = existingSavedTactics,
            )
        }
        if (inputName.length > maximumNameLength) {
            return rejected(
                status = LegacySavedTacticsSaveStatus.NAME_TOO_LONG,
                inputName = inputName,
                existingSavedTactics = existingSavedTactics,
            )
        }

        val storedPlayerIds = ArrayList<Int>(currentLegacyRuntimePlayerIds.size)
        val storedSlotCodes = ArrayList<Int>(currentLegacyRuntimePlayerIds.size)
        for (index in currentLegacyRuntimePlayerIds.indices) {
            storedPlayerIds += currentLegacyRuntimePlayerIds[index] ?: nullPlayerSentinel
            // Intentionally indexed independently: the legacy method throws if this parallel list is short.
            storedSlotCodes += currentSlotCodes[index]
        }

        val snapshot = LegacySavedTacticSnapshot(
            displayName = formationNames[formationIndex] + " " + inputName,
            formationIndex = formationIndex,
            legacyRuntimePlayerIds = storedPlayerIds,
            slotCodes = storedSlotCodes,
        )
        return LegacySavedTacticsSaveResult(
            status = LegacySavedTacticsSaveStatus.SAVED,
            savedTactics = existingSavedTactics + snapshot,
            feedbackVisible = true,
            inputTextAfter = "",
            refreshSavedTacticsList = true,
        )
    }

    private fun rejected(
        status: LegacySavedTacticsSaveStatus,
        inputName: String,
        existingSavedTactics: List<LegacySavedTacticSnapshot>,
    ): LegacySavedTacticsSaveResult = LegacySavedTacticsSaveResult(
        status = status,
        savedTactics = existingSavedTactics,
        feedbackVisible = true,
        inputTextAfter = inputName,
        refreshSavedTacticsList = false,
    )
}

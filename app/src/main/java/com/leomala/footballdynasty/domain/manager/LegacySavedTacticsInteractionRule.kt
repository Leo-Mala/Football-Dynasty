package com.leomala.footballdynasty.domain.manager

data class LegacySavedTacticsSpinnerState(
    val enabled: Boolean,
    val names: List<String>?,
    val selectedIndex: Int?,
)

data class LegacySavedTacticsDeleteResult(
    val savedTactics: List<LegacySavedTacticSnapshot>,
    val spinnerState: LegacySavedTacticsSpinnerState,
)

data class LegacySavedTacticsLoadResult(
    val resultCode: Int?,
    val savedTacticIndex: Int?,
    val finishActivity: Boolean,
)

/**
 * Exact list/delete/load dispatch from `ActivitySavedTatics.e/b/f` plus the request/result guard
 * in `ActivityEscalacao.onActivityResult` from the official corpus.
 *
 * The legacy guards only reject an empty list or `index >= size`; they do not reject a negative
 * index. We therefore intentionally preserve the subsequent negative-index failure instead of
 * adding a modern clamp that would change observable behavior.
 */
object LegacySavedTacticsInteractionRule {
    const val legacyLoadRequestCode: Int = 101
    const val legacyOkResultCode: Int = -1

    fun refresh(savedTactics: List<LegacySavedTacticSnapshot>): LegacySavedTacticsSpinnerState {
        if (savedTactics.isEmpty()) {
            return LegacySavedTacticsSpinnerState(
                enabled = false,
                names = null,
                selectedIndex = null,
            )
        }
        return LegacySavedTacticsSpinnerState(
            enabled = true,
            names = savedTactics.map { it.displayName },
            selectedIndex = savedTactics.lastIndex,
        )
    }

    fun delete(
        savedTactics: List<LegacySavedTacticSnapshot>,
        selectedIndex: Int,
    ): LegacySavedTacticsDeleteResult {
        if (savedTactics.isEmpty() || savedTactics.size <= selectedIndex) {
            return LegacySavedTacticsDeleteResult(savedTactics, refresh(savedTactics))
        }
        val updated = savedTactics.toMutableList().also { it.removeAt(selectedIndex) }
        return LegacySavedTacticsDeleteResult(updated, refresh(updated))
    }

    fun requestLoad(
        savedTactics: List<LegacySavedTacticSnapshot>,
        selectedIndex: Int,
    ): LegacySavedTacticsLoadResult {
        if (savedTactics.isEmpty() || savedTactics.size <= selectedIndex) {
            return LegacySavedTacticsLoadResult(null, null, finishActivity = false)
        }
        savedTactics[selectedIndex]
        return LegacySavedTacticsLoadResult(
            resultCode = legacyOkResultCode,
            savedTacticIndex = selectedIndex,
            finishActivity = true,
        )
    }

    fun consumeLoadResult(
        requestCode: Int,
        resultCode: Int,
        savedTacticIndex: Int,
        savedTactics: List<LegacySavedTacticSnapshot>,
    ): LegacySavedTacticSnapshot? {
        if (requestCode != legacyLoadRequestCode || resultCode != legacyOkResultCode) return null
        if (savedTactics.isEmpty() || savedTactics.size <= savedTacticIndex) return null
        return savedTactics[savedTacticIndex]
    }
}

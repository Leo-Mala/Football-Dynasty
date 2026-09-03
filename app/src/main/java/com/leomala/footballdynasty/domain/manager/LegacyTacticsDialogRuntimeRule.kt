package com.leomala.footballdynasty.domain.manager

/** Exact persistence-independent state used by the readable `DialogTatics.onCreate()/j()` paths. */
data class LegacyTacticsRawState(
    val optionSlots: List<Int>,
    val checkboxT: Boolean,
)

data class LegacyTacticsDialogUiState(
    val optionSlot1Selection: Int?,
    val optionSlot2Selection: Int?,
    val optionSlot3Selection: Int?,
    val checkboxT: Boolean,
    val controlsEnabled: Boolean,
)

enum class LegacySpecialTacticsRole(val actionKey: String) {
    CAPTAIN("cap"),
    FREE_KICK("bFaltas"),
    CORNER("bEscanteios"),
    FALSE_NINE("fNove"),
}

data class LegacySpecialTacticsAssignments<T>(
    val captain: T?,
    val freeKick: T?,
    val corner: T?,
    val falseNine: T?,
)

object LegacyTacticsDialogRuntimeRule {
    /** `onCreate`: reads legacy option slots 1, 3 and 2 plus checkbox T. */
    fun load(
        state: LegacyTacticsRawState,
        editableQ0: Boolean,
    ): LegacyTacticsDialogUiState = LegacyTacticsDialogUiState(
        optionSlot1Selection = state.optionSlots[1].takeIf { it in 0..2 },
        optionSlot2Selection = state.optionSlots[2].takeIf { it in 0..2 },
        optionSlot3Selection = state.optionSlots[3].takeIf { it in 0..1 },
        checkboxT = state.checkboxT,
        controlsEnabled = editableQ0,
    )

    /** `j()`: Q0=false is a complete no-op; otherwise only checked known options write. */
    fun commit(
        state: LegacyTacticsRawState,
        ui: LegacyTacticsDialogUiState,
        editableQ0: Boolean,
    ): LegacyTacticsRawState {
        if (!editableQ0) return state
        val next = state.optionSlots.toMutableList()
        ui.optionSlot1Selection?.takeIf { it in 0..2 }?.let { next[1] = it }
        ui.optionSlot2Selection?.takeIf { it in 0..2 }?.let { next[2] = it }
        ui.optionSlot3Selection?.takeIf { it in 0..1 }?.let { next[3] = it }
        return LegacyTacticsRawState(next, ui.checkboxT)
    }

    /** `k(String,best.o)`: exact special-player keys only. */
    fun <T> assignSpecialPlayer(
        actionKey: String,
        current: LegacySpecialTacticsAssignments<T>,
        player: T,
    ): LegacySpecialTacticsAssignments<T> = when (actionKey) {
        LegacySpecialTacticsRole.CAPTAIN.actionKey -> current.copy(captain = player)
        LegacySpecialTacticsRole.FREE_KICK.actionKey -> current.copy(freeKick = player)
        LegacySpecialTacticsRole.CORNER.actionKey -> current.copy(corner = player)
        LegacySpecialTacticsRole.FALSE_NINE.actionKey -> current.copy(falseNine = player)
        else -> current
    }

    /** Exact cleanup shared by best.c0 F0/G0/H0/I0. */
    fun <T> sanitizeReference(
        current: T?,
        belongsToClub: Boolean,
        t0FallbackActive: Boolean,
        rosterContainsCurrent: Boolean,
    ): T? {
        if (current == null) return null
        if (belongsToClub) return current
        if (!t0FallbackActive) return null
        return current.takeIf { rosterContainsCurrent }
    }

    /** `v2.b()`: reference identity, then index 0 fallback only for non-empty candidates. */
    fun <T> initialPickerSelection(candidates: List<T>, current: T?): Int {
        val exactIndex = candidates.indexOfFirst { it === current }
        return when {
            exactIndex >= 0 -> exactIndex
            candidates.isNotEmpty() -> 0
            else -> -1
        }
    }

    /** `v2.c()`: invalid index is a no-op; valid index returns exactly that object. */
    fun <T> confirmPickerSelection(candidates: List<T>, selectedIndex: Int): T? =
        if (selectedIndex < 0 || selectedIndex >= candidates.size) null else candidates[selectedIndex]
}

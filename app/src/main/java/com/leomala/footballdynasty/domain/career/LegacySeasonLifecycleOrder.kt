package com.leomala.footballdynasty.domain.career

/**
 * Structural characterization of legacy best.b.d() from the Brasfoot 2026/27 baseline.
 *
 * Names intentionally preserve legacy method labels when the sporting meaning is not
 * proven. This models orchestration only; it does not pretend that opaque side effects
 * have already been reconstructed in the modern domain.
 */
enum class LegacySeasonLifecycleStage {
    G1_L1_MAINTENANCE,
    OPTIONAL_W2,
    CALENDAR_REBUILD_L,
    S_MAINTENANCE,
    OPTIONAL_N,
    D_MAINTENANCE,
    R_MAINTENANCE,
    O_MAINTENANCE,
    OPTIONAL_BEST_A_L,
    OPTIONAL_A1,
    BEST_A_M,
    Y3,
    Q_MAINTENANCE,
    A0_D,
    A0_A,
    OPTIONAL_D1,
    P0_SELECT_NEXT_PLAYABLE_DAY,
    CLEAR_M0,
    ROUTER_BEST_N_N,
}

data class LegacySeasonLifecycleFlags(
    val f0: Boolean,
    val v0: Boolean,
    val y1Enabled: Boolean,
    val bestASelectionAvailable: Boolean,
)

object LegacySeasonLifecycleOrder {
    /**
     * Exact branch/order projection of best.b.d() as confirmed by Java + SMALI.
     * The y1Enabled name corresponds to legacy Y1(), not best.b.y1().
     */
    fun stages(flags: LegacySeasonLifecycleFlags): List<LegacySeasonLifecycleStage> = buildList {
        add(LegacySeasonLifecycleStage.G1_L1_MAINTENANCE)
        if (flags.f0) add(LegacySeasonLifecycleStage.OPTIONAL_W2)
        if (!flags.v0) add(LegacySeasonLifecycleStage.CALENDAR_REBUILD_L)
        add(LegacySeasonLifecycleStage.S_MAINTENANCE)
        if (flags.f0) add(LegacySeasonLifecycleStage.OPTIONAL_N)
        add(LegacySeasonLifecycleStage.D_MAINTENANCE)
        add(LegacySeasonLifecycleStage.R_MAINTENANCE)
        add(LegacySeasonLifecycleStage.O_MAINTENANCE)
        if (flags.y1Enabled && flags.bestASelectionAvailable) {
            add(LegacySeasonLifecycleStage.OPTIONAL_BEST_A_L)
        }
        if (flags.y1Enabled) add(LegacySeasonLifecycleStage.OPTIONAL_A1)
        add(LegacySeasonLifecycleStage.BEST_A_M)
        add(LegacySeasonLifecycleStage.Y3)
        add(LegacySeasonLifecycleStage.Q_MAINTENANCE)
        add(LegacySeasonLifecycleStage.A0_D)
        add(LegacySeasonLifecycleStage.A0_A)
        if (flags.v0) add(LegacySeasonLifecycleStage.OPTIONAL_D1)
        add(LegacySeasonLifecycleStage.P0_SELECT_NEXT_PLAYABLE_DAY)
        add(LegacySeasonLifecycleStage.CLEAR_M0)
        add(LegacySeasonLifecycleStage.ROUTER_BEST_N_N)
    }
}

package com.leomala.footballdynasty.domain.career

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacySeasonLifecycleOrderTest {
    @Test
    fun `standard path preserves proven best b d order`() {
        val stages = LegacySeasonLifecycleOrder.stages(
            LegacySeasonLifecycleFlags(
                f0 = false,
                v0 = false,
                y1Enabled = false,
                bestASelectionAvailable = false,
            ),
        )

        assertEquals(
            listOf(
                LegacySeasonLifecycleStage.G1_L1_MAINTENANCE,
                LegacySeasonLifecycleStage.CALENDAR_REBUILD_L,
                LegacySeasonLifecycleStage.S_MAINTENANCE,
                LegacySeasonLifecycleStage.D_MAINTENANCE,
                LegacySeasonLifecycleStage.R_MAINTENANCE,
                LegacySeasonLifecycleStage.O_MAINTENANCE,
                LegacySeasonLifecycleStage.BEST_A_M,
                LegacySeasonLifecycleStage.Y3,
                LegacySeasonLifecycleStage.Q_MAINTENANCE,
                LegacySeasonLifecycleStage.A0_D,
                LegacySeasonLifecycleStage.A0_A,
                LegacySeasonLifecycleStage.P0_SELECT_NEXT_PLAYABLE_DAY,
                LegacySeasonLifecycleStage.CLEAR_M0,
                LegacySeasonLifecycleStage.ROUTER_BEST_N_N,
            ),
            stages,
        )
    }

    @Test
    fun `f0 enables w2 before calendar path and n after s`() {
        val stages = LegacySeasonLifecycleOrder.stages(
            LegacySeasonLifecycleFlags(
                f0 = true,
                v0 = false,
                y1Enabled = false,
                bestASelectionAvailable = false,
            ),
        )

        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_W2) < stages.indexOf(LegacySeasonLifecycleStage.CALENDAR_REBUILD_L))
        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.S_MAINTENANCE) < stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_N))
    }

    @Test
    fun `v0 suppresses calendar rebuild and enables d1 near end`() {
        val stages = LegacySeasonLifecycleOrder.stages(
            LegacySeasonLifecycleFlags(
                f0 = false,
                v0 = true,
                y1Enabled = false,
                bestASelectionAvailable = false,
            ),
        )

        assertFalse(stages.contains(LegacySeasonLifecycleStage.CALENDAR_REBUILD_L))
        assertTrue(stages.contains(LegacySeasonLifecycleStage.OPTIONAL_D1))
        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.A0_A) < stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_D1))
        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_D1) < stages.indexOf(LegacySeasonLifecycleStage.P0_SELECT_NEXT_PLAYABLE_DAY))
    }

    @Test
    fun `y1 gate and positive best a selection enable both conditional branches`() {
        val stages = LegacySeasonLifecycleOrder.stages(
            LegacySeasonLifecycleFlags(
                f0 = false,
                v0 = false,
                y1Enabled = true,
                bestASelectionAvailable = true,
            ),
        )

        assertTrue(stages.contains(LegacySeasonLifecycleStage.OPTIONAL_BEST_A_L))
        assertTrue(stages.contains(LegacySeasonLifecycleStage.OPTIONAL_A1))
        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.O_MAINTENANCE) < stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_BEST_A_L))
        assertTrue(stages.indexOf(LegacySeasonLifecycleStage.OPTIONAL_A1) < stages.indexOf(LegacySeasonLifecycleStage.BEST_A_M))
    }
}

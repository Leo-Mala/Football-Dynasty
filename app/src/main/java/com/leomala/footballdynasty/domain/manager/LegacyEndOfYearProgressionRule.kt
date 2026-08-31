package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

enum class LegacyDismissalGateEffect {
    RESET_DISMISSAL_ROWS,
    LOAD_DISMISSAL_ROWS,
    OPEN_DISMISSALS,
    CONTINUE_END_OF_YEAR,
}

data class LegacyDismissalGateResult<R>(
    val dismissalRows: List<R>?,
    val effectsInOrder: List<LegacyDismissalGateEffect>,
)

/**
 * Exact persistence-independent orchestration of official Phase 4R `best.n.l()`.
 *
 * The rule intentionally preserves the legacy double manager lookup for a non-null dismissal row,
 * scans the whole list even after finding a human manager, and reads `V0/u1()` only when the list
 * is non-empty. `DialogDemissoes` itself remains a separate surface; this rule only decides whether
 * that surface is opened or whether execution falls through to `best.n.m()`.
 */
object LegacyDismissalGateRule {
    fun <R, M> execute(
        currentCompetitionKind: () -> Int,
        dismissalRowsForKind: (Int) -> List<R>?,
        managerOfRow: (R) -> M?,
        isHumanManager: (M) -> Boolean,
        legacyV0: () -> Boolean,
        legacyU1: () -> Int,
        openDismissals: () -> Unit,
        continueEndOfYear: () -> Unit,
    ): LegacyDismissalGateResult<R> {
        val effects = mutableListOf<LegacyDismissalGateEffect>()
        effects += LegacyDismissalGateEffect.RESET_DISMISSAL_ROWS
        var rows: List<R>? = null

        val kind = currentCompetitionKind()
        if (kind == 1 || kind == 3) {
            effects += LegacyDismissalGateEffect.LOAD_DISMISSAL_ROWS
            rows = dismissalRowsForKind(kind)
        }

        var shouldOpen = false
        if (rows != null && rows.isNotEmpty()) {
            var hasHumanManager = false
            for (row in rows) {
                val firstLookup = managerOfRow(row)
                if (firstLookup != null) {
                    // SMALI invokes row.b() again before K(). Preserve that second lookup/null edge.
                    val secondLookup = managerOfRow(row)!!
                    if (isHumanManager(secondLookup)) {
                        hasHumanManager = true
                    }
                }
            }

            var specialLegacyGate = false
            if (legacyV0() && legacyU1() == 1) {
                specialLegacyGate = true
            }
            shouldOpen = hasHumanManager || specialLegacyGate
        }

        if (shouldOpen && rows != null && rows.isNotEmpty()) {
            effects += LegacyDismissalGateEffect.OPEN_DISMISSALS
            openDismissals()
        } else {
            effects += LegacyDismissalGateEffect.CONTINUE_END_OF_YEAR
            continueEndOfYear()
        }

        return LegacyDismissalGateResult(
            dismissalRows = rows,
            effectsInOrder = effects,
        )
    }
}

enum class LegacyEndOfYearProgressionEffect {
    CAPTURE_INITIAL_P0,
    TRY_D4,
    DRAW_G4_GATE,
    RUN_G4,
    TRY_E4,
    SET_J2_ONE,
    SET_F2_TRUE,
    FINALIZE_F,
    CONTINUE_I,
    OPEN_END_YEAR,
}

data class LegacyEndOfYearProgressionResult(
    val randomDraw: Int,
    val effectsInOrder: List<LegacyEndOfYearProgressionEffect>,
)

/**
 * Exact host ordering of official Phase 4R `best.n.m()`.
 *
 * Legacy creates a fresh `java.util.Random` and performs exactly one `nextInt(100)` draw. The
 * modern boundary injects [RandomSource] so the draw is observable/testable while preserving the
 * exact bound and `> 50` condition; it does not claim the legacy wall-clock seed is reproducible.
 * Only `d4()` and `e4()` are protected by `catch (Exception)` in the original method.
 */
object LegacyEndOfYearProgressionRule {
    fun execute(
        currentP0: () -> Int,
        o0: () -> List<*>?,
        d4: () -> Unit,
        random: RandomSource,
        g4: () -> Unit,
        g0: () -> List<*>?,
        e4: () -> Unit,
        j2: (Int) -> Unit,
        f2: (Boolean) -> Unit,
        legacyV0: () -> Boolean,
        e1: () -> Boolean,
        finalizeF: () -> Unit,
        continueI: () -> Unit,
        openEndYear: () -> Unit,
    ): LegacyEndOfYearProgressionResult {
        val effects = mutableListOf<LegacyEndOfYearProgressionEffect>()

        effects += LegacyEndOfYearProgressionEffect.CAPTURE_INITIAL_P0
        val initialP0 = currentP0()

        val firstO0 = o0()
        if (firstO0 != null && o0()!!.isNotEmpty()) {
            effects += LegacyEndOfYearProgressionEffect.TRY_D4
            try {
                d4()
            } catch (_: Exception) {
                // Official method swallows Exception from d4 only.
            }
        }

        effects += LegacyEndOfYearProgressionEffect.DRAW_G4_GATE
        val draw = random.nextInt(100)
        if (draw > 50) {
            effects += LegacyEndOfYearProgressionEffect.RUN_G4
            g4()
        }

        val firstG0 = g0()
        if (firstG0 != null && g0()!!.isNotEmpty()) {
            effects += LegacyEndOfYearProgressionEffect.TRY_E4
            try {
                e4()
            } catch (_: Exception) {
                // Official method swallows Exception from e4 only.
            }
        }

        effects += LegacyEndOfYearProgressionEffect.SET_J2_ONE
        j2(1)

        if (initialP0 == 0) {
            effects += LegacyEndOfYearProgressionEffect.SET_F2_TRUE
            f2(true)
        }

        val v0 = legacyV0()
        val ended = e1()
        if (!v0) {
            if (ended) {
                effects += LegacyEndOfYearProgressionEffect.FINALIZE_F
                finalizeF()
            }
        } else if (!ended) {
            effects += LegacyEndOfYearProgressionEffect.CONTINUE_I
            continueI()
            return LegacyEndOfYearProgressionResult(draw, effects)
        } else {
            effects += LegacyEndOfYearProgressionEffect.FINALIZE_F
            finalizeF()
            effects += LegacyEndOfYearProgressionEffect.OPEN_END_YEAR
            openEndYear()
        }

        return LegacyEndOfYearProgressionResult(draw, effects)
    }
}

package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.foundation.random.RandomSource

enum class LegacyDismissalDispatchEffect {
    RESET_DISMISSALS,
    LOAD_DISMISSALS,
    OPEN_DISMISSALS,
    DISPATCH_POST_DISMISSAL,
}

data class LegacyDismissalDispatchResult<E>(
    val dismissals: List<E>?,
    val openedDismissals: Boolean,
    val effectsInOrder: List<LegacyDismissalDispatchEffect>,
)

/** Exact persistence-independent orchestration of official Phase 4R `best.n.l()`. */
object LegacyDismissalDispatchRule {
    fun <E, M> execute(
        currentCompetitionKind: () -> Int,
        loadDismissals: (competitionKind: Int) -> List<E>?,
        managerOf: (E) -> M?,
        isHumanManager: (M) -> Boolean,
        worldV0: () -> Boolean,
        worldU1: () -> Int,
        resetDismissals: () -> Unit,
        openDismissals: (List<E>) -> Unit,
        dispatchPostDismissal: () -> Unit,
    ): LegacyDismissalDispatchResult<E> {
        val effects = mutableListOf<LegacyDismissalDispatchEffect>()
        effects += LegacyDismissalDispatchEffect.RESET_DISMISSALS
        resetDismissals()

        val kind = currentCompetitionKind()
        var dismissals: List<E>? = null
        if (kind == 1 || kind == 3) {
            effects += LegacyDismissalDispatchEffect.LOAD_DISMISSALS
            dismissals = loadDismissals(kind)
        }

        var hasHumanDismissal = false
        var specialLegacyGate = false
        if (dismissals != null && dismissals.isNotEmpty()) {
            for (entry in dismissals) {
                // Legacy SMALI resolves the manager once for null-check and again before K().
                if (managerOf(entry) != null) {
                    val manager = managerOf(entry)!!
                    if (isHumanManager(manager)) hasHumanDismissal = true
                }
            }
            if (worldV0() && worldU1() == 1) {
                specialLegacyGate = true
            }
        }

        val shouldOpen =
            dismissals != null &&
                dismissals.isNotEmpty() &&
                (hasHumanDismissal || specialLegacyGate)
        if (shouldOpen) {
            effects += LegacyDismissalDispatchEffect.OPEN_DISMISSALS
            openDismissals(dismissals!!)
        } else {
            effects += LegacyDismissalDispatchEffect.DISPATCH_POST_DISMISSAL
            dispatchPostDismissal()
        }
        return LegacyDismissalDispatchResult(dismissals, shouldOpen, effects)
    }
}

enum class LegacyPostDismissalContinuationEffect {
    CAPTURE_P0,
    ATTEMPT_D4,
    SWALLOW_D4_EXCEPTION,
    DRAW_G4_GATE,
    RUN_G4,
    ATTEMPT_E4,
    SWALLOW_E4_EXCEPTION,
    SET_J2_ONE,
    SET_F2_TRUE,
    READ_V0,
    READ_E1,
    RUN_WORLD_F,
    DISPATCH_CONTINUATION_I,
    OPEN_END_YEAR,
}

data class LegacyPostDismissalContinuationResult(
    val capturedP0: Int,
    val g4GateDraw: Int,
    val effectsInOrder: List<LegacyPostDismissalContinuationEffect>,
)

/**
 * Exact orchestration of official Phase 4R `best.n.m()` with the legacy implicit `Random` routed
 * through the career [RandomSource]. This preserves draw position/bound/threshold without claiming
 * wall-clock seed equivalence with the old fresh `java.util.Random` instance.
 */
object LegacyPostDismissalContinuationRule {
    fun execute(
        random: RandomSource,
        p0: () -> Int,
        o0: () -> List<*>?,
        runD4: () -> Unit,
        runG4: () -> Unit,
        g0: () -> List<*>?,
        runE4: () -> Unit,
        setJ2: (Int) -> Unit,
        setF2: (Boolean) -> Unit,
        worldV0: () -> Boolean,
        worldE1: () -> Boolean,
        runWorldF: () -> Unit,
        dispatchContinuationI: () -> Unit,
        openEndYear: () -> Unit,
    ): LegacyPostDismissalContinuationResult {
        val effects = mutableListOf<LegacyPostDismissalContinuationEffect>()

        effects += LegacyPostDismissalContinuationEffect.CAPTURE_P0
        val capturedP0 = p0()

        if (o0() != null && o0()!!.isNotEmpty()) {
            effects += LegacyPostDismissalContinuationEffect.ATTEMPT_D4
            try {
                runD4()
            } catch (_: Exception) {
                effects += LegacyPostDismissalContinuationEffect.SWALLOW_D4_EXCEPTION
            }
        }

        effects += LegacyPostDismissalContinuationEffect.DRAW_G4_GATE
        val draw = random.nextInt(100)
        if (draw > 50) {
            effects += LegacyPostDismissalContinuationEffect.RUN_G4
            runG4()
        }

        if (g0() != null && g0()!!.isNotEmpty()) {
            effects += LegacyPostDismissalContinuationEffect.ATTEMPT_E4
            try {
                runE4()
            } catch (_: Exception) {
                effects += LegacyPostDismissalContinuationEffect.SWALLOW_E4_EXCEPTION
            }
        }

        effects += LegacyPostDismissalContinuationEffect.SET_J2_ONE
        setJ2(1)
        if (capturedP0 == 0) {
            effects += LegacyPostDismissalContinuationEffect.SET_F2_TRUE
            setF2(true)
        }

        effects += LegacyPostDismissalContinuationEffect.READ_V0
        val v0 = worldV0()
        effects += LegacyPostDismissalContinuationEffect.READ_E1
        val e1 = worldE1()
        if (v0) {
            if (e1) {
                effects += LegacyPostDismissalContinuationEffect.RUN_WORLD_F
                runWorldF()
                effects += LegacyPostDismissalContinuationEffect.OPEN_END_YEAR
                openEndYear()
            } else {
                effects += LegacyPostDismissalContinuationEffect.DISPATCH_CONTINUATION_I
                dispatchContinuationI()
            }
        } else if (e1) {
            effects += LegacyPostDismissalContinuationEffect.RUN_WORLD_F
            runWorldF()
        }

        return LegacyPostDismissalContinuationResult(
            capturedP0 = capturedP0,
            g4GateDraw = draw,
            effectsInOrder = effects,
        )
    }
}

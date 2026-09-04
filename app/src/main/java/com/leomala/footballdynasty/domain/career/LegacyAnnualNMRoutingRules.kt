package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure control-flow projection of reachable legacy `best.n.m()`.
 *
 * The legacy method surrounds only `best.b.d4()` and `best.b.e4()` with exception-swallowing
 * boundaries. This rule deliberately does not model their mutations; it freezes which calls are
 * attempted, the one unconditional `nextInt(100)` gate between them, and the exact final route.
 * Substantive callees remain independently characterized and persisted by their own boundaries.
 */
object LegacyAnnualNMRoutingRules {
    enum class Action {
        TRY_D4,
        CALL_G4,
        TRY_E4,
        CALL_J2_ONE,
        SET_F2_TRUE,
        CALL_F,
        START_ACTIVITY_FIM_ANO,
        CALL_N_I,
    }

    data class Input(
        val originalP0: Int,
        val hasO0Entries: Boolean,
        val hasG0Entries: Boolean,
        val legacyV0: Boolean,
        val annualRouteE1: Boolean,
    )

    /**
     * Mirrors the SMALI order exactly:
     * optional d4 -> unconditional nextInt(100) -> optional g4 -> optional e4 -> j2(1) ->
     * optional F2(true) -> V0/E1 final routing.
     *
     * `CALL_G4` uses the exact legacy predicate `nextInt(100) > 50`. The draw therefore occurs
     * even when both maintenance queues are absent and irrespective of the final V0/E1 route.
     */
    fun plan(
        input: Input,
        random: RandomSource,
    ): List<Action> = buildList {
        if (input.hasO0Entries) {
            add(Action.TRY_D4)
        }

        if (random.nextInt(100) > 50) {
            add(Action.CALL_G4)
        }

        if (input.hasG0Entries) {
            add(Action.TRY_E4)
        }

        add(Action.CALL_J2_ONE)

        if (input.originalP0 == 0) {
            add(Action.SET_F2_TRUE)
        }

        if (input.legacyV0) {
            if (input.annualRouteE1) {
                add(Action.CALL_F)
                add(Action.START_ACTIVITY_FIM_ANO)
            } else {
                add(Action.CALL_N_I)
            }
        } else if (input.annualRouteE1) {
            add(Action.CALL_F)
        }
    }
}

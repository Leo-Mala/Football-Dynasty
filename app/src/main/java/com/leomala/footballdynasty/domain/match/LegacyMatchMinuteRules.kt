package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Deterministic structural boundary for one legacy `best.s.k(match, half, minute)` tick.
 *
 * The event labels intentionally retain neutral legacy names until their complete sporting
 * semantics and side effects are characterized from Java + SMALI. This rule only owns the RNG
 * routing/bounds proven before the downstream player-selection/event-materialization helpers.
 */
object LegacyMatchMinuteRules {
    enum class Side {
        HOME,
        AWAY,
    }

    enum class Action {
        LEGACY_C,
        LEGACY_D,
        LEGACY_TYPE_5,
        SECOND_HALF_J,
        NONE,
    }

    data class Decision(
        val side: Side,
        val action: Action,
        val refreshPlayerState: Boolean,
        val primaryBound: Int,
        val secondaryBound: Int,
        val tertiaryBound: Int,
    )

    private val firstHalfPrimary = intArrayOf(70, 40, 30)
    private val secondHalfPrimary = intArrayOf(45, 40, 30)
    private val firstHalfSecondary = intArrayOf(1200, 900, 800)
    private val secondHalfSecondary = intArrayOf(800, 700, 550)
    private val tertiary = intArrayOf(2000, 1500, 1100)
    private val tacticOffset = intArrayOf(30, 10, 0)

    fun decide(
        random: RandomSource,
        half: Int,
        minute: Int,
        homeTacticIndex: Int,
        awayTacticIndex: Int,
        primaryCounter: Int,
        secondaryCounter: Int,
        tertiaryCounter: Int,
    ): Decision {
        require(half == 1 || half == 2) { "Legacy match half must be 1 or 2: $half" }
        require(minute >= 0) { "Legacy match minute must be non-negative: $minute" }

        val side = if (random.nextInt(100) > 55) Side.HOME else Side.AWAY
        val rawTacticIndex = if (side == Side.HOME) homeTacticIndex else awayTacticIndex
        require(rawTacticIndex >= 0) { "Legacy tactic index must be non-negative: $rawTacticIndex" }
        val tacticIndex = if (rawTacticIndex >= 3) 0 else rawTacticIndex
        val segment = when {
            minute < 15 -> 0
            minute < 30 -> 1
            else -> 2
        }

        var primaryBound =
            (if (half == 1) firstHalfPrimary[segment] else secondHalfPrimary[segment]) +
                tacticOffset[tacticIndex]
        val secondaryBound =
            if (half == 1) firstHalfSecondary[segment] else secondHalfSecondary[segment]
        val tertiaryBound = tertiary[segment]

        // Preserve the exact legacy branch order. The `> 10` branch is unreachable because it is
        // an `else if` after `> 5`; changing this would change the legacy probability surface.
        if (primaryCounter > 5) {
            primaryBound *= 2
        } else if (primaryCounter > 10) {
            primaryBound = 1000
        }
        if (secondaryCounter >= 2) {
            primaryBound = secondaryBound * 2
        }
        if (tertiaryCounter >= 1) {
            primaryBound = tertiaryBound * 5
        }

        val refresh = minute % 7 == 0
        if (random.nextInt(primaryBound) == 1) {
            return Decision(side, Action.LEGACY_C, refresh, primaryBound, secondaryBound, tertiaryBound)
        }
        if (random.nextInt(secondaryBound) == 1) {
            return Decision(side, Action.LEGACY_D, refresh, primaryBound, secondaryBound, tertiaryBound)
        }
        if (random.nextInt(tertiaryBound) == 1) {
            return Decision(side, Action.LEGACY_TYPE_5, refresh, primaryBound, secondaryBound, tertiaryBound)
        }

        val action = if (half == 2 && minute >= 5) Action.SECOND_HALF_J else Action.NONE
        return Decision(side, action, refresh, primaryBound, secondaryBound, tertiaryBound)
    }
}

package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for legacy `best.s.r(...)` and `best.s.r0(...)`. */
object LegacyMatchTransitionRules {
    enum class LegacyClubSide {
        LEGACY_E,
        LEGACY_F,
        OTHER,
    }

    data class Player<T>(
        val value: T,
        val legacyPositionIndex: Int,
        val legacyG0: Int,
        val legacyN: Int,
        val legacyL0: Int,
        val clubSide: LegacyClubSide = LegacyClubSide.OTHER,
    )

    enum class FollowUpAction {
        LEGACY_C,
        LEGACY_D,
    }

    data class FollowUp<T>(
        val sideFlag: Int,
        val action: FollowUpAction,
        val player: Player<T>,
        val legacyP2: Int,
        val legacyP3: Int,
    )

    data class R0Result<T>(
        val code: Int,
        val followUp: FollowUp<T>?,
    )

    fun <T> selectR(
        mode: Int,
        legacyP2: Int,
        legacyP4: Int,
        candidates: List<Player<T>>,
        legacyG: Set<T>,
        legacyH: Set<T>,
        random: RandomSource,
    ): Player<T>? = when (mode) {
        1 -> selectRMode1(legacyP4, candidates, random)
        2 -> selectRMode2(legacyP2, candidates, legacyG, legacyH, random)
        else -> null
    }

    private fun <T> selectRMode1(
        legacyP4: Int,
        candidates: List<Player<T>>,
        random: RandomSource,
    ): Player<T>? {
        val threshold = if (legacyP4 > 40) 90 else 60
        val start = if (legacyP4 > 40 && candidates.isNotEmpty()) {
            random.nextInt(candidates.size)
        } else {
            0
        }
        for (index in start until candidates.size) {
            val candidate = candidates[index]
            if (candidate.legacyG0 != 1 && candidate.legacyN < threshold) {
                return candidate
            }
        }
        return null
    }

    private fun <T> selectRMode2(
        legacyP2: Int,
        candidates: List<Player<T>>,
        legacyG: Set<T>,
        legacyH: Set<T>,
        random: RandomSource,
    ): Player<T>? {
        if (candidates.isEmpty()) return null
        val used = if (legacyP2 == 2) legacyH else legacyG
        var index = random.nextInt(candidates.size)
        if (candidates[index].value in used) {
            // Exact legacy behavior: a single reroll, with no loop/retry guarantee.
            index = random.nextInt(candidates.size)
        }
        val candidate = candidates[index]
        return candidate.takeIf { it.legacyG0 != 1 && it.value !in used }
    }

    fun <T> resolveR0(
        player: Player<T>,
        legacyP2: Int,
        legacyP3: Int,
        legacyEPlayers: List<Player<T>>,
        legacyFPlayers: List<Player<T>>,
        random: RandomSource,
    ): R0Result<T> {
        val draw = random.nextInt(1000)
        var code = when (legacyP2) {
            1 -> when {
                draw < 900 -> 1
                draw < 950 -> 3
                draw < 980 -> 4
                draw < 990 -> 2
                draw < 995 -> 5
                else -> 1
            }
            2 -> when {
                draw < 800 -> 1
                draw < 850 -> 3
                draw < 980 -> 4
                draw < 990 -> 2
                draw < 995 -> 5
                else -> 1
            }
            else -> 1
        }

        if (code == 5 && (player.legacyL0 == 0 || player.legacyL0 == 2)) {
            code = 1
        }

        if (code != 3) return R0Result(code, null)

        val (sideFlag, pool) = when (player.clubSide) {
            LegacyClubSide.LEGACY_E -> 1 to legacyEPlayers
            LegacyClubSide.LEGACY_F -> 0 to legacyFPlayers
            LegacyClubSide.OTHER -> return R0Result(code, null)
        }

        val followDraw = random.nextInt(100)
        val selectorCandidates = pool.map {
            LegacyMatchPlayerSelectionRules.Candidate(it, it.legacyPositionIndex)
        }
        val followUp = when {
            followDraw < 50 -> LegacyMatchPlayerSelectionRules
                .selectS(selectorCandidates, random)
                ?.value
                ?.let { FollowUp(sideFlag, FollowUpAction.LEGACY_C, it, legacyP2, legacyP3) }
            followDraw < 60 -> LegacyMatchPlayerSelectionRules
                .selectU(selectorCandidates, random)
                ?.value
                ?.let { FollowUp(sideFlag, FollowUpAction.LEGACY_D, it, legacyP2, legacyP3) }
            else -> null
        }
        return R0Result(code, followUp)
    }
}

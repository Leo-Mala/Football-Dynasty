package com.leomala.footballdynasty.domain.match

import com.leomala.footballdynasty.foundation.random.RandomSource

/** Structural parity for reachable legacy substitution flow `best.s.p1(...) -> best.s.o1(...)`. */
object LegacyMatchSubstitutionRules {
    data class Player<T>(
        val value: T,
        val legacyG0: Int,
        val legacyL0: Int,
        val legacyF0: Int,
        val legacyR: Int,
    )

    data class SelectionPlan<T>(
        val outgoing: Player<T>,
        val incoming: Player<T>,
        val finalIncomingPositionIndex: Int,
        val eventType: LegacyMatchEventType = LegacyMatchEventType.SUBSTITUTION,
        val ignoredLegacyTrailingArgument: Int,
    )

    enum class MutationOperation {
        DECREMENT_SUBSTITUTION_COUNT,
        SET_INCOMING_TO_OUTGOING_POSITION,
        OVERRIDE_INCOMING_POSITION_WHEN_POSITIVE,
        REMOVE_OUTGOING_FROM_ACTIVE,
        ADD_INCOMING_TO_ACTIVE,
        ADD_INCOMING_TO_USED,
        MARK_INCOMING_SELECTED,
        REMOVE_INCOMING_FROM_BENCH,
        EMIT_SUBSTITUTION_EVENT,
    }

    /** Exact mutation order inside `best.s.o1(...)` for a valid side/list state. */
    val mutationOrder: List<MutationOperation> = listOf(
        MutationOperation.DECREMENT_SUBSTITUTION_COUNT,
        MutationOperation.SET_INCOMING_TO_OUTGOING_POSITION,
        MutationOperation.OVERRIDE_INCOMING_POSITION_WHEN_POSITIVE,
        MutationOperation.REMOVE_OUTGOING_FROM_ACTIVE,
        MutationOperation.ADD_INCOMING_TO_ACTIVE,
        MutationOperation.ADD_INCOMING_TO_USED,
        MutationOperation.MARK_INCOMING_SELECTED,
        MutationOperation.REMOVE_INCOMING_FROM_BENCH,
        MutationOperation.EMIT_SUBSTITUTION_EVENT,
    )

    fun <T> resolve(
        original: Player<T>,
        active: List<Player<T>>,
        bench: List<Player<T>>,
        automaticOutgoing: Boolean,
        enforceLegacyL0Compatibility: Boolean,
        random: RandomSource,
    ): SelectionPlan<T>? {
        val outgoing = if (automaticOutgoing) {
            selectW(active, 18, 25, random)
                ?: selectW(active, 14, 17, random)
                ?: if (original.legacyG0 == 1) selectW(active, 2, 25, random) else null
        } else {
            original
        } ?: return null

        if (original.legacyG0 < 0) return null

        val incoming = selectBenchCandidate(
            candidates = bench,
            originalPositionIndex = original.legacyG0,
            requireLegacyL0 = original.legacyL0 != 0,
        ) ?: return null

        if (enforceLegacyL0Compatibility && outgoing.legacyL0 != 0 && incoming.legacyL0 == 0) {
            return null
        }

        // o1 first assigns outgoing.g0 and then overwrites it only when the original g0 is > 0.
        val finalPosition = if (original.legacyG0 > 0) original.legacyG0 else outgoing.legacyG0

        return SelectionPlan(
            outgoing = outgoing,
            incoming = incoming,
            finalIncomingPositionIndex = finalPosition,
            // p1 passes 5 in this branch and -1 otherwise, but o1 never reads its final argument.
            ignoredLegacyTrailingArgument = if (enforceLegacyL0Compatibility) 5 else -1,
        )
    }

    /** Deterministic replacement for legacy `best.s.W`: filter, shuffle, return first or null. */
    private fun <T> selectW(
        candidates: List<Player<T>>,
        minInclusive: Int,
        maxInclusive: Int,
        random: RandomSource,
    ): Player<T>? {
        val wrapped = candidates.map {
            LegacyMatchPlayerSelectionRules.Candidate(
                value = it,
                legacyPositionIndex = it.legacyG0,
            )
        }
        return LegacyMatchPlayerSelectionRules.selectWithinRange(
            candidates = wrapped,
            range = LegacyMatchPlayerSelectionRules.PositionRange(minInclusive, maxInclusive),
            random = random,
        )?.value
    }

    /**
     * Exact ordered scan from `components.y3.e(ArrayList, int, boolean, boolean)` used by `p1`.
     * The third legacy boolean parameter passed by p1 is always false and is not read by the bytecode;
     * only the final `requireLegacyL0` flag affects this scan.
     */
    fun <T> selectBenchCandidate(
        candidates: List<Player<T>>,
        originalPositionIndex: Int,
        requireLegacyL0: Boolean,
    ): Player<T>? {
        val mapping = LEGACY_Z1[originalPositionIndex]
        val baseGroup = mapping[0]
        var legacyF0 = mapping[1]
        var legacyR = mapping[2]
        val f0WasWildcard = legacyF0 == -1
        val rWasWildcard = legacyR == -1
        if (originalPositionIndex >= 18) {
            legacyR = -1
        }

        for (priority in 0..4) {
            var legacyL0 = LEGACY_C2[baseGroup][priority]
            for (mode in 1..4) {
                for (candidate in candidates) {
                    if (f0WasWildcard) {
                        legacyF0 = candidate.legacyF0
                    }
                    if (rWasWildcard) {
                        legacyR = candidate.legacyR
                    }
                    if (mode == 2) {
                        legacyF0 = candidate.legacyF0
                    }
                    if (mode == 3) {
                        legacyF0 = candidate.legacyF0
                        legacyR = candidate.legacyR
                    }
                    if (mode == 4) {
                        if (!requireLegacyL0) {
                            legacyL0 = candidate.legacyL0
                        } else if (candidate.legacyL0 != 0) {
                            legacyL0 = candidate.legacyL0
                        }
                    }

                    if (
                        candidate.legacyL0 == legacyL0 &&
                        candidate.legacyF0 == legacyF0 &&
                        candidate.legacyR == legacyR
                    ) {
                        return candidate
                    }
                }
            }
        }
        return null
    }

    private val LEGACY_Z1 = arrayOf(
        intArrayOf(0, -1, -1), intArrayOf(0, -1, -1), intArrayOf(1, 1, -1),
        intArrayOf(2, 1, -1), intArrayOf(2, -1, -1), intArrayOf(2, 0, -1),
        intArrayOf(2, 1, -1), intArrayOf(2, -1, -1), intArrayOf(2, 0, -1),
        intArrayOf(1, 0, -1), intArrayOf(1, 1, 1), intArrayOf(3, 1, 0),
        intArrayOf(3, -1, 0), intArrayOf(3, 0, 0), intArrayOf(3, 1, 1),
        intArrayOf(3, -1, 1), intArrayOf(3, 0, 1), intArrayOf(1, 0, 0),
        intArrayOf(4, 1, -1), intArrayOf(4, 1, -1), intArrayOf(4, -1, -1),
        intArrayOf(4, 0, -1), intArrayOf(4, 1, -1), intArrayOf(4, -1, -1),
        intArrayOf(4, 0, -1), intArrayOf(4, 0, -1),
    )

    private val LEGACY_C2 = arrayOf(
        intArrayOf(0, 2, 1, 3, 4),
        intArrayOf(1, 3, 2, 4, 0),
        intArrayOf(2, 1, 3, 4, 0),
        intArrayOf(3, 1, 2, 4, 0),
        intArrayOf(4, 3, 1, 2, 0),
    )
}

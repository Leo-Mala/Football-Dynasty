package com.leomala.footballdynasty.domain.career

/** Pure mutation plan for reachable annual `best.a.p()` (`cD`) from official SMALI. */
object LegacyAnnualPCompetitionRules {
    data class Round<ClubId>(
        val legacyS0: Int,
        val participants: List<ClubId>,
    )

    enum class LastRoundAction {
        SET_A1_FALSE,
        ADD_F0_PARTICIPANTS,
        REMOVE_FROM_GROUP_S0,
        SET_A1_FALSE_AGAIN,
        SET_D1_4,
        CALL_F1,
        CALL_H1,
    }

    data class Result<ClubId>(
        val guardedMutationRan: Boolean,
        val lastRoundActions: List<LastRoundAction>,
        val lastRoundParticipants: List<ClubId>?,
        val groupS0Pool: List<ClubId>,
        val rebuiltW: List<ClubId>?,
        /** `best.b.f1().clear()` is unconditional, including when the cD guard fails. */
        val clearGlobalF1: Boolean = true,
    )

    /**
     * `legacyF0` is the exact modern boundary for `konrent.o.f0(rawArgument)`. It is invoked in
     * bytecode order: optional last-round `S0()`, then `128 - count(first three N0 lists)`.
     */
    fun <ClubId> apply(
        hasLegacyGroup29: Boolean,
        legacyY1: Boolean,
        rounds: List<Round<ClubId>?>,
        groupS0Pool: List<ClubId>,
        legacyF0: (Int) -> List<ClubId>?,
    ): Result<ClubId> {
        if (!hasLegacyGroup29 || !legacyY1 || rounds.size != 4) {
            return Result(
                guardedMutationRan = false,
                lastRoundActions = emptyList(),
                lastRoundParticipants = rounds.lastOrNull()?.participants,
                groupS0Pool = groupS0Pool,
                rebuiltW = null,
            )
        }

        val mutableGroupS0 = groupS0Pool.toMutableList()
        val lastRound = rounds.last()
        val lastActions = mutableListOf<LastRoundAction>()
        var lastParticipants = lastRound?.participants

        if (lastRound != null) {
            lastActions += LastRoundAction.SET_A1_FALSE
            val recovered = legacyF0(lastRound.legacyS0)
            if (recovered != null && lastRound.legacyS0 == recovered.size) {
                lastParticipants = lastRound.participants + recovered
                mutableGroupS0.removeAll(recovered.toSet())
                lastActions +=
                    listOf(
                        LastRoundAction.ADD_F0_PARTICIPANTS,
                        LastRoundAction.REMOVE_FROM_GROUP_S0,
                        LastRoundAction.SET_A1_FALSE_AGAIN,
                        LastRoundAction.SET_D1_4,
                        LastRoundAction.CALL_F1,
                        LastRoundAction.CALL_H1,
                    )
            }
        }

        val firstThree = buildList {
            for (index in 0..2) {
                addAll(requireNotNull(rounds[index]) { "legacy cD first three rounds must be non-null" }.participants)
            }
        }
        val refillArgument = 128 - firstThree.size
        // Legacy calls addAll(f0(...)) without a null guard at this site.
        val refill = requireNotNull(legacyF0(refillArgument)) {
            "legacy konrent.o.f0(128-count) returned null"
        }

        return Result(
            guardedMutationRan = true,
            lastRoundActions = lastActions,
            lastRoundParticipants = lastParticipants,
            groupS0Pool = mutableGroupS0,
            rebuiltW = firstThree + refill,
        )
    }
}

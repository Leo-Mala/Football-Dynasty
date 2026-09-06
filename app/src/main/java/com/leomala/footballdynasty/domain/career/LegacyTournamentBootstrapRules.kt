package com.leomala.footballdynasty.domain.career

/**
 * Pure executable fragments recovered from legacy `konrent.b0.V()`.
 *
 * The legacy method begins by clearing all eight participant/competition references owned by the
 * bootstrap (`o`, `D`, `E`, `F`, `H`, `G`, `J`, `I`). It then recovers six participants from
 * already-existing competition objects, builds a fallback candidate list from clubs whose raw
 * `J()` is greater than 1, shuffles that list with `Collections.shuffle`, then fills only still-null
 * tier slots for raw `J()==2/3/4/5` with the first matching candidate encountered. A later local
 * `java.util.Random().nextInt(3)` chooses one of three fixed four-club index orders.
 *
 * This boundary deliberately accepts the already-shuffled candidate order and models prior-source
 * callback dispatch as an opaque call plan. It does not invent semantic names for the obfuscated
 * `t0/v0/b0/j0/c0` callbacks and does not claim ownership of the legacy unseeded
 * `Collections.shuffle` or local `Random` lifecycle.
 */
object LegacyTournamentBootstrapRules {
    data class OwnedReferences<T>(
        val o: T?,
        val dUpper: T?,
        val e: T?,
        val f: T?,
        val h: T?,
        val g: T?,
        val j: T?,
        val i: T?,
    )

    enum class PriorSource {
        Y0,
        V0,
        X0,
        W0,
        Z0,
        B0,
    }

    data class PriorCompetition<T>(
        val present: Boolean,
        val first: T?,
        val v0ReplacementWhenRawJ0Is131: T? = null,
    )

    data class PriorSourceCall<T>(
        val source: PriorSource,
        val participant: T?,
    )

    data class PriorParticipantRecovery<T>(
        val e: T?,
        val f: T?,
        val h: T?,
        val g: T?,
        val j: T?,
        val i: T?,
        val postSelectionCalls: List<PriorSourceCall<T>>,
    )

    data class TierSlots<T>(
        val tier2: T?,
        val tier3: T?,
        val tier4: T?,
        val tier5: T?,
    )

    fun <T> clearOwnedReferences(): OwnedReferences<T> =
        OwnedReferences(
            o = null,
            dUpper = null,
            e = null,
            f = null,
            h = null,
            g = null,
            j = null,
            i = null,
        )

    /**
     * Freezes the participant-recovery prefix of `konrent.b0.V()`.
     *
     * Source-to-slot routing is exactly `y0 -> E`, `v0 -> F`, `x0 -> H`, `w0 -> G`, `z0 -> J`,
     * `B0 -> I`. When a source object exists, legacy invokes its source-specific callback with the
     * first participant returned by `m(0)`, including a null first participant. Only the `v0`
     * source has an additional branch: after that callback, if its first participant is non-null
     * and raw `j0()==131`, slot `F` is replaced by `v0.k0()`.
     */
    fun <T> recoverPriorParticipants(
        y0: PriorCompetition<T>,
        v0: PriorCompetition<T>,
        x0: PriorCompetition<T>,
        w0: PriorCompetition<T>,
        z0: PriorCompetition<T>,
        b0: PriorCompetition<T>,
        rawJ0: (T) -> Int,
    ): PriorParticipantRecovery<T> {
        val calls = mutableListOf<PriorSourceCall<T>>()

        fun firstOrNull(
            source: PriorSource,
            competition: PriorCompetition<T>,
        ): T? {
            if (!competition.present) return null
            calls += PriorSourceCall(source, competition.first)
            return competition.first
        }

        val e = firstOrNull(PriorSource.Y0, y0)
        val v0First = firstOrNull(PriorSource.V0, v0)
        val f = if (v0.present && v0First != null && rawJ0(v0First) == 131) {
            v0.v0ReplacementWhenRawJ0Is131
        } else {
            v0First
        }
        val h = firstOrNull(PriorSource.X0, x0)
        val g = firstOrNull(PriorSource.W0, w0)
        val j = firstOrNull(PriorSource.Z0, z0)
        val i = firstOrNull(PriorSource.B0, b0)

        return PriorParticipantRecovery(
            e = e,
            f = f,
            h = h,
            g = g,
            j = j,
            i = i,
            postSelectionCalls = calls,
        )
    }

    fun <T> eligibleCandidates(
        source: List<T>,
        rawJ: (T) -> Int,
    ): List<T> = source.filter { rawJ(it) > 1 }

    fun <T> fillMissingTierSlots(
        shuffledCandidates: List<T>,
        initial: TierSlots<T>,
        rawJ: (T) -> Int,
    ): TierSlots<T> {
        var tier2 = initial.tier2
        var tier3 = initial.tier3
        var tier4 = initial.tier4
        var tier5 = initial.tier5

        for (candidate in shuffledCandidates) {
            when (rawJ(candidate)) {
                2 -> if (tier2 == null) tier2 = candidate
                3 -> if (tier3 == null) tier3 = candidate
                4 -> if (tier4 == null) tier4 = candidate
                5 -> if (tier5 == null) tier5 = candidate
            }

            if (tier2 != null && tier3 != null && tier4 != null && tier5 != null) break
        }

        return TierSlots(tier2, tier3, tier4, tier5)
    }

    fun fourClubOrder(legacyNextInt3: Int): IntArray =
        when (legacyNextInt3) {
            0 -> intArrayOf(2, 0, 3, 1)
            1 -> intArrayOf(3, 2, 1, 0)
            2 -> intArrayOf(0, 2, 3, 1)
            else -> error("legacyNextInt3 must be a java.util.Random.nextInt(3) result")
        }
}

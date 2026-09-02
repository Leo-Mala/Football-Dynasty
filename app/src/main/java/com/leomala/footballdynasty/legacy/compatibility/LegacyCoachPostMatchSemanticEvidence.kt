package com.leomala.footballdynasty.legacy.compatibility

/**
 * Mutation families proven to be touched by the reachable legacy coach post-match lifecycle.
 *
 * These names deliberately stay structural/neutral. Exact field formulas and ordering remain a
 * separate recovery problem and must not be inferred from this catalog.
 */
enum class LegacyCoachPostMatchMutationFamily {
    RAW_G,
    RAW_H,
    AGGREGATE_MANAGER_STATISTICS,
    SEASON_AND_CLUB_RECORDS,
}

/**
 * Semantic-recovery checkpoint for one exact legacy method.
 *
 * [completeFieldOrderingRecovered] is intentionally false until Java↔SMALI reconstruction has
 * proven every mutation and its ordering. Listing a mutation family is evidence of surface area,
 * not permission to persist a partial projection.
 */
data class LegacyCoachPostMatchMethodSemanticEvidence(
    val legacyMethod: String,
    val mutationFamilies: Set<LegacyCoachPostMatchMutationFamily>,
    val completeFieldOrderingRecovered: Boolean,
)

/**
 * Fail-closed semantic coverage catalog for `best.f0.i(best.s)` / `best.f0.j(best.s)`.
 *
 * The current official-corpus recovery proves that `i` mutates raw manager G/H state and that `j`
 * mutates aggregate manager statistics plus season/club records. It does not yet prove the complete
 * field-by-field formulas/order for both methods, so neither method is semantically complete.
 */
object LegacyCoachPostMatchSemanticEvidence {
    val methods: List<LegacyCoachPostMatchMethodSemanticEvidence> =
        listOf(
            LegacyCoachPostMatchMethodSemanticEvidence(
                legacyMethod = "best.f0.i(best.s)",
                mutationFamilies =
                    linkedSetOf(
                        LegacyCoachPostMatchMutationFamily.RAW_G,
                        LegacyCoachPostMatchMutationFamily.RAW_H,
                    ),
                completeFieldOrderingRecovered = false,
            ),
            LegacyCoachPostMatchMethodSemanticEvidence(
                legacyMethod = "best.f0.j(best.s)",
                mutationFamilies =
                    linkedSetOf(
                        LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                        LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
                    ),
                completeFieldOrderingRecovered = false,
            ),
        )

    private val byMethod: Map<String, LegacyCoachPostMatchMethodSemanticEvidence> =
        methods.associateBy { it.legacyMethod }

    fun findExact(legacyMethod: String): LegacyCoachPostMatchMethodSemanticEvidence? =
        byMethod[legacyMethod]

    /**
     * Promotion is derived from exact required methods rather than a manually toggled boolean.
     */
    fun completeFor(requiredMethods: Collection<String>): Boolean =
        requiredMethods.isNotEmpty() &&
            requiredMethods.all { required ->
                byMethod[required]?.completeFieldOrderingRecovered == true
            }
}

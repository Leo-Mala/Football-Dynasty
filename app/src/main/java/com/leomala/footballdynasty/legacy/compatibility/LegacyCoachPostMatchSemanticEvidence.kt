package com.leomala.footballdynasty.legacy.compatibility

/**
 * Mutation families proven to be touched by the reachable legacy coach post-match lifecycle.
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
 * [mutationFamilies] is the complete currently-proven surface touched by the method.
 * [fullyCharacterizedMutationFamilies] is deliberately narrower: a family enters that set only
 * after its exact mutation formula/control flow has already been recovered and regression tested.
 * That distinction prevents the already-characterized H projection from accidentally promoting
 * the unresolved G/statistics/record behavior.
 *
 * [completeFieldOrderingRecovered] stays false until Java↔SMALI reconstruction has proven every
 * mutation and its ordering, including interactions between the listed families.
 */
data class LegacyCoachPostMatchMethodSemanticEvidence(
    val legacyMethod: String,
    val mutationFamilies: Set<LegacyCoachPostMatchMutationFamily>,
    val fullyCharacterizedMutationFamilies: Set<LegacyCoachPostMatchMutationFamily>,
    val completeFieldOrderingRecovered: Boolean,
) {
    init {
        require(fullyCharacterizedMutationFamilies.all(mutationFamilies::contains)) {
            "Characterized mutation families must be part of the proven method surface"
        }
    }

    fun mutationFamilyCharacterized(family: LegacyCoachPostMatchMutationFamily): Boolean =
        family in fullyCharacterizedMutationFamilies

    /**
     * Exact fail-closed recovery remainder for this method. The order follows [mutationFamilies]
     * so the value is deterministic and can be used by tests/evidence tooling without inventing
     * semantic priority among still-unresolved fields.
     */
    val unresolvedMutationFamilies: Set<LegacyCoachPostMatchMutationFamily>
        get() = mutationFamilies.filterNotTo(linkedSetOf()) { it in fullyCharacterizedMutationFamilies }

    /**
     * A method may leave the fail-closed boundary only when both dimensions are complete:
     * every proven mutation family has its exact semantics recovered and the complete cross-field
     * ordering is known. Keeping these conditions together prevents a future ordering-only toggle
     * from promoting a partially reconstructed method.
     */
    val semanticallyComplete: Boolean
        get() =
            completeFieldOrderingRecovered &&
                unresolvedMutationFamilies.isEmpty()
}

/**
 * Fail-closed semantic coverage catalog for `best.f0.i(best.s)` / `best.f0.j(best.s)`.
 *
 * Official-corpus recovery currently proves:
 * - `i` mutates raw manager G/H state;
 * - the exact H-only match projection is already characterized by `LegacyCoachRawHRule`;
 * - G remains unresolved;
 * - `j` mutates aggregate manager statistics plus season/club records, whose complete formulas and
 *   ordering remain unresolved.
 *
 * Therefore neither method is semantically complete and production post-match persistence remains
 * blocked.
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
                fullyCharacterizedMutationFamilies =
                    linkedSetOf(
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
                fullyCharacterizedMutationFamilies = emptySet(),
                completeFieldOrderingRecovered = false,
            ),
        )

    private val byMethod: Map<String, LegacyCoachPostMatchMethodSemanticEvidence> =
        methods.associateBy { it.legacyMethod }

    fun findExact(legacyMethod: String): LegacyCoachPostMatchMethodSemanticEvidence? =
        byMethod[legacyMethod]

    /**
     * Deterministic unresolved-family projection for the exact required method sequence. Unknown
     * methods remain explicit blockers instead of being silently ignored.
     */
    fun unresolvedFor(requiredMethods: Collection<String>): Map<String, Set<LegacyCoachPostMatchMutationFamily>?> =
        linkedMapOf<String, Set<LegacyCoachPostMatchMutationFamily>?>().apply {
            requiredMethods.forEach { required ->
                this[required] = byMethod[required]?.unresolvedMutationFamilies
            }
        }

    /**
     * Promotion is derived from exact required methods rather than a manually toggled boolean.
     */
    fun completeFor(requiredMethods: Collection<String>): Boolean =
        requiredMethods.isNotEmpty() &&
            requiredMethods.all { required ->
                byMethod[required]?.semanticallyComplete == true
            }
}

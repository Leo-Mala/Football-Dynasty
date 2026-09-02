package com.leomala.footballdynasty.legacy.compatibility

/** Mutation families proven to be touched by the reachable legacy coach post-match lifecycle. */
enum class LegacyCoachPostMatchMutationFamily {
    RAW_G,
    RAW_H,
    AGGREGATE_MANAGER_STATISTICS,
    SEASON_AND_CLUB_RECORDS,
}

data class LegacyCoachPostMatchMethodSemanticEvidence(
    val legacyMethod: String,
    val mutationFamilies: Set<LegacyCoachPostMatchMutationFamily>,
    val fullyCharacterizedMutationFamilies: Set<LegacyCoachPostMatchMutationFamily>,
    val completeFieldOrderingRecovered: Boolean,
) {
    init {
        require(mutationFamilies.isNotEmpty()) {
            "A reachable coach post-match method must retain at least one proven mutation family"
        }
        require(fullyCharacterizedMutationFamilies.all(mutationFamilies::contains)) {
            "Characterized mutation families must be part of the proven method surface"
        }
    }

    fun mutationFamilyCharacterized(family: LegacyCoachPostMatchMutationFamily): Boolean =
        family in fullyCharacterizedMutationFamilies

    val unresolvedMutationFamilies: Set<LegacyCoachPostMatchMutationFamily>
        get() = mutationFamilies.filterNotTo(linkedSetOf()) { it in fullyCharacterizedMutationFamilies }

    val semanticallyComplete: Boolean
        get() = completeFieldOrderingRecovered && unresolvedMutationFamilies.isEmpty()
}

/**
 * Semantic coverage catalog for `best.f0.i(best.s)` / `best.f0.j(best.s)`.
 *
 * The official Java+SMALI corpus now has complete pure-rule representations for both methods:
 * - `LegacyCoachPostMatchAdjustmentRule` preserves every G write, the already-certified H writes,
 *   debt/matrix conditions, clamps and the final H<30 -> G-5 cross-field dependency;
 * - `LegacyCoachPostMatchStatisticsRule` preserves D/E/F/o, first season+club record lookup/append,
 *   A() -> alternative-l association priority and exact competition/subtype/mando point routing.
 *
 * This semantic checkpoint authorizes persistence work; it does not itself claim that a Room
 * persistence implementation already exists or that Phase 14 is closed.
 */
object LegacyCoachPostMatchSemanticEvidence {
    val methods: List<LegacyCoachPostMatchMethodSemanticEvidence> = listOf(
        LegacyCoachPostMatchMethodSemanticEvidence(
            legacyMethod = "best.f0.i(best.s)",
            mutationFamilies = linkedSetOf(
                LegacyCoachPostMatchMutationFamily.RAW_G,
                LegacyCoachPostMatchMutationFamily.RAW_H,
            ),
            fullyCharacterizedMutationFamilies = linkedSetOf(
                LegacyCoachPostMatchMutationFamily.RAW_G,
                LegacyCoachPostMatchMutationFamily.RAW_H,
            ),
            completeFieldOrderingRecovered = true,
        ),
        LegacyCoachPostMatchMethodSemanticEvidence(
            legacyMethod = "best.f0.j(best.s)",
            mutationFamilies = linkedSetOf(
                LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
            ),
            fullyCharacterizedMutationFamilies = linkedSetOf(
                LegacyCoachPostMatchMutationFamily.AGGREGATE_MANAGER_STATISTICS,
                LegacyCoachPostMatchMutationFamily.SEASON_AND_CLUB_RECORDS,
            ),
            completeFieldOrderingRecovered = true,
        ),
    )

    private val byMethod: Map<String, LegacyCoachPostMatchMethodSemanticEvidence> =
        methods.associateBy { it.legacyMethod }.also { indexed ->
            check(indexed.size == methods.size) {
                "Duplicate coach post-match semantic evidence would make recovery ambiguous"
            }
        }

    fun findExact(legacyMethod: String): LegacyCoachPostMatchMethodSemanticEvidence? = byMethod[legacyMethod]

    private fun requireUniqueRequiredMethods(requiredMethods: Collection<String>) {
        require(requiredMethods.size == requiredMethods.toSet().size) {
            "Duplicate required coach post-match methods would make caller recovery ambiguous"
        }
    }

    fun unresolvedFor(requiredMethods: Collection<String>): Map<String, Set<LegacyCoachPostMatchMutationFamily>?> {
        requireUniqueRequiredMethods(requiredMethods)
        return linkedMapOf<String, Set<LegacyCoachPostMatchMutationFamily>?>().apply {
            requiredMethods.forEach { required -> this[required] = byMethod[required]?.unresolvedMutationFamilies }
        }
    }

    fun completeFor(requiredMethods: Collection<String>): Boolean {
        requireUniqueRequiredMethods(requiredMethods)
        return requiredMethods.isNotEmpty() &&
            requiredMethods.all { required -> byMethod[required]?.semanticallyComplete == true }
    }
}

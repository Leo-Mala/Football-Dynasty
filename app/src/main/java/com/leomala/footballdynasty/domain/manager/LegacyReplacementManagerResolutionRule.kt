package com.leomala.footballdynasty.domain.manager

/**
 * Current-club fields copied by official `best.f0.k()` immediately before replacement resolution.
 */
data class LegacyReplacementCurrentClubSnapshot(
    val identityKey: String,
    /** Legacy `best.c0.j0()`. */
    val countryCode: Int,
    /** Legacy `best.c0.O()`. */
    val divisionValue: Int,
)

/** Pure projection of `best.f0.k()` onto the fields consumed by replacement-club discovery. */
object LegacyReplacementManagerRefreshRule {
    fun refreshForCurrentClub(
        manager: LegacyReplacementSearchManager,
        currentClub: LegacyReplacementCurrentClubSnapshot,
    ): LegacyReplacementSearchManager = manager.copy(
        rawE = currentClub.countryCode,
        rawD = currentClub.divisionValue - 1,
        excludedClubIdentityKey = currentClub.identityKey,
    )
}

enum class LegacyReplacementManagerResolutionEffect {
    REFRESH_CURRENT_MANAGER,
    TRY_MODE_0,
    TRY_MODE_1,
    TRY_MODE_2,
    TRY_MODE_MINUS_1,
    TRY_FIRST_UNEMPLOYED,
    SEARCH_REPLACEMENT_CLUBS_FALSE,
    SWAP_MANAGERS,
    TRANSFER_MANAGER_TO_TARGET,
    REPAIR_TARGET_SQUAD,
}

data class LegacyReplacementManagerResolutionResult<M>(
    val selectedManager: M?,
    val effectsInOrder: List<LegacyReplacementManagerResolutionEffect>,
)

/**
 * Exact persistence-independent host orchestration of official Phase 4R `best.c0.y()`.
 *
 * Candidate selection (`best.b.t/u`), replacement-club search (`best.b.B`), swap (`best.b.b4`) and
 * direct employment mutation (`best.b.G`) remain separate characterized rules. This host preserves
 * their call order, short-circuiting, reference-identity guards, legacy null dereferences, and the
 * final `c0.o()` side effect without re-implementing those helpers here.
 */
object LegacyReplacementManagerResolutionRule {
    fun <M, C> resolve(
        currentManager: () -> M?,
        refreshCurrentManager: (M) -> Unit,
        isHumanManager: (M) -> Boolean,
        tryCandidateMode: (Int) -> M?,
        /** Reads backing `best.c0.o` at the exact point after `t(0)` and `t(1)`. */
        targetRawLevelField: () -> Int,
        firstUnemployedNonHuman: () -> M?,
        searchReplacementClubsFalse: (M) -> List<C>,
        managerOfClub: (C) -> M?,
        sameManagerReference: (M, M?) -> Boolean,
        swapManagers: (M, M) -> Unit,
        transferManagerToTarget: (outgoing: M?, incoming: M) -> Unit,
        isTargetQ0: () -> Boolean,
        repairTargetSquad: () -> Unit,
    ): LegacyReplacementManagerResolutionResult<M> {
        val effects = mutableListOf<LegacyReplacementManagerResolutionEffect>()

        val initialManager = currentManager()
        val initialHuman = if (initialManager != null) {
            refreshCurrentManager(initialManager)
            effects += LegacyReplacementManagerResolutionEffect.REFRESH_CURRENT_MANAGER
            // SMALI calls y0() again before K(); retain the same observable lookup/null behavior.
            isHumanManager(currentManager()!!)
        } else {
            false
        }

        effects += LegacyReplacementManagerResolutionEffect.TRY_MODE_0
        var candidate = tryCandidateMode(0)

        if (candidate == null) {
            effects += LegacyReplacementManagerResolutionEffect.TRY_MODE_1
            candidate = tryCandidateMode(1)
        }

        if (candidate == null && targetRawLevelField() <= 3) {
            effects += LegacyReplacementManagerResolutionEffect.TRY_MODE_2
            candidate = tryCandidateMode(2)
        }

        if (candidate == null && isHumanManager(currentManager()!!)) {
            effects += LegacyReplacementManagerResolutionEffect.TRY_MODE_MINUS_1
            candidate = tryCandidateMode(-1)
        }

        if (candidate == null && isHumanManager(currentManager()!!)) {
            effects += LegacyReplacementManagerResolutionEffect.TRY_FIRST_UNEMPLOYED
            candidate = firstUnemployedNonHuman()
        }

        if (candidate == null && !isHumanManager(currentManager()!!)) {
            val managerForSearch = currentManager()!!
            effects += LegacyReplacementManagerResolutionEffect.SEARCH_REPLACEMENT_CLUBS_FALSE
            val clubs = searchReplacementClubsFalse(managerForSearch)

            var currentForSwap: M? = null
            if (clubs.isNotEmpty()) {
                candidate = managerOfClub(clubs.first())
                // The official host resolves y0() again only when B returned a non-empty list.
                currentForSwap = currentManager()
            }

            if (
                candidate != null &&
                currentForSwap != null &&
                !sameManagerReference(candidate, currentForSwap)
            ) {
                effects += LegacyReplacementManagerResolutionEffect.SWAP_MANAGERS
                swapManagers(candidate, currentForSwap)
            }
        } else if (candidate != null) {
            val outgoing = currentManager()
            if (!sameManagerReference(candidate, outgoing)) {
                effects += LegacyReplacementManagerResolutionEffect.TRANSFER_MANAGER_TO_TARGET
                transferManagerToTarget(outgoing, candidate)
            }
        }

        if (initialHuman && !isTargetQ0()) {
            effects += LegacyReplacementManagerResolutionEffect.REPAIR_TARGET_SQUAD
            repairTargetSquad()
        }

        return LegacyReplacementManagerResolutionResult(
            selectedManager = candidate,
            effectsInOrder = effects,
        )
    }
}

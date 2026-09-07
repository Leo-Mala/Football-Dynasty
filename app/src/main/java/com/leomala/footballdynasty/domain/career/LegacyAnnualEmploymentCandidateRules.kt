package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Pure executable projection of reachable legacy `best.b.A(best.f0, false)`.
 *
 * The official SMALI is authoritative for this rule. Raw field names are retained because their
 * sporting meaning is not required for parity. The legacy method builds a club pool from fixed
 * group-phase sources, calls `Collections.shuffle`, optionally applies a late-season raw-J quota,
 * selects by one of two fixed raw-p0 matrices, and finally applies a first-H0 replacement quirk.
 *
 * Modern code replaces the implicit shuffle seed with the project `RandomSource`, per AGENTS.md;
 * distribution/control-flow are preserved, but no APK implicit-seed bit-parity claim is made.
 * `best.n.g` ownership is intentionally absent: that static field is a separate transient-session
 * boundary characterized by [LegacyAnnualNEmploymentTransientRules].
 */
object LegacyAnnualEmploymentCandidateRules {
    data class Club<ClubId>(
        val id: ClubId,
        val legacyJ: Int,
        val legacyP0: Int,
        val legacyJ0: Int,
        val legacyF0: Int,
        val legacyQ0: Boolean = false,
    )

    data class Sources<ClubId>(
        /** Six fixed sources `c0/X/Z/a0/b0/d0`, in that exact order. Null means no group phase. */
        val lateSeasonSources: List<List<Club<ClubId>>?> = emptyList(),
        /** `t0().getFaseGrupos()`; non-null also selects the simple matrix-consumption branch. */
        val t0: List<Club<ClubId>>? = null,
        val f0: List<Club<ClubId>>? = null,
        val r: List<Club<ClubId>>? = null,
        val q: List<Club<ClubId>>? = null,
        val s: List<Club<ClubId>>? = null,
        val t: List<Club<ClubId>>? = null,
        val u: List<Club<ClubId>>? = null,
    )

    data class Input<ClubId>(
        val legacyN0: Int,
        val legacyB: Int,
        /** Raw `best.f0.w()`; directly indexes the fixed matrix family. */
        val legacyW: Int,
        /** Already-resolved raw `best.b.U0(best.f0.u()).p()`. */
        val resolvedLegacyPForU: Int,
        /** Raw `best.f0.A()?.J()`. */
        val sourceAClubLegacyJ: Int?,
        /** Raw `best.b.H0()[0].u()`; null means H0 was empty. */
        val firstH0LegacyU: Int?,
        /** Raw `best.b.H0()[0].A()?.p0()`. */
        val firstH0AClubLegacyP0: Int?,
        val sources: Sources<ClubId>,
    )

    data class Result<ClubId>(
        val lateSeasonBranch: Boolean,
        val hasLegacyT0GroupPhase: Boolean,
        val sourceCategoryOrder: List<Int>,
        val requiredLegacyP0: List<Int>,
        val sourceCandidates: List<Club<ClubId>>,
        val shuffledCandidates: List<Club<ClubId>>,
        val effectiveCandidates: List<Club<ClubId>>,
        val selected: List<Club<ClubId>>,
    )

    fun <ClubId> select(
        random: RandomSource,
        input: Input<ClubId>,
    ): Result<ClubId> {
        val lateSeason = input.legacyN0 - 1 == input.legacyB
        if (lateSeason) {
            require(input.sources.lateSeasonSources.size == 6) {
                "best.b.A late-season source vector must contain exactly six entries"
            }
        }

        val sourceCandidates = mutableListOf<Club<ClubId>>()
        val sourceCategoryOrder = mutableListOf<Int>()

        fun appendEligible(source: List<Club<ClubId>>?) {
            if (source == null) return
            source.forEach { club ->
                if (!club.legacyQ0) sourceCandidates += club
            }
        }

        if (lateSeason) {
            input.sources.lateSeasonSources.forEach(::appendEligible)
        }

        appendEligible(input.sources.t0)

        listOf(
            input.sources.f0 to 0,
            input.sources.r to 1,
            input.sources.q to 2,
            input.sources.s to 3,
            input.sources.t to 5,
            input.sources.u to 4,
        ).forEach { (source, rawCategory) ->
            if (source != null) {
                appendEligible(source)
                sourceCategoryOrder += rawCategory
            }
        }

        val beforeShuffle = sourceCandidates.toList()
        if (sourceCandidates.isNotEmpty()) {
            LegacyAnnualRandomRules.shuffleInPlace(sourceCandidates, random)
        }
        val shuffled = sourceCandidates.toList()

        val required =
            if (lateSeason) {
                LATE_SEASON_MATRIX[input.legacyW].toList()
            } else {
                DEFAULT_MATRIX[input.legacyW].toList()
            }

        val effective =
            if (lateSeason && sourceCandidates.isNotEmpty()) {
                applyLateSeasonQuota(
                    candidates = sourceCandidates,
                    resolvedLegacyPForU = input.resolvedLegacyPForU,
                    sourceAClubLegacyJ = input.sourceAClubLegacyJ,
                )
            } else {
                sourceCandidates.toList()
            }

        val selected =
            if (input.sources.t0 != null || sourceCategoryOrder.isEmpty()) {
                selectSimple(effective, required)
            } else {
                selectBySourceCategory(effective, required, sourceCategoryOrder)
            }.toMutableList()

        applyFirstH0Replacement(
            selected = selected,
            candidates = effective,
            firstH0LegacyU = input.firstH0LegacyU,
            firstH0AClubLegacyP0 = input.firstH0AClubLegacyP0,
        )

        return Result(
            lateSeasonBranch = lateSeason,
            hasLegacyT0GroupPhase = input.sources.t0 != null,
            sourceCategoryOrder = sourceCategoryOrder,
            requiredLegacyP0 = required,
            sourceCandidates = beforeShuffle,
            shuffledCandidates = shuffled,
            effectiveCandidates = effective,
            selected = selected,
        )
    }

    private fun <ClubId> applyLateSeasonQuota(
        candidates: List<Club<ClubId>>,
        resolvedLegacyPForU: Int,
        sourceAClubLegacyJ: Int?,
    ): List<Club<ClubId>> {
        val limits = intArrayOf(12, 7, 7, 8, 10, 10)
        val counts = IntArray(6)

        if (sourceAClubLegacyJ == 0) {
            // Exact bytecode quirk: index 0 is written as 12 and then immediately overwritten by 7.
            limits[0] = 12
            limits[0] = 7
        } else if (sourceAClubLegacyJ == 1) {
            limits[0] = 9
            limits[1] = 10
        }

        val filtered = mutableListOf<Club<ClubId>>()
        candidates.forEach { club ->
            if (counts[club.legacyJ] < limits[club.legacyJ] && !filtered.contains(club)) {
                filtered += club
                counts[club.legacyJ]++
            }
            if (
                resolvedLegacyPForU == club.legacyJ &&
                club.legacyP0 >= 4 &&
                !filtered.contains(club)
            ) {
                // This extra append does not increment the legacy per-J counter.
                filtered += club
            }
        }

        return if (filtered.isNotEmpty()) filtered else candidates
    }

    private fun <ClubId> selectSimple(
        candidates: List<Club<ClubId>>,
        requiredLegacyP0: List<Int>,
    ): List<Club<ClubId>> {
        val selected = mutableListOf<Club<ClubId>>()
        requiredLegacyP0.forEach { required ->
            if (required < 1) return@forEach
            candidates.firstOrNull { club ->
                club.legacyP0 == required && !selected.contains(club)
            }?.let(selected::add)
        }
        return selected
    }

    private fun <ClubId> selectBySourceCategory(
        candidates: List<Club<ClubId>>,
        requiredLegacyP0: List<Int>,
        sourceCategoryOrder: List<Int>,
    ): List<Club<ClubId>> {
        val selected = mutableListOf<Club<ClubId>>()
        var legacyJ5AlreadySelected = false

        repeat(2) {
            var categoryCursor = 0
            requiredLegacyP0.forEach { required ->
                if (required < 1) return@forEach
                val rawCategory = sourceCategoryOrder[categoryCursor]
                val candidate = candidates.firstOrNull { club ->
                    club.legacyP0 == required &&
                        club.legacyJ == rawCategory &&
                        !selected.contains(club)
                }

                if (candidate != null) {
                    if (rawCategory == 5) {
                        if (!legacyJ5AlreadySelected) {
                            selected += candidate
                            legacyJ5AlreadySelected = true
                        }
                    } else {
                        selected += candidate
                    }

                    categoryCursor++
                    if (categoryCursor >= sourceCategoryOrder.size) categoryCursor = 0
                }
            }
        }

        if (selected.size < requiredLegacyP0.size) {
            requiredLegacyP0.forEach { required ->
                if (required < 1 || selected.size >= requiredLegacyP0.size) return@forEach

                // Legacy stops at the first matching unselected p0 even when it is a second J==5
                // and therefore cannot be appended. It does not continue searching that slot.
                val candidate = candidates.firstOrNull { club ->
                    club.legacyP0 == required && !selected.contains(club)
                } ?: return@forEach

                if (candidate.legacyJ == 5) {
                    if (!legacyJ5AlreadySelected) {
                        selected += candidate
                        legacyJ5AlreadySelected = true
                    }
                } else {
                    selected += candidate
                }
            }
        }

        return selected
    }

    private fun <ClubId> applyFirstH0Replacement(
        selected: MutableList<Club<ClubId>>,
        candidates: List<Club<ClubId>>,
        firstH0LegacyU: Int?,
        firstH0AClubLegacyP0: Int?,
    ) {
        val targetLegacyJ0 = firstH0LegacyU ?: return
        if (targetLegacyJ0 < 0) return

        var replacement: Club<ClubId>? = null
        var replacementEnabled = false

        candidates.forEach { club ->
            if (club.legacyJ0 == targetLegacyJ0) {
                // SMALI calls selected.contains(candidates), i.e. the whole ArrayList object, not
                // selected.contains(club). The typed lists make that check effectively false, so
                // even an already-selected club remains eligible for this replacement scan.
                replacement = club
                if (club.legacyF0 < 18) {
                    replacementEnabled = true
                } else if (firstH0AClubLegacyP0 != null && firstH0AClubLegacyP0 >= 3) {
                    replacementEnabled = true
                }
            }
        }

        if (replacementEnabled) {
            val value = requireNotNull(replacement)
            if (selected.isEmpty()) {
                selected += value
            } else {
                selected[selected.lastIndex] = value
            }
        }
    }

    private val DEFAULT_MATRIX =
        arrayOf(
            intArrayOf(2, 1, 1, -1, -1),
            intArrayOf(2, 2, 1, 1, 1),
            intArrayOf(3, 2, 2, 1, 1),
            intArrayOf(4, 3, 2, 2, 1),
            intArrayOf(5, 4, 5, 3, 2),
            intArrayOf(5, 4, 4, 3, 2),
        )

    private val LATE_SEASON_MATRIX =
        arrayOf(
            intArrayOf(-1, -1, -1, -1, -1),
            intArrayOf(2, 2, 1, 1, 1, 1),
            intArrayOf(2, 2, 1, 1, 1, 1),
            intArrayOf(4, 3, 2, 2, 2, 1),
            intArrayOf(5, 4, 4, 3, 2, 1),
            intArrayOf(5, 5, 4, 4, 3, 2),
        )
}

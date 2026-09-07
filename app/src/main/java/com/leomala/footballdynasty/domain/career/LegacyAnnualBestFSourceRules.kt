package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.RandomSource

/**
 * Object/source-order projection for the reachable `best.f` constructor and its `q(...)`/`p()`
 * candidate collection paths.
 *
 * The names below intentionally retain raw legacy fields. The official executable proves that
 * `best.f` is a non-serializable runtime selector: it builds ordered `best.x` pools, derives an
 * ordered club candidate list, shuffles that list, and then applies already-characterized club
 * predicates. No Room ownership is introduced here.
 */
object LegacyAnnualBestFSourceRules {
    data class Club<ClubId>(
        val id: ClubId,
        val legacyO: Int,
        val legacyP0: Int,
        val legacyR0: Boolean,
        val legacyQ0: Boolean,
        val rosterSize: Int,
    )

    data class Group<GroupId, ClubId>(
        val id: GroupId,
        val legacyP: Int,
        val legacyA0: Int,
        val clubs: List<Club<ClubId>>,
    )

    data class GroupPools<GroupId, ClubId>(
        val primary: List<Group<GroupId, ClubId>>,
        val secondary: List<Group<GroupId, ClubId>>,
        val tertiary: List<Group<GroupId, ClubId>>,
    )

    data class QCandidate<ClubId>(
        val club: Club<ClubId>,
        /** Legacy `best.f.q(...)` calls `best.c0.D0(true)` before adding this candidate. */
        val markLegacyD0True: Boolean = true,
    )

    /**
     * Exact source-order pool construction from `best.f.<init>(o, int, boolean, boolean, mode)`.
     * `legacyNationalGroup` is the already-resolved result of legacy `best.b.T0(current.j0())`.
     */
    fun <GroupId, ClubId> buildGroupPools(
        random: RandomSource,
        mode: Int,
        currentLegacyJ: Int,
        currentLegacyJ0: Int,
        currentLegacyR0: Boolean,
        subjectOverall: Int,
        subjectO0: Boolean,
        subjectW0: Boolean,
        legacyNationalGroup: Group<GroupId, ClubId>?,
        allGroups: List<Group<GroupId, ClubId>>,
    ): GroupPools<GroupId, ClubId> {
        require(allGroups.map { it.id }.distinct().size == allGroups.size) {
            "best.f source groups must have unique modern identities"
        }

        val primary = mutableListOf<Group<GroupId, ClubId>>()
        val secondary = mutableListOf<Group<GroupId, ClubId>>()
        val tertiary = mutableListOf<Group<GroupId, ClubId>>()

        fun addPrimary(group: Group<GroupId, ClubId>) {
            if (primary.none { it.id == group.id }) primary += group
        }

        if (mode == 2) {
            allGroups.filterTo(primary) { it.legacyP == 0 }
            return GroupPools(primary = primary, secondary = secondary, tertiary = tertiary)
        }

        if (mode !in 0..1) {
            return GroupPools(primary = primary, secondary = secondary, tertiary = tertiary)
        }

        if (currentLegacyR0) {
            addPrimary(
                requireNotNull(legacyNationalGroup) {
                    "legacy T0(current.j0) group is required when current R0 is true"
                },
            )
        }

        val expandsPrimary =
            LegacyAnnualSelectionRules.bestFConstructorExpandsPrimaryGroup(
                random = random,
                mode = mode,
                legacyJ = currentLegacyJ,
                legacyJ0 = currentLegacyJ0,
                subjectO = subjectOverall,
                subjectO0 = subjectO0,
                subjectW0 = subjectW0,
            )

        if (expandsPrimary) {
            allGroups.filter { it.legacyP == 0 }.forEach(::addPrimary)
        }

        if (mode == 1) {
            when (currentLegacyJ) {
                1 -> allGroups.filter { it.legacyP != 5 }.forEach(::addPrimary)
                2 -> allGroups.filter { it.legacyP == 0 || it.legacyP == 3 }.forEach(::addPrimary)
                3 -> allGroups.filter { it.legacyP == 3 || it.legacyP == 0 }.forEach(::addPrimary)
            }

            if (currentLegacyJ0 == 131 || currentLegacyJ0 == 68) {
                allGroups.filter { it.legacyP == 0 }.forEach(::addPrimary)
            } else if (currentLegacyJ == 4) {
                allGroups.filter { it.legacyP == 4 }.forEach(::addPrimary)
            }

            if (currentLegacyJ == 5) {
                allGroups.filter { it.legacyP == 5 || it.legacyP == 3 }.forEach(::addPrimary)
            }
        }

        allGroups
            .filter { it.legacyP == 0 && primary.none { selected -> selected.id == it.id } }
            .forEach(secondary::add)
        allGroups.filter { it.legacyP == 0 }.forEach(tertiary::add)

        return GroupPools(primary = primary, secondary = secondary, tertiary = tertiary)
    }

    /**
     * Exact pre-shuffle club collection from legacy `best.f.q(...)`, including source order and
     * the per-candidate `D0(true)` side-effect signal. Duplicate club occurrences are retained if
     * the same club is present in multiple source groups, matching the legacy list append logic.
     */
    fun <GroupId, ClubId> collectQCandidates(
        groups: List<Group<GroupId, ClubId>>,
        currentClubId: ClubId,
        currentLegacyO: Int,
        currentLegacyJ: Int,
        currentLegacyP0: Int,
        subjectOverall: Int,
        mode: Int,
    ): List<QCandidate<ClubId>> = buildList {
        groups.forEach { group ->
            val range =
                LegacyAnnualSelectionRules.bestFQRange(
                    mode = mode,
                    currentO = currentLegacyO,
                    currentJ = currentLegacyJ,
                    currentP0 = currentLegacyP0,
                    subjectO = subjectOverall,
                    groupA0 = group.legacyA0,
                )

            group.clubs.forEach { club ->
                if (
                    LegacyAnnualSelectionRules.bestFQCandidateEligible(
                        candidateO = club.legacyO,
                        range = range,
                        isCurrent = club.id == currentClubId,
                        candidateQ0 = club.legacyQ0,
                        rosterSize = club.rosterSize,
                    )
                ) {
                    add(QCandidate(club = club))
                }
            }
        }
    }

    /** Exact source-order pre-shuffle collection from legacy `best.f.p()`. */
    fun <ClubId> collectFallbackCandidates(allClubs: List<Club<ClubId>>): List<Club<ClubId>> =
        allClubs.filter { club ->
            LegacyAnnualSelectionRules.bestFPFallbackEligible(
                candidateR0 = club.legacyR0,
                candidateQ0 = club.legacyQ0,
                rosterSize = club.rosterSize,
            )
        }
}

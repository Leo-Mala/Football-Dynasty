package com.leomala.footballdynasty.domain.manager

enum class LegacyCareerContinuationEffect {
    SET_S2_FALSE,
    RESET_SPECIAL_FLAG,
    OPEN_MAIN_TEAM,
    SET_SPECIAL_FLAG_H1,
    SET_Q3_FALSE,
    SET_SPECIAL_FLAG_G1,
    RUN_Y3_G,
    RUN_S_R0,
    RUN_I_I,
    DISPATCH_PENDING_MATCH,
    DISPATCH_POST_SEASON,
    SET_R3_FALSE,
}

data class LegacyCareerContinuationResult(
    val specialFlag: Boolean,
    val effectsInOrder: List<LegacyCareerContinuationEffect>,
)

/** Exact persistence-independent orchestration of official Phase 4R `best.n.i()`. */
object LegacyCareerContinuationRule {
    fun execute(
        setS2: (Boolean) -> Unit,
        setSpecialFlag: (Boolean) -> Unit,
        s1: () -> Boolean,
        p0: () -> Any?,
        h1: () -> Boolean,
        i1: () -> Int,
        legacyIF: () -> Boolean,
        e2: () -> Boolean,
        setQ3: (Boolean) -> Unit,
        g1: () -> Boolean,
        legacyIS: () -> Boolean,
        runY3G: () -> Unit,
        runSR0: () -> Unit,
        legacyV0: () -> Boolean,
        legacyIH: () -> Boolean,
        runII: () -> Unit,
        openMainTeam: () -> Unit,
        dispatchPendingMatch: (specialFlag: Boolean) -> Unit,
        dispatchPostSeason: (specialFlag: Boolean) -> Unit,
        f2: () -> Boolean,
        setR3: (Boolean) -> Unit,
    ): LegacyCareerContinuationResult {
        val effects = mutableListOf<LegacyCareerContinuationEffect>()
        effects += LegacyCareerContinuationEffect.SET_S2_FALSE
        setS2(false)
        effects += LegacyCareerContinuationEffect.RESET_SPECIAL_FLAG
        var specialFlag = false
        setSpecialFlag(false)

        if (s1() && p0() != null) {
            effects += LegacyCareerContinuationEffect.OPEN_MAIN_TEAM
            openMainTeam()
        } else {
            if (h1() && i1() == 1 && !legacyIF() && !e2()) {
                specialFlag = true
                effects += LegacyCareerContinuationEffect.SET_SPECIAL_FLAG_H1
                setSpecialFlag(true)
            }
            if (e2()) {
                effects += LegacyCareerContinuationEffect.SET_Q3_FALSE
                setQ3(false)
            }
            if (g1() && i1() == 3 && !legacyIS()) {
                specialFlag = true
                effects += LegacyCareerContinuationEffect.SET_SPECIAL_FLAG_G1
                setSpecialFlag(true)
            }

            effects += LegacyCareerContinuationEffect.RUN_Y3_G
            runY3G()
            effects += LegacyCareerContinuationEffect.RUN_S_R0
            runSR0()

            if (legacyV0() && !specialFlag && legacyIH()) {
                effects += LegacyCareerContinuationEffect.RUN_I_I
                runII()
                effects += LegacyCareerContinuationEffect.DISPATCH_PENDING_MATCH
                dispatchPendingMatch(specialFlag)
            } else {
                effects += LegacyCareerContinuationEffect.DISPATCH_POST_SEASON
                dispatchPostSeason(specialFlag)
            }
        }

        if (f2()) {
            effects += LegacyCareerContinuationEffect.SET_R3_FALSE
            setR3(false)
        }
        return LegacyCareerContinuationResult(specialFlag, effects)
    }
}

enum class LegacyPendingMatchLaunchEffect {
    CLEAR_ACTIVITY_MATCH_BUFFER,
    DISPATCH_POST_SEASON,
    SET_ACTIVITY_F_ZERO,
    SET_ACTIVITY_G_ONE,
    SET_ACTIVITY_I_FALSE,
    CREATE_ACTIVITY_MATCH_BUFFER,
    MARK_MATCH_FALSE,
    OPEN_MATCH,
}

data class LegacyPendingMatchLaunchResult(
    val pendingMatchIndex: Int,
    val effectsInOrder: List<LegacyPendingMatchLaunchEffect>,
)

/** Exact persistence-independent orchestration of official Phase 4R `best.n.h()`. */
object LegacyPendingMatchLaunchRule {
    fun <R> execute(
        pendingMatchIndex: () -> Int,
        hasActivityMatchBuffer: () -> Boolean,
        clearActivityMatchBuffer: () -> Unit,
        setActivityF: (Int) -> Unit,
        setActivityG: (Int) -> Unit,
        setActivityI: (Boolean) -> Unit,
        createActivityMatchBuffer: () -> Unit,
        pendingMatches: () -> List<R>,
        markMatch: (R, Boolean) -> Unit,
        openMatch: (index: Int) -> Unit,
        dispatchPostSeason: () -> Unit,
    ): LegacyPendingMatchLaunchResult {
        val effects = mutableListOf<LegacyPendingMatchLaunchEffect>()
        val index = pendingMatchIndex()
        if (index < 0) {
            if (hasActivityMatchBuffer()) {
                effects += LegacyPendingMatchLaunchEffect.CLEAR_ACTIVITY_MATCH_BUFFER
                clearActivityMatchBuffer()
            }
            effects += LegacyPendingMatchLaunchEffect.DISPATCH_POST_SEASON
            dispatchPostSeason()
            return LegacyPendingMatchLaunchResult(index, effects)
        }

        effects += LegacyPendingMatchLaunchEffect.SET_ACTIVITY_F_ZERO
        setActivityF(0)
        effects += LegacyPendingMatchLaunchEffect.SET_ACTIVITY_G_ONE
        setActivityG(1)
        effects += LegacyPendingMatchLaunchEffect.SET_ACTIVITY_I_FALSE
        setActivityI(false)
        effects += LegacyPendingMatchLaunchEffect.CREATE_ACTIVITY_MATCH_BUFFER
        createActivityMatchBuffer()
        val match = pendingMatches()[index]
        effects += LegacyPendingMatchLaunchEffect.MARK_MATCH_FALSE
        markMatch(match, false)
        effects += LegacyPendingMatchLaunchEffect.OPEN_MATCH
        openMatch(index)
        return LegacyPendingMatchLaunchResult(index, effects)
    }
}

enum class LegacyPostSeasonResultsEffect {
    RUN_S_G,
    RUN_B_V,
    RUN_WORLD_E,
    RUN_WORLD_M,
    RESET_NATIONAL_INVITES,
    SET_J2_ZERO,
    OPEN_RESULTS,
    DISPATCH_INVITATIONS,
}

/** Exact persistence-independent orchestration of official Phase 4R `best.n.j()`. */
object LegacyPostSeasonResultsRule {
    fun execute(
        runSG: () -> Unit,
        runBV: () -> Unit,
        runWorldE: () -> Unit,
        runWorldM: () -> Unit,
        resetNationalInvites: () -> Unit,
        setJ2: (Int) -> Unit,
        legacyV0: () -> Boolean,
        specialFlag: Boolean,
        openResults: () -> Unit,
        dispatchInvitations: () -> Unit,
    ): List<LegacyPostSeasonResultsEffect> {
        val effects = mutableListOf<LegacyPostSeasonResultsEffect>()
        effects += LegacyPostSeasonResultsEffect.RUN_S_G
        runSG()
        effects += LegacyPostSeasonResultsEffect.RUN_B_V
        runBV()
        effects += LegacyPostSeasonResultsEffect.RUN_WORLD_E
        runWorldE()
        effects += LegacyPostSeasonResultsEffect.RUN_WORLD_M
        runWorldM()
        effects += LegacyPostSeasonResultsEffect.RESET_NATIONAL_INVITES
        resetNationalInvites()
        effects += LegacyPostSeasonResultsEffect.SET_J2_ZERO
        setJ2(0)
        if (legacyV0() && !specialFlag) {
            effects += LegacyPostSeasonResultsEffect.OPEN_RESULTS
            openResults()
        } else {
            effects += LegacyPostSeasonResultsEffect.DISPATCH_INVITATIONS
            dispatchInvitations()
        }
        return effects
    }
}

enum class LegacyInvitationDispatchEffect {
    RESET_CLUB_INVITES,
    RESET_SELECTED_MANAGER,
    LOAD_CLUB_INVITES_TRUE,
    LOAD_CLUB_INVITES_FALSE,
    OPEN_NATIONAL_INVITATION,
    OPEN_CLUB_INVITATION,
    DISPATCH_DISMISSALS,
}

data class LegacyInvitationDispatchResult<M>(
    val clubInvitations: List<*>?,
    val selectedManager: M?,
    val effectsInOrder: List<LegacyInvitationDispatchEffect>,
)

/** Exact persistence-independent orchestration of official Phase 4R `best.n.k()`. */
object LegacyInvitationDispatchRule {
    fun <M> execute(
        setClubInvites: (List<*>?) -> Unit,
        setSelectedManager: (M?) -> Unit,
        currentCompetitionKind: () -> Int,
        q0: () -> Int,
        loadClubInvites: (kind: Int, q0IsZero: Boolean) -> List<*>?,
        nationalInvites: () -> List<*>?,
        managers: () -> List<M>,
        isHumanManager: (M) -> Boolean,
        openNationalInvitation: (selectedManager: M?) -> Unit,
        openClubInvitation: (selectedManager: M?) -> Unit,
        dispatchDismissals: () -> Unit,
    ): LegacyInvitationDispatchResult<M> {
        val effects = mutableListOf<LegacyInvitationDispatchEffect>()
        effects += LegacyInvitationDispatchEffect.RESET_CLUB_INVITES
        setClubInvites(null)
        effects += LegacyInvitationDispatchEffect.RESET_SELECTED_MANAGER
        setSelectedManager(null)

        val kind = currentCompetitionKind()
        val q0IsZero = q0() == 0
        var clubInvites: List<*>? = null
        if (q0IsZero) {
            effects += LegacyInvitationDispatchEffect.LOAD_CLUB_INVITES_TRUE
            clubInvites = loadClubInvites(kind, true)
            setClubInvites(clubInvites)
        }
        if (!q0IsZero && (kind == 1 || kind == 3)) {
            effects += LegacyInvitationDispatchEffect.LOAD_CLUB_INVITES_FALSE
            clubInvites = loadClubInvites(kind, false)
            setClubInvites(clubInvites)
        }

        val national = nationalInvites()
        if (national != null && national.isNotEmpty()) {
            val selected = firstHumanManagerLikeLegacy(managers, isHumanManager)
            if (selected != null) setSelectedManager(selected)
            effects += LegacyInvitationDispatchEffect.OPEN_NATIONAL_INVITATION
            openNationalInvitation(selected)
            return LegacyInvitationDispatchResult(clubInvites, selected, effects)
        }
        if (clubInvites != null && clubInvites.isNotEmpty()) {
            val selected = firstHumanManagerLikeLegacy(managers, isHumanManager)
            if (selected != null) setSelectedManager(selected)
            effects += LegacyInvitationDispatchEffect.OPEN_CLUB_INVITATION
            openClubInvitation(selected)
            return LegacyInvitationDispatchResult(clubInvites, selected, effects)
        }

        effects += LegacyInvitationDispatchEffect.DISPATCH_DISMISSALS
        dispatchDismissals()
        return LegacyInvitationDispatchResult(clubInvites, null, effects)
    }

    private fun <M> firstHumanManagerLikeLegacy(
        managers: () -> List<M>,
        isHumanManager: (M) -> Boolean,
    ): M? {
        var index = 0
        while (true) {
            if (index >= managers().size) return null
            val candidate = managers()[index]
            if (isHumanManager(candidate)) {
                return managers()[index]
            }
            index++
        }
    }
}

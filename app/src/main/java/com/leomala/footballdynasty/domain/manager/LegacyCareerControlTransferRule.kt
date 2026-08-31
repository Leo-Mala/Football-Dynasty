package com.leomala.footballdynasty.domain.manager

enum class LegacyCareerResumeEffect {
    RUN_END_OF_YEAR_FINALIZER,
    CONTINUE_CAREER,
}

object LegacyCareerResumeRule {
    fun dispatch(
        legacyEndOfYearFlag: () -> Boolean,
        runEndOfYearFinalizer: () -> Unit,
        continueCareer: () -> Unit,
    ): LegacyCareerResumeEffect {
        return if (legacyEndOfYearFlag()) {
            runEndOfYearFinalizer()
            LegacyCareerResumeEffect.RUN_END_OF_YEAR_FINALIZER
        } else {
            continueCareer()
            LegacyCareerResumeEffect.CONTINUE_CAREER
        }
    }
}

enum class LegacyControlledClubSwitchEffect {
    OLD_CLUB_UNCONTROLLED,
    OLD_MANAGER_UNCONTROLLED,
    NEW_CLUB_CONTROLLED,
    NEW_MANAGER_CONTROLLED,
    MANAGER_LIST_CLEARED,
    NEW_MANAGER_ADDED,
}

data class LegacyControlledClubSwitchResult<C, M>(
    val previousClub: C?,
    val selectedClub: C?,
    val selectedManager: M?,
    val effectsInOrder: List<LegacyControlledClubSwitchEffect>,
)

/**
 * Persistence-independent reconstruction of official `best.n.s(String)`.
 *
 * Source order is observable: the first controlled club is selected as the outgoing club and the
 * first club whose `g0()` equals the requested name is selected as the incoming club. The legacy
 * code performs both scans before mutating either club and does not special-case identical source
 * and destination objects, so the false -> true transition is preserved even for a self-switch.
 */
object LegacyControlledClubSwitchRule {
    fun <C, M> switchByName(
        clubs: List<C>,
        requestedClubName: String?,
        isControlledClub: (C) -> Boolean,
        clubName: (C) -> String,
        managerOfClub: (C) -> M,
        setClubControlled: (C, Boolean) -> Unit,
        setManagerControlled: (M, Boolean) -> Unit,
        clearManagerList: () -> Unit,
        addManager: (M) -> Unit,
    ): LegacyControlledClubSwitchResult<C, M> {
        var previousClub: C? = null
        for (club in clubs) {
            if (isControlledClub(club)) {
                previousClub = club
                break
            }
        }

        var selectedClub: C? = null
        for (club in clubs) {
            if (clubName(club).equals(requestedClubName)) {
                selectedClub = club
                break
            }
        }

        if (previousClub == null || selectedClub == null) {
            return LegacyControlledClubSwitchResult(
                previousClub = previousClub,
                selectedClub = selectedClub,
                selectedManager = null,
                effectsInOrder = emptyList(),
            )
        }

        val effects = mutableListOf<LegacyControlledClubSwitchEffect>()
        val previousManager = managerOfClub(previousClub)
        val selectedManager = managerOfClub(selectedClub)

        setClubControlled(previousClub, false)
        effects += LegacyControlledClubSwitchEffect.OLD_CLUB_UNCONTROLLED
        setManagerControlled(previousManager, false)
        effects += LegacyControlledClubSwitchEffect.OLD_MANAGER_UNCONTROLLED
        setClubControlled(selectedClub, true)
        effects += LegacyControlledClubSwitchEffect.NEW_CLUB_CONTROLLED
        setManagerControlled(selectedManager, true)
        effects += LegacyControlledClubSwitchEffect.NEW_MANAGER_CONTROLLED
        clearManagerList()
        effects += LegacyControlledClubSwitchEffect.MANAGER_LIST_CLEARED
        addManager(selectedManager)
        effects += LegacyControlledClubSwitchEffect.NEW_MANAGER_ADDED

        return LegacyControlledClubSwitchResult(
            previousClub = previousClub,
            selectedClub = selectedClub,
            selectedManager = selectedManager,
            effectsInOrder = effects,
        )
    }
}

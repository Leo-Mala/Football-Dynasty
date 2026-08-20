package com.leomala.footballdynasty.domain.career

import com.leomala.footballdynasty.foundation.random.StatefulJavaRandomSource

object CareerStateFactory {
    fun create(
        id: String,
        seed: Long,
        managedClubId: String? = null,
    ): CareerState {
        require(id.isNotBlank()) { "Career id must not be blank" }
        val randomSnapshot = StatefulJavaRandomSource(seed).snapshot()
        val state = CareerState(
            id = id,
            season = SeasonState(number = 1, year = LegacyCalendarRules.BASE_YEAR),
            calendar = LegacyCalendarRules.calendarForSeason(1),
            managedClub = managedClubId?.let(::ManagedClubState),
            random = CareerRandomState(
                initialSeed = randomSnapshot.initialSeed,
                internalState = randomSnapshot.internalState,
                draws = randomSnapshot.draws,
            ),
        )
        CareerIntegrityValidator.validate(state)
        return state
    }
}

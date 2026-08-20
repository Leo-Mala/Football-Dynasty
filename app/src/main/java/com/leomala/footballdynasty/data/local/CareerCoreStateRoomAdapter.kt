package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.data.local.entity.CareerCoreStateEntity
import com.leomala.footballdynasty.domain.career.CareerCalendarState
import com.leomala.footballdynasty.domain.career.CareerRandomState
import com.leomala.footballdynasty.domain.career.CareerState
import com.leomala.footballdynasty.domain.career.ManagedClubState
import com.leomala.footballdynasty.domain.career.SeasonState

object CareerCoreStateRoomAdapter {
    fun entity(
        state: CareerState,
        updatedAtEpochMillis: Long,
    ): CareerCoreStateEntity = CareerCoreStateEntity(
        careerId = state.id,
        stateVersion = state.stateVersion,
        seasonNumber = state.season.number,
        seasonYear = state.season.year,
        calendarYear = state.calendar.year,
        currentDayIndex = state.calendar.currentDayIndex,
        startDayIndex = state.calendar.startDayIndex,
        dayCount = state.calendar.dayCount,
        rngInitialSeed = state.random.initialSeed,
        rngInternalState = state.random.internalState,
        rngDraws = state.random.draws,
        managedClubId = state.managedClub?.clubId,
        transitionCount = state.transitionCount,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

    fun state(entity: CareerCoreStateEntity): CareerState = CareerState(
        id = entity.careerId,
        stateVersion = entity.stateVersion,
        season = SeasonState(entity.seasonNumber, entity.seasonYear),
        calendar = CareerCalendarState(
            year = entity.calendarYear,
            currentDayIndex = entity.currentDayIndex,
            startDayIndex = entity.startDayIndex,
            dayCount = entity.dayCount,
        ),
        managedClub = entity.managedClubId?.let(::ManagedClubState),
        random = CareerRandomState(
            initialSeed = entity.rngInitialSeed,
            internalState = entity.rngInternalState,
            draws = entity.rngDraws,
        ),
        transitionCount = entity.transitionCount,
    )
}

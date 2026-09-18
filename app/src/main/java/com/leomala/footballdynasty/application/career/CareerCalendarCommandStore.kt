package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.CareerMatchStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.repository.RoomCareerStateRepository
import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerScheduleCalendarProjection
import com.leomala.footballdynasty.domain.career.CareerTransition

/**
 * Phase 17 write boundary for the already-characterized legacy dJ next-event selection.
 *
 * The boundary consumes only the persisted match schedule and delegates the actual state
 * transition/persistence to CareerSimulationCoordinator. It does not invent dates, events,
 * competition rules, RNG or season transitions.
 */
class CareerCalendarCommandStore(
    database: FootballDynastyDatabase,
    clockMillis: () -> Long = System::currentTimeMillis,
) {
    private val matchStore = CareerMatchStore(database, clockMillis)
    private val simulationCoordinator = CareerSimulationCoordinator(
        RoomCareerStateRepository(database, clockMillis)
    )

    suspend fun moveToNextScheduledEvent(careerId: String): CareerTransition {
        require(careerId.isNotBlank()) { "Career id must not be blank" }
        val schedule = matchStore.loadSchedule(careerId)
        return simulationCoordinator.apply(
            careerId = careerId,
            command = CareerCommand.MoveToNextScheduledEvent(
                CareerScheduleCalendarProjection.calendarDays(schedule)
            ),
        )
    }
}

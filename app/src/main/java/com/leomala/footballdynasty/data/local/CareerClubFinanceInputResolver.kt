package com.leomala.footballdynasty.data.local

import com.leomala.footballdynasty.domain.model.RosterKind

/**
 * Resolves characterized club-finance inputs from persisted career/source state.
 *
 * Salary-bearing paths in legacy `c0.q()` read the club's actual senior and youth rosters. Modern
 * callers must therefore not inject ad-hoc salary lists. Missing commercial materialization fails
 * closed instead of silently dropping a player from payroll.
 *
 * The annual sponsor path also reads immutable country, raw division O() and Q0() from the club.
 * Country comes from the frozen source club row; division comes from the V9 ticket/club runtime
 * materialization; Q0 is represented by the already-materialized manager-runtime active flag.
 */
data class CareerClubFinanceRuntimeInput(
    val rawCountryCode: Int,
    val rawDivisionCode: Int,
    val seniorSalaryCodes: List<Int>,
    val youthSalaryCodes: List<Int>,
    val recordFinanceLedger: Boolean,
)

class CareerClubFinanceInputResolver(
    private val database: FootballDynastyDatabase,
) {
    private val managerDao = database.careerManagerRuntimeDao()
    private val playerDao = database.careerPlayerRuntimeDao()
    private val ticketStore = CareerTicketRuntimeStore(database)

    suspend fun resolve(
        careerId: String,
        clubId: String,
    ): CareerClubFinanceRuntimeInput {
        val club = requireNotNull(database.clubDao().findById(clubId)) {
            "Missing immutable club $clubId"
        }
        val runtime = requireNotNull(managerDao.findClubRuntime(careerId, clubId)) {
            "Missing materialized club manager runtime $careerId/$clubId"
        }
        val ticket = requireNotNull(ticketStore.findClubState(careerId, clubId)) {
            "Missing materialized V9 club finance/ticket state $careerId/$clubId"
        }

        return CareerClubFinanceRuntimeInput(
            rawCountryCode = club.country,
            rawDivisionCode = ticket.rawDivisionCode,
            seniorSalaryCodes = salaryCodes(careerId, clubId, RosterKind.SENIOR),
            youthSalaryCodes = salaryCodes(careerId, clubId, RosterKind.JUNIOR),
            recordFinanceLedger = runtime.active,
        )
    }

    suspend fun resolvePayroll(
        careerId: String,
        clubId: String,
    ): Pair<List<Int>, List<Int>> =
        salaryCodes(careerId, clubId, RosterKind.SENIOR) to
            salaryCodes(careerId, clubId, RosterKind.JUNIOR)

    private suspend fun salaryCodes(
        careerId: String,
        clubId: String,
        rosterKind: RosterKind,
    ): List<Int> = playerDao.membershipsForClub(careerId, clubId, rosterKind.name).map { membership ->
        requireNotNull(managerDao.findPlayerCommercial(careerId, membership.playerId)) {
            "Missing materialized commercial state for payroll player ${membership.playerId}"
        }.salario
    }
}

package com.leomala.footballdynasty.application.career

import com.leomala.footballdynasty.data.local.entity.CareerClubManagerRuntimeEntity
import com.leomala.footballdynasty.data.local.entity.CareerSquadMembershipEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CareerLineupClubEligibilityOwnerStoreTest {
    @Test
    fun `membership and active club runtime materialize exact u0 and q0 owners`() {
        assertEquals(
            CareerLineupClubEligibilityOwner(
                clubId = CLUB_A,
                hasClub = true,
                clubActiveQ0 = true,
            ),
            CareerLineupClubEligibilityOwnerStore.resolvePersisted(
                careerId = CAREER_A,
                membership = membership(),
                clubRuntime = clubRuntime(active = true),
            ),
        )
    }

    @Test
    fun `false q0 is preserved instead of being treated as unresolved`() {
        assertEquals(
            CareerLineupClubEligibilityOwner(
                clubId = CLUB_A,
                hasClub = true,
                clubActiveQ0 = false,
            ),
            CareerLineupClubEligibilityOwnerStore.resolvePersisted(
                careerId = CAREER_A,
                membership = membership(),
                clubRuntime = clubRuntime(active = false),
            ),
        )
    }

    @Test
    fun `missing club runtime remains fail closed`() {
        assertNull(
            CareerLineupClubEligibilityOwnerStore.resolvePersisted(
                careerId = CAREER_A,
                membership = membership(),
                clubRuntime = null,
            )
        )
    }

    @Test
    fun `mismatched persisted ownership remains fail closed`() {
        assertNull(
            CareerLineupClubEligibilityOwnerStore.resolvePersisted(
                careerId = CAREER_A,
                membership = membership(),
                clubRuntime = clubRuntime(active = true, clubId = CLUB_B),
            )
        )
        assertNull(
            CareerLineupClubEligibilityOwnerStore.resolvePersisted(
                careerId = CAREER_A,
                membership = membership(careerId = CAREER_B),
                clubRuntime = clubRuntime(active = true),
            )
        )
    }

    private fun membership(
        careerId: String = CAREER_A,
        clubId: String = CLUB_A,
    ) = CareerSquadMembershipEntity(
        careerId = careerId,
        playerId = PLAYER_A,
        clubId = clubId,
        rosterKind = "SENIOR",
        sourceOrdinal = 0,
    )

    private fun clubRuntime(
        active: Boolean,
        careerId: String = CAREER_A,
        clubId: String = CLUB_A,
    ) = CareerClubManagerRuntimeEntity(
        careerId = careerId,
        clubId = clubId,
        active = active,
        cash = 0L,
        primarySlotPlayerCode = null,
        secondarySlotPlayerCode = null,
        rawStateFlag = false,
        ticketIncome = 0,
        playerSaleIncome = 0L,
        prizeIncome = 0,
        sponsorIncome = 0,
        playerPurchaseExpense = 0L,
        stadiumExpense = 0,
        salaryExpense = 0L,
        borrowingChargeExpense = 0,
        fineExpense = 0,
        miscellaneousExpense = 0,
        borrowed = 0,
        monthlyBorrowingCharge = 0,
    )

    private companion object {
        const val CAREER_A = "career-a"
        const val CAREER_B = "career-b"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
        const val PLAYER_A = "player-a"
    }
}

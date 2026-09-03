package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagedClubVisualIdentityTest {
    @Test
    fun `preserves raw source backed visual identity without normalization`() {
        val team = LegacyTeamSnapshot(
            name = "Fixture Club",
            fileRef = "fixture.ban",
            country = 1,
            state = 2,
            level = 3,
            stadium = "Fixture Stadium",
            capacity = 12345,
            reputation = 67,
            players = emptyList(),
            juniors = emptyList(),
            primaryColor = "#00AaFf",
            secondaryColor = " legacy-secondary ",
            baseColor = -123456789,
        )

        assertEquals(
            LegacyManagedClubVisualIdentity(
                primaryColor = "#00AaFf",
                secondaryColor = " legacy-secondary ",
                baseColor = -123456789,
            ),
            LegacyManagedClubVisualIdentityProjection.from(team),
        )
    }
}

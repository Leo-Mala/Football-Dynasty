package com.leomala.footballdynasty.domain.manager

import com.leomala.footballdynasty.domain.model.LegacyTeamSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class LegacyManagedClubSourceIdentityTest {
    @Test
    fun preservesOpaqueLegacyIdentityFieldsWithoutInterpretation() {
        val team = LegacyTeamSnapshot(
            name = "Legacy",
            fileRef = "teams/legacy.ban",
            country = 0,
            state = 0,
            level = 0,
            stadium = "",
            capacity = 0,
            reputation = 0,
            players = emptyList(),
            juniors = emptyList(),
            legacyAid = -7,
            legacySid = 11,
            legacyTid = 22,
            legacyVid = 33,
            legacyId = 44,
            legacyValid = false,
        )

        val result = LegacyManagedClubSourceIdentityProjection.from(team)

        assertEquals(-7, result.legacyAid)
        assertEquals(11, result.legacySid)
        assertEquals(22, result.legacyTid)
        assertEquals(33, result.legacyVid)
        assertEquals(44, result.legacyId)
        assertFalse(result.legacyValid)
    }
}

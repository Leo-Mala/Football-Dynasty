package com.leomala.footballdynasty.domain.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class LegacyManagerSwapRuleTest {
    @Test
    fun capturesBothClubsThenRunsBothDeparturesBeforeEitherArrival() {
        val clubs = mutableMapOf<String, String?>("a" to "club-a", "b" to "club-b")
        val events = mutableListOf<String>()

        LegacyManagerSwapRule.execute(
            firstManager = "a",
            secondManager = "b",
            currentClubOf = { manager ->
                events += "capture:$manager"
                clubs[manager]
            },
            depart = { manager, replacement ->
                events += "depart:$manager:$replacement"
                clubs[manager] = null
            },
            arrive = { manager, club ->
                events += "arrive:$manager:$club"
                clubs[manager] = club
            },
        )

        assertEquals(
            listOf(
                "capture:a",
                "capture:b",
                "depart:a:b",
                "depart:b:a",
                "arrive:a:club-b",
                "arrive:b:club-a",
            ),
            events,
        )
        assertEquals("club-b", clubs["a"])
        assertEquals("club-a", clubs["b"])
    }

    @Test
    fun nullCapturedClubIsNotSilentlyRepaired() {
        val events = mutableListOf<String>()
        LegacyManagerSwapRule.execute(
            firstManager = "a",
            secondManager = "b",
            currentClubOf = { manager -> if (manager == "a") null else "club-b" },
            depart = { manager, replacement -> events += "depart:$manager:$replacement" },
            arrive = { manager, club -> events += "arrive:$manager:$club" },
        )
        assertEquals(
            listOf("depart:a:b", "depart:b:a", "arrive:a:club-b", "arrive:b:null"),
            events,
        )
    }
}

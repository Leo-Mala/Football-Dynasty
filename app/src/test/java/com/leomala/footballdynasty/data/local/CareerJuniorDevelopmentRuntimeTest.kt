package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerJuniorDraftEntity
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerJuniorDevelopmentRuntimeTest {
    @Test
    fun `development persists strict remainder threshold without consuming unrelated draft state`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        seedOwners(database)
        val atExactlyOne = draft(sourceOrdinal = 0, age = 17, potential = 10, ability = 50, remainder = 0.39)
        val aboveOne = draft(sourceOrdinal = 1, age = 17, potential = 10, ability = 60, remainder = 0.40)
        val overTwenty = draft(sourceOrdinal = 2, age = 21, potential = 10, ability = 70, remainder = 0.99)
        database.careerJuniorDraftDao().upsertAll(listOf(atExactlyOne, aboveOne, overTwenty))

        val result = CareerJuniorRuntimeStore(database).progressDevelopment(CAREER, CLUB)
        val persisted = database.careerJuniorDraftDao().listForClub(CAREER, CLUB)

        assertEquals(3, result.size)
        assertEquals(50, persisted[0].legacyO)
        assertEquals(1.0, persisted[0].developmentRemainder, 0.0000001)
        assertEquals(61, persisted[1].legacyO)
        assertEquals(0.01, persisted[1].developmentRemainder, 0.0000001)
        assertEquals(70, persisted[2].legacyO)
        assertEquals(0.99, persisted[2].developmentRemainder, 0.0000001)
        assertEquals(listOf(17, 17, 21), persisted.map { it.legacyC })
        assertEquals(listOf("draft-0", "draft-1", "draft-2"), persisted.map { it.name })
        database.close()
        Unit
    }

    private suspend fun seedOwners(database: FootballDynastyDatabase) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Phase 15 junior development",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.clubDao().upsertAll(listOf(club()))
    }

    private fun draft(
        sourceOrdinal: Int,
        age: Int,
        potential: Int,
        ability: Int,
        remainder: Double,
    ) = CareerJuniorDraftEntity(
        careerId = CAREER,
        clubId = CLUB,
        sourceOrdinal = sourceOrdinal,
        legacyN = potential,
        legacyB = false,
        legacyC = age,
        legacyE = sourceOrdinal.coerceAtMost(4),
        legacyJ = 0,
        legacyL = 0,
        legacyD = 0,
        name = "draft-$sourceOrdinal",
        legacyG = 0,
        legacyF = 1,
        legacyO = ability,
        legacyM = 0,
        legacyH = 500,
        legacyI = 500,
        developmentRemainder = remainder,
    )

    private fun club() = ClubEntity(
        id = CLUB,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "fixture.ban",
        name = "Fixture",
        country = 0,
        state = 0,
        level = 1,
        stadium = "Fixture",
        capacity = 1,
        reputation = 1,
        primaryColor = "000000",
        secondaryColor = "ffffff",
        coach = "Fixture",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 1,
        legacyValid = true,
    )

    private companion object {
        const val CAREER = "career-phase15-junior-development"
        const val CLUB = "club-phase15-junior-development"
    }
}

package com.leomala.footballdynasty.application.career

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeState
import com.leomala.footballdynasty.data.local.CareerStadiumRuntimeStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.entity.CareerStadiumConstructionEntity
import com.leomala.footballdynasty.data.local.entity.ClubEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerStadiumCatalogStoreTest {
    @Test
    fun `stadium projection uses persisted four-sector runtime for managed club`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", "Arena A", 50_000),
                    club(CLUB_B, "Club B", "Arena B", 60_000),
                )
            )
            createCareer(database)
            CareerStadiumRuntimeStore(database).materialize(
                careerId = CAREER_A,
                clubId = CLUB_A,
                state = CareerStadiumRuntimeState(listOf(1_500, 7_410, 900, 90)),
            )

            val stadium = CareerStadiumCatalogStore(database).loadStadium(CAREER_A)

            requireNotNull(stadium)
            assertEquals(CAREER_A, stadium.careerId)
            assertEquals(CLUB_A, stadium.clubId)
            assertEquals("Club A", stadium.clubName)
            assertEquals("Arena A", stadium.stadiumName)
            assertEquals(listOf(1_500, 7_410, 900, 90), stadium.sectorCapacities)
            assertEquals(9_900L, stadium.totalCapacity)
            assertEquals(emptyList<CareerStadiumCatalogStore.ConstructionSnapshot>(), stadium.constructions)
        } finally {
            database.close()
        }
    }

    @Test
    fun `construction projection keeps source order and isolates managed club ownership`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(
                listOf(
                    club(CLUB_A, "Club A", "Arena A", 50_000),
                    club(CLUB_B, "Club B", "Arena B", 60_000),
                )
            )
            createCareer(database)
            CareerStadiumRuntimeStore(database).materialize(
                careerId = CAREER_A,
                clubId = CLUB_A,
                state = CareerStadiumRuntimeState(listOf(1_500, 7_410, 900, 90)),
            )
            database.careerManagerRuntimeDao().upsertStadiumConstructions(
                listOf(
                    construction(0, CLUB_A, 20_000L, listOf(100, 0, 20, 0)),
                    construction(1, CLUB_B, 30_000L, listOf(0, 200, 0, 0)),
                    construction(2, CLUB_A, 40_000L, listOf(0, 0, 0, 10)),
                )
            )

            val stadium = requireNotNull(CareerStadiumCatalogStore(database).loadStadium(CAREER_A))

            assertEquals(
                listOf(
                    CareerStadiumCatalogStore.ConstructionSnapshot(
                        sourceOrdinal = 0,
                        endTimestampMillis = 20_000L,
                        additions = listOf(100, 0, 20, 0),
                    ),
                    CareerStadiumCatalogStore.ConstructionSnapshot(
                        sourceOrdinal = 2,
                        endTimestampMillis = 40_000L,
                        additions = listOf(0, 0, 0, 10),
                    ),
                ),
                stadium.constructions,
            )
        } finally {
            database.close()
        }
    }

    @Test
    fun `unknown migrated construction ownership hides only construction projection`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", "Arena A", 50_000)))
            createCareer(database)
            CareerStadiumRuntimeStore(database).materialize(
                careerId = CAREER_A,
                clubId = CLUB_A,
                state = CareerStadiumRuntimeState(listOf(1_500, 7_410, 900, 90)),
            )
            database.careerManagerRuntimeDao().upsertStadiumConstruction(
                construction(0, null, 20_000L, listOf(100, 0, 0, 0))
            )

            val stadium = requireNotNull(CareerStadiumCatalogStore(database).loadStadium(CAREER_A))

            assertEquals(listOf(1_500, 7_410, 900, 90), stadium.sectorCapacities)
            assertEquals(9_900L, stadium.totalCapacity)
            assertNull(stadium.constructions)
        } finally {
            database.close()
        }
    }

    @Test
    fun `missing materialized runtime fails closed instead of rebuilding aggregate capacity`() = runBlocking {
        val database = database()
        try {
            database.clubDao().upsertAll(listOf(club(CLUB_A, "Club A", "Arena A", 50_000)))
            createCareer(database)

            assertNull(CareerStadiumCatalogStore(database).loadStadium(CAREER_A))
            assertNull(CareerStadiumCatalogStore(database).loadStadium("missing"))
        } finally {
            database.close()
        }
    }

    private suspend fun createCareer(database: FootballDynastyDatabase) {
        CareerEntryCommandStore(database, clockMillis = { 100L }).createCareer(
            careerId = CAREER_A,
            displayName = "Career A",
            seed = 7L,
            managedClubId = CLUB_A,
        )
    }

    private fun construction(
        sourceOrdinal: Int,
        ownerClubId: String?,
        endTimestampMillis: Long,
        additions: List<Int>,
    ): CareerStadiumConstructionEntity {
        require(additions.size == 4)
        return CareerStadiumConstructionEntity(
            careerId = CAREER_A,
            sourceOrdinal = sourceOrdinal,
            stadiumCode = 77 + sourceOrdinal,
            endTimestampMillis = endTimestampMillis,
            addition0 = additions[0],
            addition1 = additions[1],
            addition2 = additions[2],
            addition3 = additions[3],
            ownerClubId = ownerClubId,
        )
    }

    private fun club(id: String, name: String, stadium: String, capacity: Int) = ClubEntity(
        id = id,
        dataVersion = 1,
        importScope = null,
        sourceFileRef = "teams/$id.ban",
        name = name,
        country = 11,
        state = 0,
        level = 1,
        stadium = stadium,
        capacity = capacity,
        reputation = 0,
        primaryColor = "",
        secondaryColor = "",
        coach = "",
        coachCountry = 0,
        baseColor = 0,
        legacyAid = 0,
        legacySid = 0,
        legacyTid = 0,
        legacyVid = 0,
        legacyId = 0,
        legacyValid = true,
    )

    private fun database(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private companion object {
        const val CAREER_A = "career-a"
        const val CLUB_A = "club-a"
        const val CLUB_B = "club-b"
    }
}

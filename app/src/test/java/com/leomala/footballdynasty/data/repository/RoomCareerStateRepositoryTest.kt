package com.leomala.footballdynasty.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.application.career.CareerSimulationCoordinator
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.career.CareerCommand
import com.leomala.footballdynasty.domain.career.CareerFingerprint
import com.leomala.footballdynasty.domain.career.CareerStateFactory
import com.leomala.footballdynasty.domain.career.LegacyCalendarRules
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomCareerStateRepositoryTest {
    private lateinit var database: FootballDynastyDatabase
    private lateinit var repository: RoomCareerStateRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        RoomCareerRepository(database) { 100L }.save(
            Career(
                id = "career-core",
                displayName = "Technical Phase 4 career",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
            )
        )
        repository = RoomCareerStateRepository(database) { 200L }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `career state round trip preserves exact fingerprint`() = runBlocking {
        val initial = CareerStateFactory.create("career-core", seed = 42L)
        val saved = repository.save(initial)
        val loaded = requireNotNull(repository.findById(initial.id))

        assertEquals(initial, saved)
        assertEquals(initial, loaded)
        assertEquals(CareerFingerprint.of(initial), CareerFingerprint.of(loaded))
    }

    @Test
    fun `season transition persists exact state`() = runBlocking {
        repository.save(CareerStateFactory.create("career-core", seed = 42L))
        val coordinator = CareerSimulationCoordinator(repository)
        val transition = coordinator.apply("career-core", CareerCommand.TransitionSeason)
        val loaded = requireNotNull(repository.findById("career-core"))

        assertEquals(2, loaded.season.number)
        assertEquals(2027, loaded.season.year)
        assertEquals(LegacyCalendarRules.firstSundayOfJanuaryIndex(2027), loaded.calendar.currentDayIndex)
        assertEquals(transition.state, loaded)
        assertEquals(CareerFingerprint.of(transition.state), CareerFingerprint.of(loaded))
    }

    @Test
    fun `concurrent advances are serialized without lost update`() = runBlocking {
        val initial = CareerStateFactory.create("career-core", seed = 42L)
        repository.save(initial)
        val coordinator = CareerSimulationCoordinator(repository)

        listOf(
            async { coordinator.apply("career-core", CareerCommand.AdvanceOneDay) },
            async { coordinator.apply("career-core", CareerCommand.AdvanceOneDay) },
        ).awaitAll()

        val loaded = requireNotNull(repository.findById("career-core"))
        assertEquals(initial.calendar.currentDayIndex + 2, loaded.calendar.currentDayIndex)
    }
}

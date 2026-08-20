package com.leomala.footballdynasty.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.domain.model.Career
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomCareerRepositoryTest {
    private lateinit var database: FootballDynastyDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `career repository round trip preserves metadata`() = runBlocking {
        var now = 100L
        val repository = RoomCareerRepository(database) { ++now }
        val initial = Career(
            id = "career-technical-probe",
            displayName = "Technical career probe",
            legacyMetadataFingerprint = "metadata-probe",
            legacyCareerFingerprint = null,
        )

        assertNull(repository.findById(initial.id))
        assertEquals(initial, repository.save(initial))
        val firstEntity = requireNotNull(database.careerMetadataDao().findById(initial.id))
        assertEquals(101L, firstEntity.createdAtEpochMillis)
        assertEquals(101L, firstEntity.updatedAtEpochMillis)

        val updated = initial.copy(
            displayName = "Technical career probe updated",
            legacyCareerFingerprint = "career-probe",
        )
        assertEquals(updated, repository.save(updated))
        val secondEntity = requireNotNull(database.careerMetadataDao().findById(initial.id))
        assertEquals(101L, secondEntity.createdAtEpochMillis)
        assertEquals(102L, secondEntity.updatedAtEpochMillis)
        assertEquals(updated, repository.findById(initial.id))
        assertEquals(listOf(updated), repository.all())
    }
}

package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.leomala.footballdynasty.data.local.entity.CareerMetadataEntity
import com.leomala.footballdynasty.data.local.entity.CareerPlayerRuntimeEntity
import com.leomala.footballdynasty.domain.manager.LegacyContractDateWriteRule
import com.leomala.footballdynasty.domain.manager.LegacyContractWriteInvocation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CareerContractDateWriteStoreTest {
    @Test
    fun `renewal date write extends later contract and survives database reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "marco-b-contract-date-reopen"
        context.deleteDatabase(name)
        var database = fileDatabase(context, name)
        seed(database, contractEndMillis = 20_000L)
        var store = CareerContractDateWriteStore(database)

        val after = store.commit(
            careerId = CAREER,
            playerId = PLAYER,
            currentCareerTimestampMillis = 10_000L,
            expectedContractEndMillis = 20_000L,
            invocation = LegacyContractWriteInvocation(180, false),
        )
        assertEquals(20_000L + (180L * LegacyContractDateWriteRule.DAY_MILLIS), after)

        database.close()
        database = fileDatabase(context, name)
        store = CareerContractDateWriteStore(database)
        val reopened = requireNotNull(database.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER))
        assertEquals(after, reopened.contractEndEpochMillis)

        // A second exact write proves the store continues from the persisted value, not from old input.
        val second = store.commit(
            careerId = CAREER,
            playerId = PLAYER,
            currentCareerTimestampMillis = 30_000L,
            expectedContractEndMillis = after,
            invocation = LegacyContractWriteInvocation(365, false),
        )
        assertEquals(after + (365L * LegacyContractDateWriteRule.DAY_MILLIS), second)

        database.close()
        context.deleteDatabase(name)
        Unit
    }

    @Test
    fun `stale expected contract end fails without changing persisted value`() = runBlocking {
        val database = inMemoryDatabase()
        seed(database, contractEndMillis = 50_000L)
        val store = CareerContractDateWriteStore(database)

        val error = runCatching {
            store.commit(
                careerId = CAREER,
                playerId = PLAYER,
                currentCareerTimestampMillis = 10_000L,
                expectedContractEndMillis = 40_000L,
                invocation = LegacyContractWriteInvocation(730, false),
            )
        }.exceptionOrNull()
        assertNotNull(error)
        assertEquals(
            50_000L,
            requireNotNull(database.careerPlayerRuntimeDao().findRuntime(CAREER, PLAYER)).contractEndEpochMillis,
        )
        database.close()
        Unit
    }

    @Test
    fun `true invocation ignores later existing contract exactly like legacy method`() = runBlocking {
        val database = inMemoryDatabase()
        seed(database, contractEndMillis = 99_000L)
        val store = CareerContractDateWriteStore(database)

        val after = store.commit(
            careerId = CAREER,
            playerId = PLAYER,
            currentCareerTimestampMillis = 10_000L,
            expectedContractEndMillis = 99_000L,
            invocation = LegacyContractWriteInvocation(1, true),
        )
        assertEquals(10_000L + LegacyContractDateWriteRule.DAY_MILLIS, after)
        database.close()
        Unit
    }

    private suspend fun seed(database: FootballDynastyDatabase, contractEndMillis: Long) {
        database.careerMetadataDao().upsert(
            CareerMetadataEntity(
                id = CAREER,
                dataVersion = 1,
                displayName = "Contract write",
                legacyMetadataFingerprint = null,
                legacyCareerFingerprint = null,
                createdAtEpochMillis = 1L,
                updatedAtEpochMillis = 1L,
            )
        )
        database.careerPlayerRuntimeDao().upsertRuntime(runtime(contractEndMillis))
    }

    private fun runtime(contractEndMillis: Long) = CareerPlayerRuntimeEntity(
        careerId = CAREER,
        playerId = PLAYER,
        sourceType = CareerPlayerRuntimeStore.SOURCE_CANONICAL,
        stateVersion = CareerPlayerRuntimeStore.RUNTIME_STATE_VERSION,
        age = 25,
        overall = 80,
        marketValue = 1_000,
        star = false,
        worldTop = false,
        legacyHash = 11,
        legacyGeneratedO = 7,
        legacyCreatedYear = 0,
        contractEndEpochMillis = contractEndMillis,
        legacyPreviousMarketValue = 0,
        legacyQ = false,
        legacyX = true,
        legacyY = false,
        legacyZ = true,
        energy = 100,
        injuryUntilEpochDay = 0L,
    )

    private fun inMemoryDatabase(): FootballDynastyDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FootballDynastyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun fileDatabase(context: Context, name: String): FootballDynastyDatabase =
        Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()

    private companion object {
        const val CAREER = "career-contract-write"
        const val PLAYER = "player-contract-write"
    }
}

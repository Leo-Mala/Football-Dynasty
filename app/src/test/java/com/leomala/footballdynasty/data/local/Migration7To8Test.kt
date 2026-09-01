package com.leomala.footballdynasty.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration7To8Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration to 8 adds empty fail closed stadium runtime`() {
        val name = "marco-b-migration-7-8"
        var db = helper.createDatabase(name, 7)
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            8,
            true,
            Phase13StadiumRuntimeMigration.MIGRATION_7_8,
        )
        db.query("SELECT COUNT(*) FROM career_stadium_runtime").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.close()
    }
}

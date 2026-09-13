package com.leomala.footballdynasty.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class Migration18To19Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FootballDynastyDatabase::class.java,
    )

    @Test
    fun `migration 18 to 19 creates empty discipline owner without fabricated backfill`() {
        val name = "phase17-migration-18-19-discipline"
        var db = helper.createDatabase(name, 18)
        db.close()

        db = helper.runMigrationsAndValidate(
            name,
            19,
            true,
            Phase17CompetitionDisciplineMigration.MIGRATION_18_19,
        )
        db.query("SELECT COUNT(*) FROM career_player_competition_discipline").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        db.close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val reopened = Room.databaseBuilder(context, FootballDynastyDatabase::class.java, name)
            .allowMainThreadQueries()
            .addMigrations(*FootballDynastyMigrations.ALL)
            .build()
        reopened.openHelper.writableDatabase.query(
            "SELECT COUNT(*) FROM career_player_competition_discipline"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        reopened.close()
        context.deleteDatabase(name)
    }
}

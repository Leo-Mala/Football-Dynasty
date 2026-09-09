package com.leomala.footballdynasty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.leomala.footballdynasty.application.career.CareerCalendarCatalogStore
import com.leomala.footballdynasty.application.career.CareerCalendarCommandStore
import com.leomala.footballdynasty.application.career.CareerCompetitionCatalogStore
import com.leomala.footballdynasty.application.career.CareerEntryCatalogStore
import com.leomala.footballdynasty.application.career.CareerFinanceCatalogStore
import com.leomala.footballdynasty.application.career.CareerFinanceCommandStore
import com.leomala.footballdynasty.application.career.CareerJuniorCatalogStore
import com.leomala.footballdynasty.application.career.CareerLineupInputCatalogStore
import com.leomala.footballdynasty.application.career.CareerSquadCatalogStore
import com.leomala.footballdynasty.application.career.CareerStadiumCatalogStore
import com.leomala.footballdynasty.data.local.FootballDynastyDatabase
import com.leomala.footballdynasty.data.local.FootballDynastyDatabaseFactory
import com.leomala.footballdynasty.ui.entry.CareerEntryFlowCoordinator
import com.leomala.footballdynasty.ui.entry.LocalCareerFinanceCommandStore
import com.leomala.footballdynasty.ui.entry.Phase17CareerEntryScreen

class MainActivity : ComponentActivity() {
    private lateinit var database: FootballDynastyDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = FootballDynastyDatabaseFactory.create(applicationContext)
        val catalogStore = CareerEntryCatalogStore(
            careerMetadataDao = database.careerMetadataDao(),
            careerCoreStateDao = database.careerCoreStateDao(),
            clubDao = database.clubDao(),
        )
        val entryCoordinator = CareerEntryFlowCoordinator(catalogStore)
        val squadCatalogStore = CareerSquadCatalogStore(database)
        val lineupCatalogStore = CareerLineupInputCatalogStore(database)
        val juniorCatalogStore = CareerJuniorCatalogStore(database)
        val competitionCatalogStore = CareerCompetitionCatalogStore(database)
        val calendarCatalogStore = CareerCalendarCatalogStore(database)
        val calendarCommandStore = CareerCalendarCommandStore(database)
        val stadiumCatalogStore = CareerStadiumCatalogStore(database)
        val financeCatalogStore = CareerFinanceCatalogStore(database)
        val financeCommandStore = CareerFinanceCommandStore(database)

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompositionLocalProvider(LocalCareerFinanceCommandStore provides financeCommandStore) {
                        Phase17CareerEntryScreen(
                            coordinator = entryCoordinator,
                            squadCatalogStore = squadCatalogStore,
                            lineupCatalogStore = lineupCatalogStore,
                            juniorCatalogStore = juniorCatalogStore,
                            competitionCatalogStore = competitionCatalogStore,
                            calendarCatalogStore = calendarCatalogStore,
                            calendarCommandStore = calendarCommandStore,
                            stadiumCatalogStore = stadiumCatalogStore,
                            financeCatalogStore = financeCatalogStore,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (::database.isInitialized) {
            database.close()
        }
        super.onDestroy()
    }
}

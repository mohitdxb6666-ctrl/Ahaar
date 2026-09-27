package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AiNutritionLabScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.ClickToKnowCaloriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FoodSearchScreen
import com.example.ui.theme.AhaarTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AhaarTheme {
                AhaarApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AhaarApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 4.dp,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.DASHBOARD) Icons.Filled.DateRange else Icons.Outlined.DateRange,
                            contentDescription = "Diary"
                        )
                    },
                    label = { Text("Diary") },
                    modifier = Modifier.testTag("nav_diary")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.CLICK_TO_KNOW_CALORIES,
                    onClick = { viewModel.navigateTo(AppScreen.CLICK_TO_KNOW_CALORIES) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.CLICK_TO_KNOW_CALORIES) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt,
                            contentDescription = "Click to know calories"
                        )
                    },
                    label = { Text("Calories") },
                    modifier = Modifier.testTag("nav_click_to_know_calories")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.FOOD_SEARCH,
                    onClick = { viewModel.navigateTo(AppScreen.FOOD_SEARCH) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.FOOD_SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                            contentDescription = "Search"
                        )
                    },
                    label = { Text("Search") },
                    modifier = Modifier.testTag("nav_search")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.AI_LAB,
                    onClick = { viewModel.navigateTo(AppScreen.AI_LAB) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.AI_LAB) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Lab"
                        )
                    },
                    label = { Text("AI Lab") },
                    modifier = Modifier.testTag("nav_ai_lab")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.ANALYTICS,
                    onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AppScreen.ANALYTICS) Icons.Filled.TrendingUp else Icons.Outlined.TrendingUp,
                            contentDescription = "Insights"
                        )
                    },
                    label = { Text("Insights") },
                    modifier = Modifier.testTag("nav_insights")
                )
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.CLICK_TO_KNOW_CALORIES -> ClickToKnowCaloriesScreen(viewModel = viewModel)
                AppScreen.FOOD_SEARCH -> FoodSearchScreen(viewModel = viewModel)
                AppScreen.AI_LAB -> AiNutritionLabScreen(viewModel = viewModel)
                AppScreen.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
            }
        }
    }
}

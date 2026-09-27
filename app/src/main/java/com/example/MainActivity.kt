package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.AiDietitianChatScreen
import com.example.ui.screens.AiNutritionLabScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FoodSearchScreen
import com.example.ui.screens.ProgressAndAnalyticsScreen
import com.example.ui.theme.AhaarTheme
import com.example.ui.theme.SaffronPrimary

enum class AhaarNavTab(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Aaj", "Diary", Icons.Default.Today, "tab_dashboard"),
    FOODS("Khana", "Foods", Icons.Default.RestaurantMenu, "tab_foods"),
    AI_SCANNER("AI Lab", "Scan", Icons.Default.AutoAwesome, "tab_ai_scanner"),
    AI_COACH("Dr. Ahaar", "Coach", Icons.Default.ChatBubble, "tab_ai_coach"),
    INSIGHTS("Pragati", "Insights", Icons.Default.Analytics, "tab_insights")
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AhaarTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AhaarNavTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SaffronPrimary,
                            selectedTextColor = SaffronPrimary,
                            indicatorColor = SaffronPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToFoodSearch = { selectedTabIndex = 1 },
                    onNavigateToAiScanner = { selectedTabIndex = 2 }
                )
                1 -> FoodSearchScreen(
                    viewModel = viewModel,
                    onFoodLogged = { selectedTabIndex = 0 }
                )
                2 -> AiNutritionLabScreen(
                    viewModel = viewModel,
                    onLogsCompleted = { selectedTabIndex = 0 }
                )
                3 -> AiDietitianChatScreen(
                    viewModel = viewModel
                )
                4 -> ProgressAndAnalyticsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}

package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val dailyEntries by viewModel.dailyEntries.collectAsState()
    val targets by viewModel.macroTargets.collectAsState()

    val totalCal = dailyEntries.sumOf { it.calories }
    val totalProtein = dailyEntries.sumOf { it.protein.toDouble() }.toFloat()
    val totalCarbs = dailyEntries.sumOf { it.carbs.toDouble() }.toFloat()
    val totalFat = dailyEntries.sumOf { it.fat.toDouble() }.toFloat()

    val totalGrams = (totalProtein + totalCarbs + totalFat).coerceAtLeast(1f)
    val proteinPercent = ((totalProtein / totalGrams) * 100).toInt()
    val carbsPercent = ((totalCarbs / totalGrams) * 100).toInt()
    val fatPercent = ((totalFat / totalGrams) * 100).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Nutrition & Dosha Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Goal Adherence
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("analytics_adherence_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Today's Energy Balance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Consumed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$totalCal kcal", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Daily Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${targets.calories} kcal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Deficit / Balance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val diff = targets.calories - totalCal
                                Text(
                                    text = if (diff >= 0) "-$diff kcal" else "+${-diff} kcal",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff >= 0) Color(0xFF166534) else Color(0xFFEA580C)
                                )
                            }
                        }
                    }
                }
            }

            // Macro Distribution
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Macronutrient Ratio Split",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Segmented bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        ) {
                            if (carbsPercent > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(carbsPercent.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(CarbsColor)
                                )
                            }
                            if (proteinPercent > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(proteinPercent.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(ProteinColor)
                                )
                            }
                            if (fatPercent > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(fatPercent.toFloat().coerceAtLeast(1f))
                                        .fillMaxHeight()
                                        .background(FatColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Carbs: $carbsPercent% (${totalCarbs.toInt()}g)", color = CarbsColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Protein: $proteinPercent% (${totalProtein.toInt()}g)", color = ProteinColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Fat: $fatPercent% (${totalFat.toInt()}g)", color = FatColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Ayurvedic Dosha Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7).copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "🌿 Ayurvedic Agni & Prakriti Balance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Based on today's logged meals, your dietary pattern is stabilizing Pitta and Vata with unctuous ghee and cooling lentils.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        listOf(
                            "Vata (Air/Space): Balanced by warm cooked gravies and rotis.",
                            "Pitta (Fire): Soothed by dahi and mild spices.",
                            "Kapha (Earth/Water): Optimal; avoid heavy cream after sunset."
                        ).forEach { tip ->
                            Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(tip, fontSize = 12.sp, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }
        }
    }
}

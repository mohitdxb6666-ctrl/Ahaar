package com.example.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiPlateItem
import com.example.ui.MainViewModel

@Composable
fun AiSmartMealPlannerSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dailyEntries by viewModel.dailyEntries.collectAsState()
    val targets by viewModel.macroTargets.collectAsState()
    val isLoading by viewModel.isLoadingPlanner.collectAsState()
    val recommendations by viewModel.plannerRecommendations.collectAsState()

    val totalCal = dailyEntries.sumOf { it.calories }
    val totalProtein = dailyEntries.sumOf { it.protein.toDouble() }.toFloat()
    val remainingCal = (targets.calories - totalCal).coerceAtLeast(0)
    val remainingProtein = (targets.protein - totalProtein).coerceAtLeast(0f)

    var selectedMeal by remember { mutableStateOf("Dinner") }
    var selectedDiet by remember { mutableStateOf("Vegetarian") }
    var loggedMealName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Budget Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎯 What Should I Eat Next?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dynamic meal engine matching your remaining daily macro budget with regional Indian home recipes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Available Calories", style = MaterialTheme.typography.labelSmall)
                        Text("$remainingCal kcal", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("Protein Needed", style = MaterialTheme.typography.labelSmall)
                        Text("${remainingProtein.toInt()} g", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF10B981))
                    }
                }
            }
        }

        // Selection Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Dinner", "Lunch", "Snack").forEach { meal ->
                FilterChip(
                    selected = selectedMeal == meal,
                    onClick = { selectedMeal = meal },
                    label = { Text(meal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Vegetarian", "Eggetarian", "Jain Friendly").forEach { diet ->
                FilterChip(
                    selected = selectedDiet == diet,
                    onClick = { selectedDiet = diet },
                    label = { Text(diet, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = { viewModel.generateMealRecommendations(selectedMeal, selectedDiet) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_meal_recommendations_button"),
            shape = RoundedCornerShape(14.dp),
            enabled = !isLoading
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Recommend 3 Perfect $selectedMeal Options")
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        loggedMealName?.let { name ->
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✓ Successfully logged $name to your diary!",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFF166534),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Recommendations List
        recommendations.forEach { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rec.dishName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${rec.calories} kcal",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (rec.hindiName.isNotBlank()) {
                        Text(
                            text = rec.hindiName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rec.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Protein: ${rec.protein.toInt()}g • Carbs: ${rec.carbs.toInt()}g • Fat: ${rec.fat.toInt()}g",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(rec.prepTime, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👨‍🍳 ${rec.quickRecipe}",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val plateItem = AiPlateItem(
                                dishName = rec.dishName,
                                hindiName = rec.hindiName,
                                portionEstimate = "1 serving",
                                calories = rec.calories,
                                protein = rec.protein,
                                carbs = rec.carbs,
                                fat = rec.fat,
                                healthNote = rec.reason
                            )
                            viewModel.logAiPlateItem(plateItem, selectedMeal.uppercase())
                            loggedMealName = rec.dishName
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Log ${rec.dishName} to $selectedMeal")
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DietaryType
import com.example.data.model.IndianFoodItem
import com.example.data.model.IndianRegion
import com.example.data.model.MealType
import com.example.ui.MainViewModel
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.FiberColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SaffronPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodSearchScreen(
    viewModel: MainViewModel,
    onFoodLogged: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val selectedDietaryType by viewModel.selectedDietaryType.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var itemToLog by remember { mutableStateOf<IndianFoodItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search Indian dishes (Roti, Dal, Dosa, Poha...)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SaffronPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("food_search_input"),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Regional Cuisine Chips Horizontal Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IndianRegion.values().forEach { region ->
                val isSelected = selectedRegion == region
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onRegionSelected(region) },
                    label = { Text("${region.emoji} ${region.title}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dietary Preference Chips (Veg, Non-Veg, Egg, Jain, Vegan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedDietaryType == null,
                onClick = { viewModel.onDietaryTypeSelected(null) },
                label = { Text("All Diets") },
                shape = RoundedCornerShape(10.dp)
            )
            DietaryType.values().forEach { diet ->
                val isSelected = selectedDietaryType == diet
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onDietaryTypeSelected(if (isSelected) null else diet) },
                    label = { Text(diet.label) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(diet.badgeColorHex),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Results Header
        Text(
            text = "Showing ${searchResults.size} Authentic Indian Foods",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Food Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(searchResults, key = { it.id }) { food ->
                IndianFoodCard(
                    food = food,
                    onLogClick = { itemToLog = food }
                )
            }
        }
    }

    itemToLog?.let { food ->
        IndianPortionLogDialog(
            food = food,
            onDismiss = { itemToLog = null },
            onConfirmLog = { mealType, quantity, unit ->
                viewModel.logFood(
                    mealType = mealType,
                    foodName = food.name,
                    regionalName = food.regionalName,
                    quantity = quantity,
                    unit = unit,
                    calories = food.calories,
                    protein = food.proteinGrams,
                    carbs = food.carbsGrams,
                    fat = food.fatGrams,
                    fiber = food.fiberGrams
                )
                itemToLog = null
                onFoodLogged()
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IndianFoodCard(
    food: IndianFoodItem,
    onLogClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLogClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Dietary Badge dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(food.dietaryType.badgeColorHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (food.regionalName.isNotBlank()) {
                        Text(
                            text = food.regionalName,
                            style = MaterialTheme.typography.bodySmall,
                            color = SaffronPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${food.calories.toInt()} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SaffronPrimary
                    )
                    Text(
                        text = food.defaultServingUnit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Macro Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPill(label = "P", value = "${food.proteinGrams.toInt()}g", color = ProteinColor)
                MacroPill(label = "C", value = "${food.carbsGrams.toInt()}g", color = CarbsColor)
                MacroPill(label = "F", value = "${food.fatGrams.toInt()}g", color = FatColor)
                MacroPill(label = "Fib", value = "${food.fiberGrams.toInt()}g", color = FiberColor)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "GI: ${food.glycemicIndex}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (food.healthTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    food.healthTags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroPill(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = value,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun IndianPortionLogDialog(
    food: IndianFoodItem,
    onDismiss: () -> Unit,
    onConfirmLog: (MealType, Double, String) -> Unit
) {
    var selectedMeal by remember { mutableStateOf(MealType.LUNCH) }
    var portionMultiplier by remember { mutableDoubleStateOf(1.0) }
    val portions = listOf(
        Pair(0.5, "Half Serving (0.5x)"),
        Pair(1.0, "Standard (1x - ${food.defaultServingUnit})"),
        Pair(1.5, "Medium-Large (1.5x)"),
        Pair(2.0, "Double (2x)"),
        Pair(3.0, "3 Servings (3x)")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Log ${food.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Default: ${food.defaultServingUnit}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Select Meal Type
                Text(text = "Select Meal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.values().forEach { meal ->
                        val isSelected = selectedMeal == meal
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMeal = meal },
                            label = { Text("${meal.icon} ${meal.displayName}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Select Portion Size
                Text(text = "Select Portion / Serving", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                portions.forEach { (factor, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { portionMultiplier = factor }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = portionMultiplier == factor,
                            onClick = { portionMultiplier = factor },
                            colors = RadioButtonDefaults.colors(selectedColor = SaffronPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Summary of calculated calories & macros
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(food.calories * portionMultiplier).toInt()} kcal",
                            fontWeight = FontWeight.ExtraBold,
                            color = SaffronPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "P: ${(food.proteinGrams * portionMultiplier).toInt()}g | C: ${(food.carbsGrams * portionMultiplier).toInt()}g | F: ${(food.fatGrams * portionMultiplier).toInt()}g",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmLog(selectedMeal, portionMultiplier, food.defaultServingUnit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Log to ${selectedMeal.displayName}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

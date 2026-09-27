package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealLogEntity
import com.example.data.model.MealType
import com.example.ui.MainViewModel
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.FiberColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.WaterColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToFoodSearch: () -> Unit,
    onNavigateToAiScanner: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyLogs by viewModel.currentDayLogs.collectAsState()
    val target by viewModel.currentTarget.collectAsState()

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var selectedMealForAdd by remember { mutableStateOf(MealType.LUNCH) }

    // Calculated daily totals
    val totalCalories = dailyLogs.sumOf { it.calories }
    val totalProtein = dailyLogs.sumOf { it.proteinGrams }
    val totalCarbs = dailyLogs.sumOf { it.carbsGrams }
    val totalFat = dailyLogs.sumOf { it.fatGrams }
    val totalFiber = dailyLogs.sumOf { it.fiberGrams }

    val remainingCalories = maxOf(0, target.targetCalories - totalCalories.toInt())
    val calorieProgress = if (target.targetCalories > 0) {
        (totalCalories.toFloat() / target.targetCalories.toFloat()).coerceIn(0f, 1f)
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Date Header Navigator
        item {
            DateNavigatorHeader(
                currentDate = selectedDate,
                onPreviousDay = { viewModel.changeDate(-1) },
                onNextDay = { viewModel.changeDate(1) }
            )
        }

        // 2. Hero Calorie Balance Ring Card
        item {
            HeroCalorieCard(
                targetCalories = target.targetCalories,
                consumedCalories = totalCalories.toInt(),
                remainingCalories = remainingCalories,
                progress = calorieProgress
            )
        }

        // 3. Macronutrient Cards (Protein, Carbs, Fat, Fiber)
        item {
            MacroGrid(
                proteinConsumed = totalProtein.toInt(),
                proteinTarget = target.targetProteinGrams,
                carbsConsumed = totalCarbs.toInt(),
                carbsTarget = target.targetCarbsGrams,
                fatConsumed = totalFat.toInt(),
                fatTarget = target.targetFatGrams,
                fiberConsumed = totalFiber.toInt(),
                fiberTarget = target.targetFiberGrams
            )
        }

        // 4. Indian Hydration Tracker Card
        item {
            WaterTrackerCard(
                waterConsumedMl = target.waterConsumedMl,
                waterTargetMl = target.targetWaterMl,
                onAddWater = { viewModel.logWater(it) }
            )
        }

        // 5. Intermittent Fasting Quick Status Banner
        item {
            FastingQuickBanner(
                isFastingActive = target.isFastingActive,
                fastStartTimeMillis = target.fastStartTimeMillis,
                goalHours = target.fastingGoalHours,
                onToggleFasting = { viewModel.toggleFasting() }
            )
        }

        // 5b. Gemini AI Intelligence Quick Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAiScanner() }
                    .testTag("dashboard_ai_intelligence_hub"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SaffronPrimary.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SaffronPrimary.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ahaar Gemini AI Lab",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = SaffronPrimary
                            )
                        }
                        Text(
                            text = "Launch AI Tools →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "📸 Photo Plate Scan",
                            "🏨 Restaurant Audit",
                            "🎯 What To Eat Next?",
                            "🍛 Thali Glycemic",
                            "🏷️ Nutri-Score"
                        ).forEach { feature ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onNavigateToAiScanner() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meals & Ahaar Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToAiScanner,
                    modifier = Modifier.testTag("ai_meal_scan_button")
                ) {
                    Text("✨ AI Meal Scan")
                }
            }
        }

        // 7. Meal Cards (Breakfast, Lunch, Snacks, Dinner, Extra)
        items(MealType.values()) { mealType ->
            val logsForMeal = dailyLogs.filter { it.mealType == mealType.name }
            MealSectionCard(
                mealType = mealType,
                loggedItems = logsForMeal,
                onAddFoodClick = {
                    selectedMealForAdd = mealType
                    showQuickAddDialog = true
                },
                onSearchFoodClick = {
                    onNavigateToFoodSearch()
                },
                onDeleteItem = { logId ->
                    viewModel.deleteLog(logId)
                }
            )
        }
    }

    if (showQuickAddDialog) {
        QuickAddFoodDialog(
            mealType = selectedMealForAdd,
            onDismiss = { showQuickAddDialog = false },
            onAddCustom = { name, quantity, unit, cal, p, c, f, fib ->
                viewModel.logFood(
                    mealType = selectedMealForAdd,
                    foodName = name,
                    quantity = quantity,
                    unit = unit,
                    calories = cal,
                    protein = p,
                    carbs = c,
                    fat = f,
                    fiber = fib
                )
                showQuickAddDialog = false
            },
            onOpenSearch = {
                showQuickAddDialog = false
                onNavigateToFoodSearch()
            }
        )
    }
}

@Composable
fun DateNavigatorHeader(
    currentDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousDay, modifier = Modifier.testTag("prev_day_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = SaffronPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onNextDay, modifier = Modifier.testTag("next_day_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
            }
        }
    }
}

@Composable
fun HeroCalorieCard(
    targetCalories: Int,
    consumedCalories: Int,
    remainingCalories: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "CalorieProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Arc
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                val primaryColor = SaffronPrimary
                val trackColor = MaterialTheme.colorScheme.surfaceVariant

                Canvas(modifier = Modifier.size(130.dp)) {
                    // Background track
                    drawCircle(
                        color = trackColor,
                        radius = size.minDimension / 2 - 8.dp.toPx(),
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFFFF9800), SaffronPrimary, Color(0xFFD84315), Color(0xFFFF9800))
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "$consumedCalories",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "kcal eaten",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right side Metrics (Budget & Remaining)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricRow(
                    label = "Target Goal",
                    value = "$targetCalories kcal",
                    color = MaterialTheme.colorScheme.onSurface
                )
                MetricRow(
                    label = "Remaining",
                    value = "$remainingCalories kcal",
                    color = SaffronPrimary,
                    isBold = true
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SaffronPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = if (consumedCalories > targetCalories) "⚠️ Over target by ${consumedCalories - targetCalories} kcal"
                    else "✅ ${(progress * 100).toInt()}% of daily energy",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (consumedCalories > targetCalories) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun MacroGrid(
    proteinConsumed: Int,
    proteinTarget: Int,
    carbsConsumed: Int,
    carbsTarget: Int,
    fatConsumed: Int,
    fatTarget: Int,
    fiberConsumed: Int,
    fiberTarget: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MacroCard(
            modifier = Modifier.weight(1f),
            title = "Protein",
            consumed = proteinConsumed,
            target = proteinTarget,
            unit = "g",
            color = ProteinColor
        )
        MacroCard(
            modifier = Modifier.weight(1f),
            title = "Carbs",
            consumed = carbsConsumed,
            target = carbsTarget,
            unit = "g",
            color = CarbsColor
        )
        MacroCard(
            modifier = Modifier.weight(1f),
            title = "Fat",
            consumed = fatConsumed,
            target = fatTarget,
            unit = "g",
            color = FatColor
        )
        MacroCard(
            modifier = Modifier.weight(1f),
            title = "Fiber",
            consumed = fiberConsumed,
            target = fiberTarget,
            unit = "g",
            color = FiberColor
        )
    }
}

@Composable
fun MacroCard(
    modifier: Modifier = Modifier,
    title: String,
    consumed: Int,
    target: Int,
    unit: String,
    color: Color
) {
    val progress = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$consumed / $target$unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun WaterTrackerCard(
    waterConsumedMl: Int,
    waterTargetMl: Int,
    onAddWater: (Int) -> Unit
) {
    val glasses = (waterConsumedMl / 250)
    val targetGlasses = (waterTargetMl / 250)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = WaterColor.copy(alpha = 0.08f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(WaterColor.copy(alpha = 0.3f), WaterColor.copy(alpha = 0.1f)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Opacity,
                        contentDescription = null,
                        tint = WaterColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Hydration / जल",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WaterColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$waterConsumedMl ml / $waterTargetMl ml ($glasses of $targetGlasses glasses)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = { onAddWater(250) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+250ml", fontSize = 12.sp)
                }
                Button(
                    onClick = { onAddWater(500) },
                    colors = ButtonDefaults.buttonColors(containerColor = WaterColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+500ml", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FastingQuickBanner(
    isFastingActive: Boolean,
    fastStartTimeMillis: Long,
    goalHours: Int,
    onToggleFasting: () -> Unit
) {
    val elapsedHours = if (isFastingActive && fastStartTimeMillis > 0) {
        ((System.currentTimeMillis() - fastStartTimeMillis) / (1000.0 * 3600.0)).toFloat()
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = if (isFastingActive) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isFastingActive) "Intermittent Fast Active ($goalHours:8)" else "Fasting Window (${goalHours}h)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isFastingActive) "Elapsed: String.format(%.1fh, $goalHours h goal)"
                        else "Tap Start to track autophagy & digestive rest",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onToggleFasting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFastingActive) Color(0xFFD32F2F) else SaffronPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(if (isFastingActive) "End Fast" else "Start Fast", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MealSectionCard(
    mealType: MealType,
    loggedItems: List<MealLogEntity>,
    onAddFoodClick: () -> Unit,
    onSearchFoodClick: () -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    val totalMealCalories = loggedItems.sumOf { it.calories }.toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Meal Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = mealType.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mealType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = mealType.hindiName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$totalMealCalories kcal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onAddFoodClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Food", tint = SaffronPrimary)
                    }
                }
            }

            // Logged Food Items
            if (loggedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                loggedItems.forEach { item ->
                    LoggedFoodRow(item = item, onDelete = { onDeleteItem(item.id) })
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No items logged yet. Tap + to add Indian dishes or quick calories.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onSearchFoodClick() }
                )
            }
        }
    }
}

@Composable
fun LoggedFoodRow(item: MealLogEntity, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.foodName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${item.quantity} ${item.unit} • P: ${item.proteinGrams.toInt()}g | C: ${item.carbsGrams.toInt()}g | F: ${item.fatGrams.toInt()}g",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${item.calories.toInt()} kcal",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary
            )
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun QuickAddFoodDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onAddCustom: (name: String, quantity: Double, unit: String, cal: Double, p: Double, c: Double, f: Double, fib: Double) -> Unit,
    onOpenSearch: () -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("katori") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log ${mealType.displayName}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenSearch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🔍 Search 200+ Indian Regional Foods")
                }

                Text(
                    text = "— OR Quick Add Custom Item —",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Dish Name (e.g. 2 Ghee Roti, Dal Tadka)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (katori/roti/plate)") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = caloriesStr,
                        onValueChange = { caloriesStr = it },
                        label = { Text("Calories (kcal)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = foodName.ifBlank { "Custom Food" }
                    val qty = quantityStr.toDoubleOrNull() ?: 1.0
                    val cal = caloriesStr.toDoubleOrNull() ?: 150.0
                    val p = proteinStr.toDoubleOrNull() ?: 4.0
                    val c = carbsStr.toDoubleOrNull() ?: 20.0
                    val f = fatStr.toDoubleOrNull() ?: 5.0
                    onAddCustom(name, qty, unit, cal, p, c, f, 2.0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Log Food")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

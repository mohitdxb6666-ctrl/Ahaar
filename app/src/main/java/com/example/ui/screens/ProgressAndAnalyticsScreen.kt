package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.BasilSecondary
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SaffronPrimary

@Composable
fun ProgressAndAnalyticsScreen(viewModel: MainViewModel) {
    val target by viewModel.currentTarget.collectAsState()
    val allLogs by viewModel.allLogs.collectAsState()

    var showEditGoalsDialog by remember { mutableStateOf(false) }
    var inputWeight by remember { mutableStateOf("${viewModel.userWeightKg}") }

    // Fasting calculation
    val isFasting = target.isFastingActive
    val fastStart = target.fastStartTimeMillis
    val elapsedHours = if (isFasting && fastStart > 0) {
        ((System.currentTimeMillis() - fastStart) / (1000.0 * 3600.0)).toFloat()
    } else 0f
    val goalHours = target.fastingGoalHours
    val fastingProgress = (elapsedHours / goalHours).coerceIn(0f, 1f)

    // Indian BMI calculation (Weight / (Height in m)^2)
    val heightM = viewModel.userHeightCm / 100.0
    val bmi = if (heightM > 0) viewModel.userWeightKg / (heightM * heightM) else 22.0
    val (bmiCategory, bmiColor) = when {
        bmi < 18.5 -> "Underweight" to Color(0xFF2196F3)
        bmi in 18.5..22.9 -> "Normal / Healthy (Indian Standard)" to Color(0xFF4CAF50)
        bmi in 23.0..24.9 -> "Overweight (Indian Threshold)" to Color(0xFFFF9800)
        else -> "Obese (Indian Threshold)" to Color(0xFFE53935)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pragati & Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(onClick = { showEditGoalsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Goals")
                }
            }
        }

        // 1. Intermittent Fasting Tracker Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = SaffronPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Intermittent Fasting (16:8)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFasting) BasilSecondary.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isFasting) "FASTING" else "EATING WINDOW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFasting) BasilSecondary else Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isFasting) "${String.format("%.1f", elapsedHours)}h / ${goalHours}h Completed"
                        else "Ready to start your ${goalHours}-hour digestive rest window",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { if (isFasting) fastingProgress else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SaffronPrimary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when {
                            !isFasting -> "💡 Fasting activates cellular cleansing (Autophagy) and stabilizes insulin."
                            elapsedHours < 12 -> "🔥 Glycogen depletion phase — body is preparing for fat oxidation."
                            elapsedHours in 12.0..14.0 -> "⚡ Fat burning & mild ketosis stage initiated!"
                            else -> "🌟 Full Autophagy & Cellular Repair stage reached!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.toggleFasting() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFasting) Color(0xFFD32F2F) else SaffronPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isFasting) "Stop Fast" else "Start Fasting Timer")
                    }
                }
            }
        }

        // 2. Indian BMI & Weight Tracker
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = SaffronPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weight & Indian BMI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = String.format("%.1f BMI", bmi),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = bmiColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Category: $bmiCategory",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = bmiColor
                    )

                    Text(
                        text = "Note: Indian population cut-off for overweight starts at BMI 23.0 to manage visceral fat risk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { inputWeight = it },
                            label = { Text("Log Current Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val w = inputWeight.toDoubleOrNull()
                                if (w != null && w > 20) {
                                    viewModel.updateWeight(w)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }

        // 3. Indian Metabolic Profile Summary (BMR & TDEE)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Desi Metabolic Baseline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Basal Metabolic Rate (BMR)", style = MaterialTheme.typography.bodySmall)
                            Text("${viewModel.calculateBMR()} kcal/day", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Energy Expenditure (TDEE)", style = MaterialTheme.typography.bodySmall)
                            Text("${viewModel.calculateTDEE()} kcal/day", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SaffronPrimary)
                        }
                    }
                }
            }
        }
    }

    if (showEditGoalsDialog) {
        EditGoalsDialog(
            currentCalories = target.targetCalories,
            currentProtein = target.targetProteinGrams,
            currentCarbs = target.targetCarbsGrams,
            currentFat = target.targetFatGrams,
            currentWater = target.targetWaterMl,
            onDismiss = { showEditGoalsDialog = false },
            onSave = { cal, p, c, f, w ->
                viewModel.updateTargets(cal, p, c, f, w)
                showEditGoalsDialog = false
            }
        )
    }
}

@Composable
fun EditGoalsDialog(
    currentCalories: Int,
    currentProtein: Int,
    currentCarbs: Int,
    currentFat: Int,
    currentWater: Int,
    onDismiss: () -> Unit,
    onSave: (calories: Int, protein: Int, carbs: Int, fat: Int, water: Int) -> Unit
) {
    var calStr by remember { mutableStateOf("$currentCalories") }
    var proteinStr by remember { mutableStateOf("$currentProtein") }
    var carbsStr by remember { mutableStateOf("$currentCarbs") }
    var fatStr by remember { mutableStateOf("$currentFat") }
    var waterStr by remember { mutableStateOf("$currentWater") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Customize Daily Nutrition Goals", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = calStr,
                    onValueChange = { calStr = it },
                    label = { Text("Daily Calorie Target (kcal)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = carbsStr,
                        onValueChange = { carbsStr = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = waterStr,
                        onValueChange = { waterStr = it },
                        label = { Text("Water (ml)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calStr.toIntOrNull() ?: currentCalories
                    val p = proteinStr.toIntOrNull() ?: currentProtein
                    val c = carbsStr.toIntOrNull() ?: currentCarbs
                    val f = fatStr.toIntOrNull() ?: currentFat
                    val w = waterStr.toIntOrNull() ?: currentWater
                    onSave(cal, p, c, f, w)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Save Targets")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

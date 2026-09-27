package com.example.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

@Composable
fun AiThaliSynergySection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isOptimizing by viewModel.isOptimizingThali.collectAsState()
    val thaliResult by viewModel.thaliResult.collectAsState()

    var thaliInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🍱 Desi Thali Glycemic & Synergy Optimizer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Indian thalis combine starches with pulses and fats. Our AI calculates your total Glycemic Load and reveals chronological 'Sequence Eating' to eliminate 35%+ of insulin spikes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input Field
        OutlinedTextField(
            value = thaliInput,
            onValueChange = { thaliInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("thali_components_input"),
            label = { Text("Enter your Thali dishes") },
            placeholder = { Text("e.g. 2 Rotis, 1 Katori Toor Dal, Lauki Sabzi, Dahi, Cucumber Salad") },
            minLines = 3,
            shape = RoundedCornerShape(16.dp)
        )

        // Preset Chips
        Text(
            text = "Or tap a traditional Indian thali:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    thaliInput = "2 Phulkas with Ghee, Yellow Moong Dal, Palak Paneer, 1 Katori Steamed Basmati Rice, Dahi"
                    viewModel.optimizeThali(thaliInput)
                },
                label = { Text("North Ghar Thali", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    thaliInput = "1 cup Sambar, 1 cup Rasam, Beans Poriyal, Curd Rice, 2 Appalams"
                    viewModel.optimizeThali(thaliInput)
                },
                label = { Text("South Indian Meals", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    thaliInput = "1 Bajra Bhakri, Pithla (Besan curry), Thecha (Chili garlic chutney), Masala Chaas"
                    viewModel.optimizeThali(thaliInput)
                },
                label = { Text("Maharashtrian Bhakri", fontSize = 11.sp) }
            )
        }

        Button(
            onClick = {
                if (thaliInput.isNotBlank()) {
                    viewModel.optimizeThali(thaliInput)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_thali_optimizer_button"),
            shape = RoundedCornerShape(14.dp),
            enabled = thaliInput.isNotBlank() && !isOptimizing
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Optimize Glycemic Load & Dosha")
        }

        if (isOptimizing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Result Card
        thaliResult?.let { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("thali_optimizer_result_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = res.thaliName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Glycemic Load", style = MaterialTheme.typography.labelSmall)
                            Text("${res.glycemicLoad}", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFFD97706))
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = res.spikeRisk,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⏱️ Optimal Chronological Eating Order:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF1D4ED8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = res.sequenceEatingAdvice,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("🌿 Ayurvedic Synergy & Absorption Hacks:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    res.ayurvedicSynergyTips.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(tip, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

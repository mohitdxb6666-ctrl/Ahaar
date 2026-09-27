package com.example.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Warning
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
fun AiRestaurantAuditSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isAuditing by viewModel.isAuditingRestaurant.collectAsState()
    val auditResult by viewModel.restaurantAuditResult.collectAsState()

    var orderInput by remember { mutableStateOf("") }

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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Hotel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dining Out & Swiggy/Zomato Auditor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Indian restaurant dishes conceal 3x to 6x more refined oil, butter, and sodium than homestyle cooking. Paste your order to uncover the hidden metabolic impact.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Input Field
        OutlinedTextField(
            value = orderInput,
            onValueChange = { orderInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("restaurant_order_input"),
            label = { Text("What did you order?") },
            placeholder = { Text("e.g. 1 Butter Naan, 1 Dal Makhani, 1 Paneer Tikka Masala, Jeera Rice") },
            minLines = 3,
            shape = RoundedCornerShape(16.dp)
        )

        // Preset Chips
        Text(
            text = "Or tap a common dining preset:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    orderInput = "1 Dal Makhani, 2 Tandoori Butter Rotis, 1 Mixed Veg Curry"
                    viewModel.auditRestaurant(orderInput)
                },
                label = { Text("Dhaba Dinner", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    orderInput = "1 Paneer Tikka Masala, 1 Butter Garlic Naan, 1 Veg Biryani, 1 Boondi Raita"
                    viewModel.auditRestaurant(orderInput)
                },
                label = { Text("Mughlai Feast", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    orderInput = "2 Chole Bhature with Onion Pickle and 1 Sweet Lassi"
                    viewModel.auditRestaurant(orderInput)
                },
                label = { Text("Sunday Brunch", fontSize = 11.sp) }
            )
        }

        Button(
            onClick = {
                if (orderInput.isNotBlank()) {
                    viewModel.auditRestaurant(orderInput)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_restaurant_audit_button"),
            shape = RoundedCornerShape(14.dp),
            enabled = orderInput.isNotBlank() && !isAuditing
        ) {
            Text("Audit Hidden Fats & Sodium")
        }

        if (isAuditing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Audit Results
        auditResult?.let { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restaurant_audit_result_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = res.restaurantType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = if (res.swasthyaScoreOutOf10 >= 6) Color(0xFFDCFCE7) else Color(0xFFFFEDD5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Swasthya: ${res.swasthyaScoreOutOf10}/10",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (res.swasthyaScoreOutOf10 >= 6) Color(0xFF166534) else Color(0xFF9A3412),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Calories", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${res.estimatedTotalCalories} kcal", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Hidden Oil/Butter", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${res.hiddenOilTsp} tsp (~${(res.hiddenOilTsp * 5).toInt()}g)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFEA580C))
                        }
                        Column {
                            Text("Total Protein", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${res.estimatedTotalProtein.toInt()}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF10B981))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(res.sodiumWarning, style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("💡 Practical Damage Control Hacks:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    res.damageControlHacks.forEach { hack ->
                        Text("• $hack", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            res.dishes.forEach { d -> viewModel.logAiPlateItem(d, "DINNER") }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Dishes to Dinner Diary")
                    }
                }
            }
        }
    }
}

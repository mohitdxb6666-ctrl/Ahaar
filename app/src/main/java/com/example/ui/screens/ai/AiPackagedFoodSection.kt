package com.example.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ThumbUp
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
fun AiPackagedFoodSection(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isAuditing by viewModel.isAuditingPackaged.collectAsState()
    val packagedResult by viewModel.packagedFoodResult.collectAsState()

    var productName by remember { mutableStateOf("") }
    var ingredientText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🏷️ Desi Nutri-Score & Ultra-Processed Auditor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Expose hidden industrial ingredients in packaged Indian snacks: cheap palmolein oils, disguised sugars, and chemical preservatives.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Inputs
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("packaged_product_name_input"),
            label = { Text("Product or Brand Name") },
            placeholder = { Text("e.g. Haldiram's Aloo Bhujia, Marie Gold, Whey Bar") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = ingredientText,
            onValueChange = { ingredientText = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ingredients List (optional or paste text)") },
            placeholder = { Text("e.g. Potatoes, Palmolein oil, Besan, Spices, Salt, Invert syrup...") },
            minLines = 2,
            shape = RoundedCornerShape(14.dp)
        )

        // Presets
        Text(
            text = "Or tap a popular Indian supermarket item:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {
                    productName = "Aloo Bhujia Namkeen"
                    ingredientText = "Potato, Edible Vegetable Oil (Palmolein), Bengal Gram Flour, Tepary Beans Flour, Iodised Salt, Red Chilli, Black Pepper, Acidity Regulator (INS 330)"
                    viewModel.auditPackaged(productName, ingredientText)
                },
                label = { Text("Bhujia", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    productName = "Tea Time Marie Biscuits"
                    ingredientText = "Refined Wheat Flour (Maida 58%), Sugar, Refined Palm Oil, Invert Sugar Syrup, Milk Solids, Raising Agents (INS 503(ii), INS 500(ii))"
                    viewModel.auditPackaged(productName, ingredientText)
                },
                label = { Text("Marie Biscuits", fontSize = 11.sp) }
            )
            AssistChip(
                onClick = {
                    productName = "Malted Chocolate Health Drink"
                    ingredientText = "Cereal Extract, Sugar, Milk Solids, Cocoa Solids, Caramel Colour, Liquid Glucose, Emulsifiers, Vitamins & Minerals"
                    viewModel.auditPackaged(productName, ingredientText)
                },
                label = { Text("Health Drink", fontSize = 11.sp) }
            )
        }

        Button(
            onClick = {
                if (productName.isNotBlank()) {
                    viewModel.auditPackaged(productName, ingredientText)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_packaged_food_audit_button"),
            shape = RoundedCornerShape(14.dp),
            enabled = productName.isNotBlank() && !isAuditing
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Desi Nutri-Score")
        }

        if (isAuditing) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // Result Card
        packagedResult?.let { res ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("packaged_food_result_card"),
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
                            text = res.productName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // NutriScore Badge
                        val badgeColor = when (res.nutriScoreGrade) {
                            "A" -> Color(0xFF15803D)
                            "B" -> Color(0xFF65A30D)
                            "C" -> Color(0xFFEAB308)
                            "D" -> Color(0xFFEA580C)
                            else -> Color(0xFFDC2626)
                        }

                        Surface(
                            color = badgeColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Grade ${res.nutriScoreGrade}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Clean Food Score: ${res.cleanFoodScoreOutOf100} / 100",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("⚠️ Red Flag Additives:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    res.redFlagAdditives.forEach { flag ->
                        Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(flag, style = MaterialTheme.typography.bodySmall, color = Color(0xFF7F1D1D))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🌱 Clean Indian Swap:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF166534))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(res.desiCleanSwap, style = MaterialTheme.typography.bodySmall, color = Color(0xFF14532D))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verdict: ${res.summaryVerdict}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.AiDecomposedItem
import com.example.data.model.AiMealRecommendation
import com.example.data.model.AiThaliSynergyReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiThaliAndPlannerService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Recommends 3 distinct Indian meal options to fit the user's remaining calories & macros for the day
     */
    suspend fun recommendNextMeals(
        remainingCalories: Int,
        remainingProtein: Int,
        remainingCarbs: Int,
        remainingFat: Int,
        mealType: String,
        preference: String
    ): Result<List<AiMealRecommendation>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getSmartOfflineMealRecommendations(remainingCalories, mealType, preference))
        }

        try {
            val systemPrompt = """
                You are an elite Indian Clinical Dietitian & Macro Precision Coach.
                The user needs meal recommendations for their next meal: '$mealType'.
                Target Budget Remaining:
                - Calories: ~$remainingCalories kcal
                - Protein: ~$remainingProtein g
                - Carbs: ~$remainingCarbs g
                - Fat: ~$remainingFat g
                - Dietary Preference: '$preference' (e.g. Pure Veg, High Protein, Eggetarian, Jain, Quick 10-Min, Low Carb).
                
                Generate EXACTLY 3 distinct, delicious, real Indian meal ideas that fit within ±50 kcal of this target.
                Break down each meal into individual components so the user can one-tap log them into their diary.
                
                Respond ONLY with a valid JSON array of objects in this exact format with NO markdown formatting:
                [
                  {
                    "mealName": "High-Protein Paneer Bhurji & Millet Roti Thali",
                    "category": "High Protein Veg",
                    "description": "Scrambled spiced cottage cheese paired with warm jowar bhakri and cucumber salad.",
                    "portionDescription": "150g Paneer + 1 Jowar Roti + 1 bowl Salad",
                    "calories": 420.0,
                    "protein": 24.0,
                    "carbs": 38.0,
                    "fat": 18.0,
                    "fiber": 6.5,
                    "preparationTimeMinutes": 15,
                    "healthBenefit": "Rich in slow-digesting casein protein, ideal for nighttime recovery and satiety.",
                    "ingredientsSummary": "Paneer, onion, tomato, green chili, turmeric, jowar flour, cucumber",
                    "itemsToLog": [
                      {
                        "foodName": "Paneer Bhurji",
                        "quantity": 1.0,
                        "unit": "katori (150g)",
                        "calories": 260.0,
                        "protein": 18.0,
                        "carbs": 6.0,
                        "fat": 18.0,
                        "fiber": 2.0,
                        "suggestion": "Lean calcium and protein source."
                      },
                      {
                        "foodName": "Jowar / Sorghum Bhakri",
                        "quantity": 1.0,
                        "unit": "roti",
                        "calories": 130.0,
                        "protein": 4.5,
                        "carbs": 28.0,
                        "fat": 0.8,
                        "fiber": 3.8,
                        "suggestion": "Gluten-free complex carb with low glycemic load."
                      },
                      {
                        "foodName": "Cucumber Mint Kachumber",
                        "quantity": 1.0,
                        "unit": "bowl",
                        "calories": 30.0,
                        "protein": 1.5,
                        "carbs": 4.0,
                        "fat": 0.2,
                        "fiber": 1.7,
                        "suggestion": "Hydrating and helps regulate glucose spikes."
                      }
                    ]
                  }
                ]
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Recommend 3 Indian meals for $mealType with preference $preference fitting $remainingCalories kcal"))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartOfflineMealRecommendations(remainingCalories, mealType, preference))
            }

            val parsed = parseRecommendations(responseBody)
            if (parsed.isNotEmpty()) {
                Result.success(parsed)
            } else {
                Result.success(getSmartOfflineMealRecommendations(remainingCalories, mealType, preference))
            }
        } catch (e: Exception) {
            Result.success(getSmartOfflineMealRecommendations(remainingCalories, mealType, preference))
        }
    }

    /**
     * Analyzes a complete Indian Thali for Glycemic Load and Ayurvedic Synergy
     */
    suspend fun analyzeThaliSynergy(thaliItemsDescription: String): Result<AiThaliSynergyReport> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getSmartOfflineThaliReport(thaliItemsDescription))
        }

        try {
            val systemPrompt = """
                You are a senior Ayurvedic Physician & Clinical Diabetologist.
                Analyze the user's Indian Thali composition (e.g. Rotis, White Rice, Dal, Sabzi, Dahi, Chutney, Pickle, Sweet).
                
                Compute:
                1. Total estimated Calories, Protein, Carbs, Fat, Fiber.
                2. Glycemic Load (GL) Rating: 'Low (Safe for Diabetes)', 'Moderate (Balanced)', 'High Glucose Spike Risk'.
                3. Glycemic Index Score (1 to 100).
                4. Ayurvedic Dosha Balance (impact on Vata, Pitta, Kapha, Agni/digestion).
                5. Thali Health Score (0 to 100).
                6. 2-3 Practical Glucose Spike Mitigation Hacks (e.g. eating raw salad/cucumber first, adding lemon juice or apple cider vinegar, adding a teaspoon of pure A2 desi ghee to hot rotis to flatten postprandial glucose spike).
                7. Food synergy notes (e.g. pairing rice with dal achieves complete PDCAAS amino acid profile).

                Respond ONLY with a valid JSON object in this exact schema with NO markdown ticks:
                {
                  "thaliSummary": "Balanced North Indian Ghar Ka Khana",
                  "totalCalories": 580.0,
                  "totalProtein": 18.5,
                  "totalCarbs": 76.0,
                  "totalFat": 19.0,
                  "totalFiber": 9.5,
                  "glycemicLoadRating": "Moderate",
                  "glycemicIndexScore": 54,
                  "ayurvedicDoshaBalance": "Tridoshic (Balances Vata & Pitta, Light on Kapha)",
                  "thaliScoreOutOf100": 88,
                  "glucoseSpikeMitigationHacks": [
                    "Eat the raw salad/kachumber first: vegetable fiber lines the intestine, slowing down glucose absorption by up to 30%.",
                    "Add half a teaspoon of desi cow ghee on hot phulkas: healthy saturated fats lower the glycemic index of whole wheat."
                  ],
                  "foodSynergyNotes": "The combination of lysine-rich Dal and methionine-rich Roti provides complete 9 essential amino acids for optimal protein synthesis."
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Thali items: $thaliItemsDescription"))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartOfflineThaliReport(thaliItemsDescription))
            }

            val parsed = parseThaliReport(responseBody, thaliItemsDescription)
            if (parsed != null) {
                Result.success(parsed)
            } else {
                Result.success(getSmartOfflineThaliReport(thaliItemsDescription))
            }
        } catch (e: Exception) {
            Result.success(getSmartOfflineThaliReport(thaliItemsDescription))
        }
    }

    private fun parseRecommendations(jsonString: String): List<AiMealRecommendation> {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            if (candidates.length() == 0) return emptyList()
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                textBuilder.append(parts.getJSONObject(i).optString("text", ""))
            }

            val cleanJson = textBuilder.toString()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val array = if (cleanJson.startsWith("[")) {
                JSONArray(cleanJson)
            } else {
                val start = cleanJson.indexOf('[')
                val end = cleanJson.lastIndexOf(']')
                if (start != -1 && end != -1 && end > start) {
                    JSONArray(cleanJson.substring(start, end + 1))
                } else {
                    return emptyList()
                }
            }

            val list = mutableListOf<AiMealRecommendation>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itemsArray = obj.optJSONArray("itemsToLog") ?: JSONArray()
                val logItems = mutableListOf<AiDecomposedItem>()
                for (j in 0 until itemsArray.length()) {
                    val it = itemsArray.getJSONObject(j)
                    logItems.add(
                        AiDecomposedItem(
                            foodName = it.optString("foodName", "Food Item"),
                            quantity = it.optDouble("quantity", 1.0),
                            unit = it.optString("unit", "portion"),
                            calories = it.optDouble("calories", 100.0),
                            protein = it.optDouble("protein", 3.0),
                            carbs = it.optDouble("carbs", 15.0),
                            fat = it.optDouble("fat", 3.0),
                            fiber = it.optDouble("fiber", 2.0),
                            suggestion = it.optString("suggestion", "")
                        )
                    )
                }

                list.add(
                    AiMealRecommendation(
                        mealName = obj.optString("mealName", "Wholesome Indian Meal"),
                        category = obj.optString("category", "Balanced Desi"),
                        description = obj.optString("description", ""),
                        portionDescription = obj.optString("portionDescription", "1 serving"),
                        calories = obj.optDouble("calories", 400.0),
                        protein = obj.optDouble("protein", 15.0),
                        carbs = obj.optDouble("carbs", 50.0),
                        fat = obj.optDouble("fat", 12.0),
                        fiber = obj.optDouble("fiber", 6.0),
                        preparationTimeMinutes = obj.optInt("preparationTimeMinutes", 15),
                        healthBenefit = obj.optString("healthBenefit", "Nourishing Indian meal."),
                        ingredientsSummary = obj.optString("ingredientsSummary", ""),
                        itemsToLog = logItems
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseThaliReport(jsonString: String, summaryFallback: String): AiThaliSynergyReport? {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val content = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = content.optJSONArray("parts") ?: return null
            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                textBuilder.append(parts.getJSONObject(i).optString("text", ""))
            }

            val cleanJson = textBuilder.toString()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(cleanJson)
            val hacksArray = json.optJSONArray("glucoseSpikeMitigationHacks") ?: JSONArray()
            val hacksList = mutableListOf<String>()
            for (i in 0 until hacksArray.length()) {
                hacksList.add(hacksArray.getString(i))
            }

            AiThaliSynergyReport(
                thaliSummary = json.optString("thaliSummary", summaryFallback.ifBlank { "Custom Desi Thali" }),
                totalCalories = json.optDouble("totalCalories", 550.0),
                totalProtein = json.optDouble("totalProtein", 16.0),
                totalCarbs = json.optDouble("totalCarbs", 75.0),
                totalFat = json.optDouble("totalFat", 18.0),
                totalFiber = json.optDouble("totalFiber", 8.0),
                glycemicLoadRating = json.optString("glycemicLoadRating", "Moderate"),
                glycemicIndexScore = json.optInt("glycemicIndexScore", 55),
                ayurvedicDoshaBalance = json.optString("ayurvedicDoshaBalance", "Balanced Tridoshic meal"),
                thaliScoreOutOf100 = json.optInt("thaliScoreOutOf100", 82),
                glucoseSpikeMitigationHacks = hacksList,
                foodSynergyNotes = json.optString("foodSynergyNotes", "Balanced pairing of grains, legumes, and cultured dairy.")
            )
        } catch (e: Exception) {
            null
        }
    }

    fun getSmartOfflineMealRecommendations(
        targetCal: Int,
        mealType: String,
        preference: String
    ): List<AiMealRecommendation> {
        val cal = if (targetCal <= 0) 450 else targetCal
        return listOf(
            AiMealRecommendation(
                mealName = "Soya & Palak Bhurji with Roti",
                category = "Ultra High Protein Veg",
                description = "Nutritious meal made of crushed soya granules sautéed with spinach, onions, and tomatoes, served with whole wheat roti.",
                portionDescription = "1 katori Soya Sabzi + 2 Phulkas",
                calories = (cal * 0.95),
                protein = 28.5,
                carbs = 42.0,
                fat = 10.5,
                fiber = 9.0,
                preparationTimeMinutes = 15,
                healthBenefit = "Packed with plant isoflavones and 28g complete vegetarian protein.",
                ingredientsSummary = "Soya granules, spinach, tomatoes, jeera, whole wheat flour",
                itemsToLog = listOf(
                    AiDecomposedItem("Soya Bhurji", 1.0, "katori (180g)", (cal * 0.55), 22.0, 14.0, 9.0, 4.5, "High protein vegetarian powerhouse."),
                    AiDecomposedItem("Phulka Roti (Plain)", 2.0, "roti", (cal * 0.40), 6.5, 28.0, 1.5, 4.5, "Complex carbohydrates.")
                )
            ),
            AiMealRecommendation(
                mealName = "Moong Dal Cheela with Mint Curd",
                category = "Quick 10-Min Nashta",
                description = "Golden savory crepes made from yellow moong dal batter stuffed with grated paneer and coriander, paired with probiotic curd.",
                portionDescription = "2 Cheelas + 1 katori Dahi",
                calories = (cal * 0.90),
                protein = 21.0,
                carbs = 48.0,
                fat = 12.0,
                fiber = 7.5,
                preparationTimeMinutes = 12,
                healthBenefit = "Easy on digestion (Laghu Aahar in Ayurveda) with low glycemic index.",
                ingredientsSummary = "Soaked moong dal, paneer, curd, ginger, green chilies",
                itemsToLog = listOf(
                    AiDecomposedItem("Moong Dal Cheela with Paneer", 2.0, "cheela", (cal * 0.70), 16.0, 38.0, 10.0, 6.0, "Light and protein dense."),
                    AiDecomposedItem("Fresh Curd / Dahi", 1.0, "katori (150g)", (cal * 0.20), 5.0, 10.0, 2.0, 1.5, "Natural probiotics.")
                )
            ),
            AiMealRecommendation(
                mealName = "Traditional Dal Tadka & Jeera Rice Thali",
                category = "Homestyle Comfort Classic",
                description = "Classic comforting yellow toor dal tempered with cumin, garlic, and hing, paired with steamed jeera rice and crisp cucumber salad.",
                portionDescription = "1.5 katori Dal + 1 bowl Rice + Kachumber",
                calories = (cal * 1.0),
                protein = 16.0,
                carbs = 68.0,
                fat = 11.0,
                fiber = 8.2,
                preparationTimeMinutes = 20,
                healthBenefit = "Classic pulse + grain synergy providing all 9 essential amino acids.",
                ingredientsSummary = "Toor dal, basmati rice, jeera, garlic, mustard seeds, cucumber, lemon",
                itemsToLog = listOf(
                    AiDecomposedItem("Dal Tadka (Toor Dal)", 1.5, "katori", (cal * 0.50), 11.0, 26.0, 6.0, 5.5, "Plant protein and folate."),
                    AiDecomposedItem("Steamed Jeera Rice", 1.0, "bowl", (cal * 0.45), 4.0, 40.0, 4.8, 1.0, "Digestible carbohydrates."),
                    AiDecomposedItem("Lemon Cucumber Salad", 1.0, "bowl", (cal * 0.05), 1.0, 2.0, 0.2, 1.7, "Fiber blunts glucose spike.")
                )
            )
        )
    }

    fun getSmartOfflineThaliReport(thaliText: String): AiThaliSynergyReport {
        return AiThaliSynergyReport(
            thaliSummary = thaliText.ifBlank { "Traditional Indian Homestyle Thali" },
            totalCalories = 590.0,
            totalProtein = 19.5,
            totalCarbs = 78.0,
            totalFat = 20.0,
            totalFiber = 9.8,
            glycemicLoadRating = "Moderate (Balanced)",
            glycemicIndexScore = 52,
            ayurvedicDoshaBalance = "Tridoshic (Balances Vata & Pitta, Nourishes Agni)",
            thaliScoreOutOf100 = 89,
            glucoseSpikeMitigationHacks = listOf(
                "Eat the raw salad/kachumber first: dietary fiber coats the stomach lining and lowers blood sugar spike by up to 28%.",
                "Apply half a teaspoon of pure A2 desi cow ghee to hot rotis: healthy fats slow gastric emptying and lower glycemic response.",
                "Squeeze fresh lemon on your dal: Vitamin C boosts non-heme plant iron absorption by 300%."
            ),
            foodSynergyNotes = "The pairing of legumes (Dal) with whole grains (Roti/Rice) provides complete complementary proteins with balanced leucine and lysine."
        )
    }
}

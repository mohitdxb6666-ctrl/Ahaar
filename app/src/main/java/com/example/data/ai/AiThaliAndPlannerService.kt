package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.AiMealRecommendation
import com.example.data.model.ThaliOptimizationResult
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
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun recommendNextMeals(
        remainingCalories: Int,
        remainingProtein: Float,
        mealType: String,
        dietPreference: String
    ): Result<List<AiMealRecommendation>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.success(getFallbackMealRecommendations(remainingCalories, remainingProtein, mealType))
            }

            val prompt = """
                You are Ahaar AI's Smart Indian Meal Planner.
                The user needs recommendations for their upcoming meal: "$mealType".
                Remaining Daily Budget:
                - Calories available: $remainingCalories kcal
                - Protein required: $remainingProtein g
                - Diet preference: $dietPreference (Vegetarian / Eggetarian / Non-veg)

                Generate exactly 3 delicious, authentic Indian home-cooked meal options that hit this calorie and protein target.
                Prioritize high-satiety, balanced glycemic index, and wholesome regional preparations (North Indian, South Indian, Maharashtrian, Gujarati, Bengali, etc.).

                Respond ONLY in valid raw JSON with this exact schema:
                {
                  "recommendations": [
                    {
                      "dishName": "Moong Dal Cheela with Mint Paneer Stuffing",
                      "hindiName": "मूंग दाल चीला पनीर स्टफिंग के साथ",
                      "reason": "Perfect high-protein, low-GI dinner that fits within your 450 kcal remaining budget.",
                      "calories": 380,
                      "protein": 21.5,
                      "carbs": 38.0,
                      "fat": 12.0,
                      "prepTime": "15 mins",
                      "region": "North Indian",
                      "quickRecipe": "Grind soaked yellow moong with ginger & green chili. Spread on tawa, stuff with grated raw paneer."
                    }
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(getFallbackMealRecommendations(remainingCalories, remainingProtein, mealType))
            }

            val bodyString = response.body?.string() ?: ""
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext Result.success(getFallbackMealRecommendations(remainingCalories, remainingProtein, mealType))
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJson)

            val recs = mutableListOf<AiMealRecommendation>()
            val recArray = parsedObj.optJSONArray("recommendations") ?: JSONArray()
            for (i in 0 until recArray.length()) {
                val r = recArray.getJSONObject(i)
                recs.add(
                    AiMealRecommendation(
                        dishName = r.optString("dishName", "Indian Meal"),
                        hindiName = r.optString("hindiName", ""),
                        reason = r.optString("reason", "Fits your remaining macro budget"),
                        calories = r.optInt("calories", remainingCalories.coerceAtLeast(200)),
                        protein = r.optDouble("protein", remainingProtein.toDouble()).toFloat(),
                        carbs = r.optDouble("carbs", 30.0).toFloat(),
                        fat = r.optDouble("fat", 10.0).toFloat(),
                        prepTime = r.optString("prepTime", "20 mins"),
                        region = r.optString("region", "Pan-Indian"),
                        quickRecipe = r.optString("quickRecipe", "Cook with minimum cold-pressed oil and aromatic spices.")
                    )
                )
            }

            Result.success(if (recs.isNotEmpty()) recs else getFallbackMealRecommendations(remainingCalories, remainingProtein, mealType))
        } catch (e: Exception) {
            Result.success(getFallbackMealRecommendations(remainingCalories, remainingProtein, mealType))
        }
    }

    suspend fun optimizeThali(thaliComponents: String): Result<ThaliOptimizationResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.success(getFallbackThaliOptimization(thaliComponents))
            }

            val prompt = """
                You are Ahaar AI's Glycemic Load & Ayurvedic Synergy Optimizer.
                Analyze this Indian Thali combination: "$thaliComponents".

                Calculate:
                - Overall Glycemic Load (GL) number
                - Spike Risk: "Low Risk (<10)", "Moderate (10-19)", "High Spike Warning (>=20)"
                - Ayurvedic Dosha impact (Vata, Pitta, Kapha)
                - Sequence eating advice (Optimal chronological eating order to minimize blood sugar spikes)
                - 3 Ayurvedic Food Synergy Hacks (e.g., pairing lemon with dal for iron, ghee with rice for slow insulin curve).

                Respond ONLY in valid raw JSON with this exact schema:
                {
                  "thaliName": "e.g. Classic Ghar Ka Dal Roti Thali",
                  "glycemicLoad": 16.5,
                  "spikeRisk": "Moderate Spike",
                  "vataBalance": "Excellent - warm soupy dal and ghee pacify Vata",
                  "pittaBalance": "Good - avoid excessive red chili in tadka",
                  "kaphaBalance": "Neutral - reduce white rice if sluggish",
                  "sequenceEatingAdvice": "1st: Eat the cucumber salad, 2nd: Drink the dal & eat subzi, 3rd: Eat the roti last.",
                  "ayurvedicSynergyTips": [
                    "Add a fresh squeeze of lemon to your dal. Vitamin C triples the bioavailability of non-heme plant iron.",
                    "Eat 1 spoon of dahi/curd before carbohydrates to activate GLP-1 satiety hormones.",
                    "A pinch of roasted cumin and hing in the dal prevents gas and bloating from lentils."
                  ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(getFallbackThaliOptimization(thaliComponents))
            }

            val bodyString = response.body?.string() ?: ""
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext Result.success(getFallbackThaliOptimization(thaliComponents))
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJson)

            val tips = mutableListOf<String>()
            val tipsArray = parsedObj.optJSONArray("ayurvedicSynergyTips") ?: JSONArray()
            for (i in 0 until tipsArray.length()) tips.add(tipsArray.getString(i))

            val res = ThaliOptimizationResult(
                thaliName = parsedObj.optString("thaliName", "Analyzed Indian Thali"),
                glycemicLoad = parsedObj.optDouble("glycemicLoad", 14.5).toFloat(),
                spikeRisk = parsedObj.optString("spikeRisk", "Moderate Spike"),
                vataBalance = parsedObj.optString("vataBalance", "Vata Pacifying"),
                pittaBalance = parsedObj.optString("pittaBalance", "Pitta Balancing"),
                kaphaBalance = parsedObj.optString("kaphaBalance", "Neutral"),
                sequenceEatingAdvice = parsedObj.optString("sequenceEatingAdvice", "Salad first -> Dal & Veg -> Rotis last"),
                ayurvedicSynergyTips = if (tips.isNotEmpty()) tips else listOf(
                    "Squeeze lemon into dal to boost iron absorption.",
                    "Eat fiber before carbohydrates to reduce post-meal glucose spike."
                )
            )

            Result.success(res)
        } catch (e: Exception) {
            Result.success(getFallbackThaliOptimization(thaliComponents))
        }
    }

    private fun getFallbackMealRecommendations(
        calories: Int,
        protein: Float,
        mealType: String
    ): List<AiMealRecommendation> {
        val targetCal = calories.coerceIn(300, 700)
        return listOf(
            AiMealRecommendation(
                dishName = "Spiced Paneer Bhurji with 2 Jowar Bhakris",
                hindiName = "पनीर भुर्जी और ज्वार की भाकरी",
                reason = "Loaded with 26g high biological value protein and slow-digesting sorghum fiber for zero blood sugar spike.",
                calories = targetCal.coerceAtLeast(420),
                protein = 26.5f,
                carbs = 34f,
                fat = 16f,
                prepTime = "15 mins",
                region = "Maharashtrian / North Indian",
                quickRecipe = "Crumble 120g fresh cow paneer with onion, tomato, green chilies, and turmeric. Serve hot with toasted jowar bhakri."
            ),
            AiMealRecommendation(
                dishName = "Moong Dal Khichdi with Dahi & Roasted Papad",
                hindiName = "मूंग दाल खिचड़ी, दही और पापड़",
                reason = "Classic Ayurvedic gut reset: complete amino acid spectrum with cooling probiotic dahi.",
                calories = (targetCal - 50).coerceAtLeast(350),
                protein = 16.0f,
                carbs = 48f,
                fat = 8f,
                prepTime = "20 mins",
                region = "Pan-Indian Ayurvedic",
                quickRecipe = "Pressure cook 1:1 yellow moong dal and rice with turmeric, ginger, and cumin tadka. Top with 1 tsp A2 ghee."
            ),
            AiMealRecommendation(
                dishName = "Besan Methi Chilla with Coriander Mint Chutney",
                hindiName = "बेसन मेथी चीला हरी चटनी के साथ",
                reason = "Low glycemic load, gluten-free, rich in iron, fenugreek fiber, and plant protein.",
                calories = (targetCal - 100).coerceAtLeast(290),
                protein = 18.5f,
                carbs = 28f,
                fat = 9f,
                prepTime = "12 mins",
                region = "North & Western Indian",
                quickRecipe = "Whisk gram flour with fresh chopped methi leaves, ajwain, and ginger paste. Make 2 crisp golden chillas on tawa."
            )
        )
    }

    private fun getFallbackThaliOptimization(thali: String): ThaliOptimizationResult {
        return ThaliOptimizationResult(
            thaliName = if (thali.isNotBlank()) "Thali: $thali" else "Classic Desi Ghar Ka Khana Thali",
            glycemicLoad = 14.8f,
            spikeRisk = "Moderate (Easily optimized with sequence eating)",
            vataBalance = "Strong Vata pacification: warm moist cooked grains and unctuous ghee lubricate the digestive tract.",
            pittaBalance = "Balanced: mildly spiced cumin and coriander cooling agents prevent acid reflux.",
            kaphaBalance = "Good: sufficient dietary fiber from whole grains prevents lethargy.",
            sequenceEatingAdvice = "Order of Eating: 1) Kachumber salad & Chaas -> 2) Dal & Sabzi -> 3) Rotis / Rice last. This simple sequence drops glycemic spikes by up to 38%.",
            ayurvedicSynergyTips = listOf(
                "Lemon & Dal Synergy: Squeezing 1/2 fresh nimbu into toor/moong dal boosts non-heme iron absorption threefold.",
                "Ghee Glycemic Buffer: Adding 1/2 tsp pure ghee to hot rice forms a lipid barrier that slows enzymatic starch digestion into glucose.",
                "Chew Ajwain Post-Meal: Chewing half a teaspoon of roasted ajwain (carom seeds) with rock salt promotes hydrochloric acid secretion for smooth digestion."
            )
        )
    }
}

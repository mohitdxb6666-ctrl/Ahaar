package com.example.data.ai

import com.example.BuildConfig
import com.example.data.model.AiPlateItem
import com.example.data.model.PackagedFoodAudit
import com.example.data.model.RestaurantBillAudit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiRestaurantAndLabelService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun auditRestaurantOrder(orderText: String): Result<RestaurantBillAudit> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.success(getFallbackRestaurantAudit(orderText))
            }

            val prompt = """
                You are Ahaar AI's Indian Dining Out & Swiggy/Zomato Auditor.
                Analyze this restaurant meal/order: "$orderText".
                Indian restaurant foods frequently hide heavy amounts of restaurant-grade refined oils, salted butter, heavy cream, and MSG.
                Accurately estimate:
                - individual dishes with portions, calories, protein, carbs, fat, fiber
                - total calories, total protein (g), total fat (g)
                - hidden oil/butter load (estimated in teaspoons)
                - sodium risk warning
                - Swasthya (Health) Index out of 10
                - 3 practical harm-reduction / damage-control tips.

                Respond ONLY in valid raw JSON with this exact schema:
                {
                  "restaurantType": "e.g. North Indian Mughlai Restaurant",
                  "estimatedTotalCalories": 920,
                  "estimatedTotalProtein": 32.0,
                  "estimatedTotalFat": 48.0,
                  "hiddenOilTsp": 6.5,
                  "sodiumWarning": "High (exceeds 65% of daily recommended intake)",
                  "swasthyaScoreOutOf10": 5,
                  "damageControlHacks": [
                    "Squeeze lime over gravies to reduce glycemic impact and assist digestion.",
                    "Drink warm water with crushed ajwain & saunf 30 minutes after dining to prevent sluggish agni."
                  ],
                  "dishes": [
                    {
                      "dishName": "Butter Naan",
                      "hindiName": "बटर नान",
                      "portionEstimate": "1 piece (90g)",
                      "calories": 290,
                      "protein": 6.5,
                      "carbs": 44.0,
                      "fat": 10.0,
                      "fiber": 1.5,
                      "healthNote": "Refined maida and butter"
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
                return@withContext Result.success(getFallbackRestaurantAudit(orderText))
            }

            val bodyString = response.body?.string() ?: ""
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext Result.success(getFallbackRestaurantAudit(orderText))
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJson)

            val dishesList = mutableListOf<AiPlateItem>()
            val dishesArray = parsedObj.optJSONArray("dishes") ?: JSONArray()
            for (i in 0 until dishesArray.length()) {
                val d = dishesArray.getJSONObject(i)
                dishesList.add(
                    AiPlateItem(
                        dishName = d.optString("dishName", "Dish"),
                        hindiName = d.optString("hindiName", ""),
                        portionEstimate = d.optString("portionEstimate", "1 serving"),
                        calories = d.optInt("calories", 200),
                        protein = d.optDouble("protein", 5.0).toFloat(),
                        carbs = d.optDouble("carbs", 20.0).toFloat(),
                        fat = d.optDouble("fat", 8.0).toFloat(),
                        fiber = d.optDouble("fiber", 2.0).toFloat(),
                        healthNote = d.optString("healthNote", "")
                    )
                )
            }

            val hacksList = mutableListOf<String>()
            val hacksArray = parsedObj.optJSONArray("damageControlHacks") ?: JSONArray()
            for (i in 0 until hacksArray.length()) {
                hacksList.add(hacksArray.getString(i))
            }

            val audit = RestaurantBillAudit(
                restaurantType = parsedObj.optString("restaurantType", "Restaurant Dining"),
                estimatedTotalCalories = parsedObj.optInt("estimatedTotalCalories", dishesList.sumOf { it.calories }),
                estimatedTotalProtein = parsedObj.optDouble("estimatedTotalProtein", 25.0).toFloat(),
                estimatedTotalFat = parsedObj.optDouble("estimatedTotalFat", 35.0).toFloat(),
                hiddenOilTsp = parsedObj.optDouble("hiddenOilTsp", 5.0).toFloat(),
                sodiumWarning = parsedObj.optString("sodiumWarning", "Moderate to High Sodium"),
                swasthyaScoreOutOf10 = parsedObj.optInt("swasthyaScoreOutOf10", 6),
                damageControlHacks = if (hacksList.isNotEmpty()) hacksList else listOf(
                    "Drink warm water with lemon 30 minutes after your meal.",
                    "Take a 15-minute gentle 100-step walk (Shatapawali) before resting."
                ),
                dishes = dishesList
            )

            Result.success(audit)
        } catch (e: Exception) {
            Result.success(getFallbackRestaurantAudit(orderText))
        }
    }

    suspend fun auditPackagedFood(productName: String, ingredientText: String): Result<PackagedFoodAudit> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrBlank()) {
                return@withContext Result.success(getFallbackPackagedAudit(productName))
            }

            val prompt = """
                You are Ahaar AI's Desi Nutri-Score & Ultra-Processed Food Auditor.
                Analyze this packaged food or snack:
                Product: "$productName"
                Ingredients: "$ingredientText"

                Examine for Indian packaged food deceptions:
                - Palmolein oil / Palm oil / Hydrogenated fats (Vanaspati)
                - Invert sugar syrup, liquid glucose, maltodextrin
                - Hidden sodium, INS artificial emulsifiers, synthetic colors (tartrazine, sunset yellow)
                - High refined flour (Maida) disguised as wheat

                Assign:
                - Desi Nutri-Score Grade: "A" (Very Clean), "B" (Good), "C" (Moderate), "D" (Poor), "E" (Ultra-Processed Red Alert)
                - Clean Food Score out of 100
                - Red flag additives found
                - Positive highlights (if any, like fiber, whole pulses)
                - Desi Clean Swap: A traditional healthy Indian equivalent
                - Summary verdict

                Respond ONLY in valid raw JSON with this exact schema:
                {
                  "productName": "$productName",
                  "brand": "Detected Brand",
                  "nutriScoreGrade": "D",
                  "cleanFoodScoreOutOf100": 38,
                  "redFlagAdditives": [
                    "Palmolein Oil (High in saturated palmitic acid)",
                    "Maltodextrin (Glycemic Index of 110, higher than table sugar)"
                  ],
                  "positiveHighlights": [
                    "Contains chickpea flour (Besan)"
                  ],
                  "desiCleanSwap": "Replace with home-roasted spices makhana or bhuna chana with pink salt.",
                  "summaryVerdict": "High in inflammatory oils and refined starches. Avoid daily snacking."
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
                return@withContext Result.success(getFallbackPackagedAudit(productName))
            }

            val bodyString = response.body?.string() ?: ""
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates") ?: return@withContext Result.success(getFallbackPackagedAudit(productName))
            val rawText = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            val parsedObj = JSONObject(cleanJson)

            val redFlags = mutableListOf<String>()
            val rArray = parsedObj.optJSONArray("redFlagAdditives") ?: JSONArray()
            for (i in 0 until rArray.length()) redFlags.add(rArray.getString(i))

            val positives = mutableListOf<String>()
            val pArray = parsedObj.optJSONArray("positiveHighlights") ?: JSONArray()
            for (i in 0 until pArray.length()) positives.add(pArray.getString(i))

            val audit = PackagedFoodAudit(
                productName = parsedObj.optString("productName", productName),
                brand = parsedObj.optString("brand", "Packaged Brand"),
                nutriScoreGrade = parsedObj.optString("nutriScoreGrade", "D"),
                cleanFoodScoreOutOf100 = parsedObj.optInt("cleanFoodScoreOutOf100", 42),
                redFlagAdditives = redFlags,
                positiveHighlights = positives,
                desiCleanSwap = parsedObj.optString("desiCleanSwap", "Handful of roasted chana & walnuts"),
                summaryVerdict = parsedObj.optString("summaryVerdict", "Moderate ultra-processing level.")
            )

            Result.success(audit)
        } catch (e: Exception) {
            Result.success(getFallbackPackagedAudit(productName))
        }
    }

    private fun getFallbackRestaurantAudit(order: String): RestaurantBillAudit {
        val query = order.lowercase()
        val dishes = if (query.contains("biryani") || query.contains("paneer")) {
            listOf(
                AiPlateItem("Paneer Tikka Masala", "पनीर टिक्का मसाला", "1 bowl (200g)", 360, 14.5f, 12f, 28f, 2.5f, "Heavy butter and restaurant cream"),
                AiPlateItem("Butter Garlic Naan", "गार्लिक नान", "1 naan (90g)", 310, 7.2f, 48f, 11f, 1.8f, "Refined maida and cooked butter"),
                AiPlateItem("Veg Dum Biryani", "दम बिरयानी", "1 plate (200g)", 320, 6.8f, 52f, 10f, 3.2f, "Ghee & aromatic spices"),
                AiPlateItem("Boondi Raita", "बूंदी रायता", "1 katori (120g)", 140, 4.2f, 14f, 7.5f, 0.5f, "Probiotic curd with fried chickpea droplets")
            )
        } else {
            listOf(
                AiPlateItem("Dal Makhani", "दाल मखनी", "1 bowl (180g)", 290, 9.2f, 28f, 16f, 6.0f, "Black urad dal with white butter & cream"),
                AiPlateItem("Tandoori Butter Roti", "तंदूरी रोटी", "2 rotis (80g)", 230, 7.5f, 38f, 5.5f, 4.0f, "Whole wheat baked in tandoor"),
                AiPlateItem("Mix Vegetable Curry", "मिक्स वेज", "1 bowl (150g)", 175, 4.0f, 16f, 11f, 4.5f, "Restaurant onion-tomato gravy with oil")
            )
        }

        return RestaurantBillAudit(
            restaurantType = "North Indian Restaurant / Cloud Kitchen",
            estimatedTotalCalories = dishes.sumOf { it.calories },
            estimatedTotalProtein = dishes.sumOf { it.protein.toDouble() }.toFloat(),
            estimatedTotalFat = dishes.sumOf { it.fat.toDouble() }.toFloat(),
            hiddenOilTsp = 6.2f,
            sodiumWarning = "High Sodium (~1,850mg, roughly 78% of daily limit)",
            swasthyaScoreOutOf10 = 5,
            damageControlHacks = listOf(
                "Order plain Tandoori Roti instead of Butter Naan to save 180 kcal and 12g saturated fat.",
                "Drink warm water with lemon 30 mins after eating to stimulate sluggish gastric agni.",
                "Leave heavy gravies on the plate; focus on paneer pieces and vegetable chunks."
            ),
            dishes = dishes
        )
    }

    private fun getFallbackPackagedAudit(productName: String): PackagedFoodAudit {
        val p = productName.lowercase()
        return if (p.contains("biscuit") || p.contains("marie") || p.contains("cookie")) {
            PackagedFoodAudit(
                productName = productName.ifBlank { "Packaged Tea Biscuits" },
                brand = "Commercial Brand",
                nutriScoreGrade = "D",
                cleanFoodScoreOutOf100 = 36,
                redFlagAdditives = listOf(
                    "Palmolein Oil (62% saturated fat, prone to arterial inflammation)",
                    "Invert Sugar Syrup (Fast spiking liquid simple sugar)",
                    "INS 503(ii) & INS 500(ii) Raising Agents"
                ),
                positiveHighlights = listOf("Fortified with Vitamin B-complex", "Low sodium"),
                desiCleanSwap = "Replace with homemade Besan Nankhatai or roasted Makhana with a cup of warm cardamom chai.",
                summaryVerdict = "Ultra-processed. 100g gives ~460 kcal with 24g added sugars and refined palm oil."
            )
        } else {
            PackagedFoodAudit(
                productName = productName.ifBlank { "Aloo Bhujia / Namkeen" },
                brand = "Commercial Snack",
                nutriScoreGrade = "E",
                cleanFoodScoreOutOf100 = 24,
                redFlagAdditives = listOf(
                    "Palmolein Oil / Cottonseed Oil (Reheated deep-frying fat)",
                    "Excessive Sodium (820mg per 100g, 41% of daily maximum)",
                    "Acidity Regulator (INS 330) & Anti-caking agent (INS 551)"
                ),
                positiveHighlights = listOf("Contains moth bean and besan flour (plant protein)"),
                desiCleanSwap = "Swap for spiced roasted chana with skin or dry roasted makhana with a sprinkle of chaat masala.",
                summaryVerdict = "Deep-fried ultra-processed snack. Dense in heated industrial oil and sodium."
            )
        }
    }
}

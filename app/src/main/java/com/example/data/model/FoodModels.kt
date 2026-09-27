package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Meal Type categorization
 */
enum class MealType(val displayName: String, val hindiName: String, val icon: String) {
    BREAKFAST("Breakfast", "नाश्ता (Nashta)", "🌅"),
    LUNCH("Lunch", "दोपहर का खाना (Lunch)", "☀️"),
    SNACKS("Evening Snacks", "चाय व नाश्ता (Snacks)", "☕"),
    DINNER("Dinner", "रात का खाना (Dinner)", "🌙"),
    EXTRA("Extra / Late Night", "अतिरिक्त (Extra)", "✨")
}

/**
 * Regional Cuisine Categories for authentic Indian tracking
 */
enum class IndianRegion(val title: String, val emoji: String) {
    ALL("All India", "🇮🇳"),
    NORTH("North Indian", "🍛"),
    SOUTH("South Indian", "🥥"),
    WEST("West / Guj & Mah", "🫓"),
    EAST("East / Bengali", "🐟"),
    STREET_FOOD("Street Food & Chaat", "🍢"),
    STAPLES("Rotis, Rice & Dals", "🌾"),
    BEVERAGES("Chai & Desi Drinks", "☕"),
    SWEETS("Mithai & Desserts", "🍯")
}

/**
 * Dietary category
 */
enum class DietaryType(val label: String, val badgeColorHex: Long) {
    VEG("Pure Veg", 0xFF2E7D32),
    NON_VEG("Non-Veg", 0xFFC62828),
    EGG("Eggitarian", 0xFFEF6C00),
    JAIN("Jain (No Root Veg)", 0xFF00897B),
    VEGAN("Vegan", 0xFF43A047)
}

/**
 * Standard Indian food item definition for regional database & suggestions
 */
data class IndianFoodItem(
    val id: String,
    val name: String,
    val regionalName: String,
    val region: IndianRegion,
    val dietaryType: DietaryType,
    val defaultServingUnit: String, // e.g. "1 medium katori (180g)", "1 roti", "1 piece", "1 plate"
    val defaultUnitGramsOrMl: Int,
    val calories: Double, // for default serving
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val glycemicIndex: String = "Medium", // Low, Medium, High
    val ayurvedicDosha: String = "Tridoshic", // Vata, Pitta, Kapha, Tridoshic
    val healthTags: List<String> = emptyList(), // e.g. "High Protein", "Diabetic Friendly", "Sattvic", "Vrat Friendly"
    val description: String = ""
)

/**
 * Room Entity for logging user meals
 */
@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // format "YYYY-MM-DD"
    val mealType: String, // MealType name
    val foodName: String,
    val regionalName: String = "",
    val quantity: Double, // e.g. 1.5
    val unit: String, // "katori", "roti", "plate", "piece", "cup", "grams"
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Room Entity for Daily Target, Water consumption, and Fasting tracking
 */
@Entity(tableName = "daily_targets")
data class DailyTargetEntity(
    @PrimaryKey
    val dateString: String, // format "YYYY-MM-DD"
    val targetCalories: Int = 2000,
    val targetProteinGrams: Int = 75,
    val targetCarbsGrams: Int = 250,
    val targetFatGrams: Int = 55,
    val targetFiberGrams: Int = 30,
    val targetWaterMl: Int = 3000,
    val waterConsumedMl: Int = 0,
    val fastingGoalHours: Int = 16,
    val isFastingActive: Boolean = false,
    val fastStartTimeMillis: Long = 0L,
    val currentWeightKg: Double = 70.0,
    val notes: String = ""
)

/**
 * Room Entity for Custom User-created Foods
 */
@Entity(tableName = "custom_foods")
data class CustomFoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val regionalName: String = "",
    val unit: String,
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val region: String = "ALL",
    val dietaryType: String = "VEG",
    val tags: String = ""
)

/**
 * Chat Message Model for Gemini Dietitian & Nutritionist
 */
data class NutritionChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val quickSuggestions: List<String> = emptyList()
)

/**
 * Decomposed Meal item from AI Scanner
 */
data class AiDecomposedItem(
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val suggestion: String = ""
)

/**
 * AI Smart Meal Recommendation for "What should I eat next?"
 */
data class AiMealRecommendation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val mealName: String,
    val category: String, // e.g. "High Protein Veg", "Quick 10-Min Nashta", "Post-Workout"
    val description: String,
    val portionDescription: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val preparationTimeMinutes: Int = 15,
    val healthBenefit: String,
    val ingredientsSummary: String = "",
    val itemsToLog: List<AiDecomposedItem> = emptyList()
)

/**
 * Restaurant Dish Analysis Item
 */
data class AiRestaurantDishItem(
    val dishName: String,
    val portion: String,
    val restaurantCalories: Double,
    val homestyleCalories: Double,
    val caloriesSavedHomestyle: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val sodiumAlert: String, // "High (600mg+)", "Moderate", "Normal"
    val healthTip: String
)

/**
 * Full Restaurant Bill / Menu Analysis
 */
data class AiRestaurantMealAnalysis(
    val restaurantNameOrType: String,
    val totalRestaurantCalories: Double,
    val totalHomestyleCalories: Double,
    val totalCalorieDifference: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val dishes: List<AiRestaurantDishItem>,
    val overallVerdict: String,
    val smartOrderingHacks: List<String>
)

/**
 * Desi Thali Glycemic Load & Ayurvedic Synergy Report
 */
data class AiThaliSynergyReport(
    val thaliSummary: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val glycemicLoadRating: String, // "Low (Safe for Diabetes)", "Moderate", "High Glucose Spike"
    val glycemicIndexScore: Int, // 1 to 100
    val ayurvedicDoshaBalance: String, // e.g., "Balances Vata & Pitta, Light for Kapha"
    val thaliScoreOutOf100: Int,
    val glucoseSpikeMitigationHacks: List<String>,
    val foodSynergyNotes: String
)

/**
 * Packaged Food / Desi Nutri-Score Analysis
 */
data class AiPackagedFoodAnalysis(
    val productName: String,
    val nutriScoreGrade: String, // "A", "B", "C", "D", "E"
    val cleanScoreOutOf100: Int,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val sugarPer100g: Double,
    val fatPer100g: Double,
    val redFlagIngredients: List<String>,
    val positiveNutrients: List<String>,
    val healthierDesiSwaps: List<String>,
    val verdict: String
)


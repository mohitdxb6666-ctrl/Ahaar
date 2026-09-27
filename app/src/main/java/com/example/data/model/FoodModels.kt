package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FoodCategory(val displayName: String, val icon: String) {
    ALL("All", "🍽️"),
    ROTI_BREAD("Roti & Bread", "🫓"),
    DAL_PULSES("Dal & Pulses", "🍲"),
    SABZI("Sabzi & Veg", "🥬"),
    RICE_GRAIN("Rice & Pulao", "🍚"),
    SOUTH_INDIAN("South Indian", "🥞"),
    SNACKS_CHAAT("Chaat & Snacks", "🥟"),
    DAIRY_PANEER("Dairy & Paneer", "🧀"),
    SWEETS_MITHAI("Mithai & Sweets", "🥮"),
    BEVERAGES("Beverages", "☕")
}

data class FoodItem(
    val id: String,
    val name: String,
    val hindiName: String,
    val category: FoodCategory,
    val basePortionName: String, // e.g. "1 medium roti (35g)"
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val glycemicIndex: Int = 50,
    val glycemicLoad: Float = 10f,
    val ayurvedicDosha: String = "Tridoshic",
    val description: String = ""
)

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // BREAKFAST, LUNCH, SNACK, DINNER
    val foodName: String,
    val hindiName: String = "",
    val portionMultiplier: Float = 1.0f,
    val portionUnit: String = "serving",
    val calories: Int,
    val carbs: Float,
    val protein: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val glycemicLoad: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyMacroTarget(
    val calories: Int = 2000,
    val protein: Int = 75,
    val carbs: Int = 225,
    val fat: Int = 55,
    val fiber: Int = 30,
    val waterMl: Int = 2500
)

data class AiPlateItem(
    val dishName: String,
    val hindiName: String = "",
    val portionEstimate: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 1.5f,
    val healthNote: String = "",
    val foodState: String = "Cooked" // "Cooked", "Raw / Uncooked", "Beverage", "Snack"
)

data class AiPhotoAnalysisResult(
    val plateTitle: String,
    val dishes: List<AiPlateItem>,
    val totalCalories: Int,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val glycemicRating: String, // "Low", "Moderate", "High"
    val ayurvedicBalanceNote: String,
    val nutritionTip: String,
    val cookedCount: Int = 0,
    val uncookedCount: Int = 0
)

data class AiMealRecommendation(
    val dishName: String,
    val hindiName: String,
    val reason: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val prepTime: String,
    val region: String,
    val quickRecipe: String
)

data class RestaurantBillAudit(
    val restaurantType: String,
    val estimatedTotalCalories: Int,
    val estimatedTotalProtein: Float,
    val estimatedTotalFat: Float,
    val hiddenOilTsp: Float,
    val sodiumWarning: String,
    val swasthyaScoreOutOf10: Int,
    val damageControlHacks: List<String>,
    val dishes: List<AiPlateItem>
)

data class ThaliOptimizationResult(
    val thaliName: String,
    val glycemicLoad: Float,
    val spikeRisk: String, // "Low Risk", "Moderate Spike", "High Spike Warning"
    val vataBalance: String,
    val pittaBalance: String,
    val kaphaBalance: String,
    val sequenceEatingAdvice: String,
    val ayurvedicSynergyTips: List<String>
)

data class PackagedFoodAudit(
    val productName: String,
    val brand: String,
    val nutriScoreGrade: String, // "A", "B", "C", "D", "E"
    val cleanFoodScoreOutOf100: Int,
    val redFlagAdditives: List<String>,
    val positiveHighlights: List<String>,
    val desiCleanSwap: String,
    val summaryVerdict: String
)

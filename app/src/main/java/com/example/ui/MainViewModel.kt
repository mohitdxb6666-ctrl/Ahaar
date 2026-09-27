package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.AppDatabase
import com.example.data.model.AiDecomposedItem
import com.example.data.model.AiMealRecommendation
import com.example.data.model.AiPackagedFoodAnalysis
import com.example.data.model.AiRestaurantDishItem
import com.example.data.model.AiRestaurantMealAnalysis
import com.example.data.model.AiThaliSynergyReport
import com.example.data.model.CustomFoodEntity
import com.example.data.model.DailyTargetEntity
import com.example.data.model.DietaryType
import com.example.data.model.IndianFoodItem
import com.example.data.model.IndianRegion
import com.example.data.model.MealLogEntity
import com.example.data.model.MealType
import com.example.data.model.NutritionChatMessage
import com.example.data.repository.AhaarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AhaarRepository(db.mealLogDao(), db.dailyTargetDao(), db.customFoodDao())
    private val geminiService = GeminiAiService()

    // Selected Date
    private val _selectedDate = MutableStateFlow(AhaarRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Meal Logs for current date
    private val _currentDayLogs = MutableStateFlow<List<MealLogEntity>>(emptyList())
    val currentDayLogs: StateFlow<List<MealLogEntity>> = _currentDayLogs.asStateFlow()

    // All logs for weekly/monthly analytics
    val allLogs: StateFlow<List<MealLogEntity>> = repository.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Target for current date
    private val _currentTarget = MutableStateFlow(DailyTargetEntity(dateString = AhaarRepository.getTodayDateString()))
    val currentTarget: StateFlow<DailyTargetEntity> = _currentTarget.asStateFlow()

    // Food Explorer / Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedRegion = MutableStateFlow(IndianRegion.ALL)
    val selectedRegion: StateFlow<IndianRegion> = _selectedRegion.asStateFlow()

    private val _selectedDietaryType = MutableStateFlow<DietaryType?>(null)
    val selectedDietaryType: StateFlow<DietaryType?> = _selectedDietaryType.asStateFlow()

    private val _searchResults = MutableStateFlow<List<IndianFoodItem>>(emptyList())
    val searchResults: StateFlow<List<IndianFoodItem>> = _searchResults.asStateFlow()

    // AI Meal Scanner State
    private val _isAnalyzingMeal = MutableStateFlow(false)
    val isAnalyzingMeal: StateFlow<Boolean> = _isAnalyzingMeal.asStateFlow()

    private val _aiDecomposedItems = MutableStateFlow<List<AiDecomposedItem>>(emptyList())
    val aiDecomposedItems: StateFlow<List<AiDecomposedItem>> = _aiDecomposedItems.asStateFlow()

    // AI Recipe Lab State
    private val _isAnalyzingRecipe = MutableStateFlow(false)
    val isAnalyzingRecipe: StateFlow<Boolean> = _isAnalyzingRecipe.asStateFlow()

    private val _recipeAnalysisResult = MutableStateFlow<String?>(null)
    val recipeAnalysisResult: StateFlow<String?> = _recipeAnalysisResult.asStateFlow()

    // AI Photo Vision Scanner
    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    // AI Restaurant Bill & Menu Audit
    private val _isAnalyzingRestaurant = MutableStateFlow(false)
    val isAnalyzingRestaurant: StateFlow<Boolean> = _isAnalyzingRestaurant.asStateFlow()

    private val _restaurantAnalysis = MutableStateFlow<AiRestaurantMealAnalysis?>(null)
    val restaurantAnalysis: StateFlow<AiRestaurantMealAnalysis?> = _restaurantAnalysis.asStateFlow()

    // AI "What Should I Eat Next?" Smart Meal Planner
    private val _isGeneratingRecommendations = MutableStateFlow(false)
    val isGeneratingRecommendations: StateFlow<Boolean> = _isGeneratingRecommendations.asStateFlow()

    private val _mealRecommendations = MutableStateFlow<List<AiMealRecommendation>>(emptyList())
    val mealRecommendations: StateFlow<List<AiMealRecommendation>> = _mealRecommendations.asStateFlow()

    // AI Desi Thali Glycemic Load & Ayurvedic Synergy
    private val _isAnalyzingThali = MutableStateFlow(false)
    val isAnalyzingThali: StateFlow<Boolean> = _isAnalyzingThali.asStateFlow()

    private val _thaliSynergyReport = MutableStateFlow<AiThaliSynergyReport?>(null)
    val thaliSynergyReport: StateFlow<AiThaliSynergyReport?> = _thaliSynergyReport.asStateFlow()

    // AI Packaged Food Desi Nutri-Score
    private val _isAnalyzingPackagedFood = MutableStateFlow(false)
    val isAnalyzingPackagedFood: StateFlow<Boolean> = _isAnalyzingPackagedFood.asStateFlow()

    private val _packagedFoodAnalysis = MutableStateFlow<AiPackagedFoodAnalysis?>(null)
    val packagedFoodAnalysis: StateFlow<AiPackagedFoodAnalysis?> = _packagedFoodAnalysis.asStateFlow()

    // AI Dietitian Chat Messages
    private val _chatMessages = MutableStateFlow<List<NutritionChatMessage>>(
        listOf(
            NutritionChatMessage(
                isUser = false,
                message = "Namaste! 🙏 I am Dr. Ahaar, your personal Indian Nutritionist & Wellness Coach.\n\nAsk me anything about balancing Indian macros, high-protein vegetarian meals, managing weight with rotis and rice, Ayurvedic doshas, or festive food tracking!",
                quickSuggestions = listOf(
                    "High protein Indian veg diet plan",
                    "How to balance 2 rotis with dal & sabzi?",
                    "Best low calorie evening snacks",
                    "Tips for Navratri / fasting"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<NutritionChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // User Profile for TDEE / Goals
    var userAge: Int = 26
    var userGender: String = "Male" // "Male", "Female"
    var userHeightCm: Double = 172.0
    var userWeightKg: Double = 72.0
    var userActivity: String = "Moderate" // Sedentary, Light, Moderate, Heavy
    var userGoal: String = "Lose Fat" // "Lose Fat", "Maintain", "Build Muscle"

    init {
        loadDataForDate(_selectedDate.value)
        performFoodSearch()
    }

    fun changeDate(offsetDays: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        try {
            val date = sdf.parse(_selectedDate.value)
            if (date != null) {
                cal.time = date
            }
        } catch (e: Exception) {
            // keep current time
        }
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        val newDateStr = sdf.format(cal.time)
        _selectedDate.value = newDateStr
        loadDataForDate(newDateStr)
    }

    fun setDate(dateString: String) {
        _selectedDate.value = dateString
        loadDataForDate(dateString)
    }

    private fun loadDataForDate(dateString: String) {
        viewModelScope.launch {
            repository.getLogsForDate(dateString).collect { logs ->
                _currentDayLogs.value = logs
            }
        }
        viewModelScope.launch {
            repository.getTargetForDate(dateString).collect { target ->
                if (target != null) {
                    _currentTarget.value = target
                } else {
                    // Create default target for date
                    val newTarget = DailyTargetEntity(
                        dateString = dateString,
                        targetCalories = calculateTDEE(),
                        targetProteinGrams = calculateTargetProtein(),
                        targetCarbsGrams = calculateTargetCarbs(),
                        targetFatGrams = calculateTargetFat()
                    )
                    repository.saveDailyTarget(newTarget)
                    _currentTarget.value = newTarget
                }
            }
        }
    }

    fun logFood(
        mealType: MealType,
        foodName: String,
        regionalName: String = "",
        quantity: Double,
        unit: String,
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double
    ) {
        viewModelScope.launch {
            val log = MealLogEntity(
                dateString = _selectedDate.value,
                mealType = mealType.name,
                foodName = foodName,
                regionalName = regionalName,
                quantity = quantity,
                unit = unit,
                calories = calories * quantity,
                proteinGrams = protein * quantity,
                carbsGrams = carbs * quantity,
                fatGrams = fat * quantity,
                fiberGrams = fiber * quantity
            )
            repository.logMeal(log)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteMealLog(id)
        }
    }

    fun logWater(amountMl: Int) {
        viewModelScope.launch {
            val current = _currentTarget.value
            val newWater = maxOf(0, current.waterConsumedMl + amountMl)
            repository.updateWaterConsumed(current.dateString, newWater)
            _currentTarget.value = current.copy(waterConsumedMl = newWater)
        }
    }

    fun toggleFasting() {
        viewModelScope.launch {
            val current = _currentTarget.value
            val newActive = !current.isFastingActive
            val newStartTime = if (newActive) System.currentTimeMillis() else 0L
            repository.updateFastingState(current.dateString, newActive, newStartTime)
            _currentTarget.value = current.copy(isFastingActive = newActive, fastStartTimeMillis = newStartTime)
        }
    }

    fun updateWeight(weightKg: Double) {
        userWeightKg = weightKg
        viewModelScope.launch {
            val current = _currentTarget.value
            repository.updateWeight(current.dateString, weightKg)
            _currentTarget.value = current.copy(currentWeightKg = weightKg)
        }
    }

    fun updateTargets(calories: Int, protein: Int, carbs: Int, fat: Int, water: Int) {
        viewModelScope.launch {
            val current = _currentTarget.value
            val updated = current.copy(
                targetCalories = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWaterMl = water
            )
            repository.saveDailyTarget(updated)
            _currentTarget.value = updated
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        performFoodSearch()
    }

    fun onRegionSelected(region: IndianRegion) {
        _selectedRegion.value = region
        performFoodSearch()
    }

    fun onDietaryTypeSelected(dietaryType: DietaryType?) {
        _selectedDietaryType.value = dietaryType
        performFoodSearch()
    }

    private fun performFoodSearch() {
        _searchResults.value = repository.searchIndianFoods(
            query = _searchQuery.value,
            region = _selectedRegion.value,
            dietaryType = _selectedDietaryType.value
        )
    }

    // AI Meal Scanner
    fun analyzeMealWithAi(description: String) {
        if (description.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingMeal.value = true
            val result = geminiService.analyzeMealText(description)
            result.onSuccess { items ->
                _aiDecomposedItems.value = items
            }.onFailure {
                _aiDecomposedItems.value = emptyList()
            }
            _isAnalyzingMeal.value = false
        }
    }

    fun logAllAiDecomposedItems(mealType: MealType) {
        val items = _aiDecomposedItems.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            items.forEach { item ->
                repository.logMeal(
                    MealLogEntity(
                        dateString = _selectedDate.value,
                        mealType = mealType.name,
                        foodName = item.foodName,
                        quantity = item.quantity,
                        unit = item.unit,
                        calories = item.calories,
                        proteinGrams = item.protein,
                        carbsGrams = item.carbs,
                        fatGrams = item.fat,
                        fiberGrams = item.fiber
                    )
                )
            }
            _aiDecomposedItems.value = emptyList()
        }
    }

    // AI Recipe Analysis
    fun analyzeRecipeWithAi(title: String, ingredients: String, servings: Int) {
        if (ingredients.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingRecipe.value = true
            val result = geminiService.analyzeRecipe(title, ingredients, servings)
            result.onSuccess { response ->
                _recipeAnalysisResult.value = response
            }.onFailure { err ->
                _recipeAnalysisResult.value = "Error analyzing recipe: ${err.localizedMessage}"
            }
            _isAnalyzingRecipe.value = false
        }
    }

    // AI Multimodal Photo Meal Scan
    fun analyzeMealPhotoWithAi(base64Image: String, mimeType: String = "image/jpeg", hint: String = "") {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            val result = geminiService.analyzeMealPhoto(base64Image, mimeType, hint)
            result.onSuccess { items ->
                _aiDecomposedItems.value = items
            }.onFailure {
                _aiDecomposedItems.value = emptyList()
            }
            _isAnalyzingPhoto.value = false
        }
    }

    // AI Restaurant Bill & Menu Audit
    fun analyzeRestaurantOrderWithAi(orderText: String) {
        if (orderText.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingRestaurant.value = true
            val result = geminiService.analyzeRestaurantBillOrMenu(orderText)
            result.onSuccess { analysis ->
                _restaurantAnalysis.value = analysis
            }.onFailure {
                _restaurantAnalysis.value = null
            }
            _isAnalyzingRestaurant.value = false
        }
    }

    fun logRestaurantDishToMeal(dish: AiRestaurantDishItem, mealType: MealType) {
        viewModelScope.launch {
            repository.logMeal(
                MealLogEntity(
                    dateString = _selectedDate.value,
                    mealType = mealType.name,
                    foodName = dish.dishName,
                    quantity = 1.0,
                    unit = dish.portion,
                    calories = dish.restaurantCalories,
                    proteinGrams = dish.protein,
                    carbsGrams = dish.carbs,
                    fatGrams = dish.fat,
                    fiberGrams = dish.fiber
                )
            )
        }
    }

    fun clearRestaurantAnalysis() {
        _restaurantAnalysis.value = null
    }

    // AI "What Should I Eat Next?" Smart Meal Planner
    fun generateMealRecommendationsWithAi(mealType: String = "Dinner", preference: String = "Pure Veg") {
        viewModelScope.launch {
            _isGeneratingRecommendations.value = true
            val logs = _currentDayLogs.value
            val target = _currentTarget.value

            val consumedCal = logs.sumOf { it.calories }.toInt()
            val consumedP = logs.sumOf { it.proteinGrams }.toInt()
            val consumedC = logs.sumOf { it.carbsGrams }.toInt()
            val consumedF = logs.sumOf { it.fatGrams }.toInt()

            val remainingCal = maxOf(250, target.targetCalories - consumedCal)
            val remainingP = maxOf(10, target.targetProteinGrams - consumedP)
            val remainingC = maxOf(20, target.targetCarbsGrams - consumedC)
            val remainingF = maxOf(5, target.targetFatGrams - consumedF)

            val result = geminiService.recommendNextMeals(
                remainingCalories = remainingCal,
                remainingProtein = remainingP,
                remainingCarbs = remainingC,
                remainingFat = remainingF,
                mealType = mealType,
                preference = preference
            )
            result.onSuccess { recommendations ->
                _mealRecommendations.value = recommendations
            }.onFailure {
                _mealRecommendations.value = emptyList()
            }
            _isGeneratingRecommendations.value = false
        }
    }

    fun logRecommendedMealToDiary(rec: AiMealRecommendation, mealType: MealType) {
        viewModelScope.launch {
            if (rec.itemsToLog.isNotEmpty()) {
                rec.itemsToLog.forEach { item ->
                    repository.logMeal(
                        MealLogEntity(
                            dateString = _selectedDate.value,
                            mealType = mealType.name,
                            foodName = item.foodName,
                            quantity = item.quantity,
                            unit = item.unit,
                            calories = item.calories,
                            proteinGrams = item.protein,
                            carbsGrams = item.carbs,
                            fatGrams = item.fat,
                            fiberGrams = item.fiber
                        )
                    )
                }
            } else {
                repository.logMeal(
                    MealLogEntity(
                        dateString = _selectedDate.value,
                        mealType = mealType.name,
                        foodName = rec.mealName,
                        quantity = 1.0,
                        unit = rec.portionDescription,
                        calories = rec.calories,
                        proteinGrams = rec.protein,
                        carbsGrams = rec.carbs,
                        fatGrams = rec.fat,
                        fiberGrams = rec.fiber
                    )
                )
            }
        }
    }

    // AI Desi Thali Glycemic Load & Ayurvedic Synergy
    fun analyzeThaliSynergyWithAi(thaliDescription: String) {
        if (thaliDescription.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingThali.value = true
            val result = geminiService.analyzeThaliSynergy(thaliDescription)
            result.onSuccess { report ->
                _thaliSynergyReport.value = report
            }.onFailure {
                _thaliSynergyReport.value = null
            }
            _isAnalyzingThali.value = false
        }
    }

    fun clearThaliReport() {
        _thaliSynergyReport.value = null
    }

    // AI Packaged Food Desi Nutri-Score
    fun analyzePackagedFoodWithAi(productName: String, ingredientsOrNutrition: String) {
        if (ingredientsOrNutrition.isBlank()) return
        viewModelScope.launch {
            _isAnalyzingPackagedFood.value = true
            val result = geminiService.analyzePackagedFood(productName, ingredientsOrNutrition)
            result.onSuccess { analysis ->
                _packagedFoodAnalysis.value = analysis
            }.onFailure {
                _packagedFoodAnalysis.value = null
            }
            _isAnalyzingPackagedFood.value = false
        }
    }

    fun clearPackagedFoodAnalysis() {
        _packagedFoodAnalysis.value = null
    }

    // AI Dietitian Chat
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        val userMsg = NutritionChatMessage(isUser = true, message = userText)
        currentList.add(userMsg)
        _chatMessages.value = currentList

        viewModelScope.launch {
            _isChatLoading.value = true
            val history = currentList.map { it.isUser to it.message }
            val profileContext = "User: $userAge yo $userGender, weight: ${userWeightKg}kg, goal: $userGoal, target: ${_currentTarget.value.targetCalories} kcal"

            val result = geminiService.chatWithDietitian(history, profileContext)
            result.onSuccess { botReply ->
                val botMsg = NutritionChatMessage(
                    isUser = false,
                    message = botReply,
                    quickSuggestions = getDynamicSuggestionsForReply(userText)
                )
                _chatMessages.value = _chatMessages.value + botMsg
            }
            _isChatLoading.value = false
        }
    }

    private fun getDynamicSuggestionsForReply(userQuery: String): List<String> {
        val lower = userQuery.lowercase()
        return when {
            lower.contains("protein") -> listOf("High protein breakfast options", "Paneer vs Soya nutrition", "Can I build muscle on dal chawal?")
            lower.contains("weight") || lower.contains("fat") -> listOf("How many rotis for fat loss?", "Low calorie Indian snacks", "Best dinner before 8 PM")
            lower.contains("fasting") || lower.contains("vrat") -> listOf("What breaks a fast in Ayurveda?", "Fasting window for weight loss", "Sabudana vs Makhana")
            else -> listOf("Calculate my Indian meal macros", "Healthy dessert alternatives", "PCOS diet tips for Indian food")
        }
    }

    // BMR & TDEE Calculations for Indian Metabolic Baseline
    fun calculateBMR(): Int {
        // Mifflin-St Jeor Formula
        return if (userGender == "Male") {
            (10 * userWeightKg + 6.25 * userHeightCm - 5 * userAge + 5).toInt()
        } else {
            (10 * userWeightKg + 6.25 * userHeightCm - 5 * userAge - 161).toInt()
        }
    }

    fun calculateTDEE(): Int {
        val bmr = calculateBMR()
        val multiplier = when (userActivity) {
            "Sedentary" -> 1.2
            "Light" -> 1.375
            "Moderate" -> 1.55
            "Heavy" -> 1.725
            else -> 1.4
        }
        val tdee = (bmr * multiplier).toInt()
        return when (userGoal) {
            "Lose Fat" -> maxOf(1400, tdee - 450)
            "Build Muscle" -> tdee + 300
            else -> tdee
        }
    }

    fun calculateTargetProtein(): Int {
        val cal = calculateTDEE()
        // 25% protein
        return (cal * 0.25 / 4).toInt()
    }

    fun calculateTargetCarbs(): Int {
        val cal = calculateTDEE()
        // 50% carbs
        return (cal * 0.50 / 4).toInt()
    }

    fun calculateTargetFat(): Int {
        val cal = calculateTDEE()
        // 25% fat
        return (cal * 0.25 / 9).toInt()
    }
}

package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.db.AppDatabase
import com.example.data.db.IndianFoodDatabase
import com.example.data.model.AiMealRecommendation
import com.example.data.model.AiPhotoAnalysisResult
import com.example.data.model.AiPlateItem
import com.example.data.model.DailyMacroTarget
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealEntry
import com.example.data.model.PackagedFoodAudit
import com.example.data.model.RestaurantBillAudit
import com.example.data.model.ThaliOptimizationResult
import com.example.data.model.WaterEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    CLICK_TO_KNOW_CALORIES,
    FOOD_SEARCH,
    AI_LAB,
    ANALYTICS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val mealDao = db.mealEntryDao()
    private val waterDao = db.waterDao()
    private val aiService = GeminiAiService()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _targetMealTypeForAdd = MutableStateFlow("LUNCH")
    val targetMealTypeForAdd: StateFlow<String> = _targetMealTypeForAdd.asStateFlow()

    private val _macroTargets = MutableStateFlow(DailyMacroTarget())
    val macroTargets: StateFlow<DailyMacroTarget> = _macroTargets.asStateFlow()

    val dailyEntries: StateFlow<List<MealEntry>> = _selectedDate
        .flatMapLatest { date -> mealDao.getEntriesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyWater: StateFlow<List<WaterEntry>> = _selectedDate
        .flatMapLatest { date -> waterDao.getWaterForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and food catalog
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FoodCategory.ALL)
    val selectedCategory: StateFlow<FoodCategory> = _selectedCategory.asStateFlow()

    val searchResults: StateFlow<List<FoodItem>> = combine(_searchQuery, _selectedCategory) { query, cat ->
        IndianFoodDatabase.search(query, cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IndianFoodDatabase.items)

    // AI Feature States
    private val _isAnalyzingPhoto = MutableStateFlow(false)
    val isAnalyzingPhoto: StateFlow<Boolean> = _isAnalyzingPhoto.asStateFlow()

    private val _photoAnalysisResult = MutableStateFlow<AiPhotoAnalysisResult?>(null)
    val photoAnalysisResult: StateFlow<AiPhotoAnalysisResult?> = _photoAnalysisResult.asStateFlow()

    private val _isAuditingRestaurant = MutableStateFlow(false)
    val isAuditingRestaurant: StateFlow<Boolean> = _isAuditingRestaurant.asStateFlow()

    private val _restaurantAuditResult = MutableStateFlow<RestaurantBillAudit?>(null)
    val restaurantAuditResult: StateFlow<RestaurantBillAudit?> = _restaurantAuditResult.asStateFlow()

    private val _isAuditingPackaged = MutableStateFlow(false)
    val isAuditingPackaged: StateFlow<Boolean> = _isAuditingPackaged.asStateFlow()

    private val _packagedFoodResult = MutableStateFlow<PackagedFoodAudit?>(null)
    val packagedFoodResult: StateFlow<PackagedFoodAudit?> = _packagedFoodResult.asStateFlow()

    private val _isLoadingPlanner = MutableStateFlow(false)
    val isLoadingPlanner: StateFlow<Boolean> = _isLoadingPlanner.asStateFlow()

    private val _plannerRecommendations = MutableStateFlow<List<AiMealRecommendation>>(emptyList())
    val plannerRecommendations: StateFlow<List<AiMealRecommendation>> = _plannerRecommendations.asStateFlow()

    private val _isOptimizingThali = MutableStateFlow(false)
    val isOptimizingThali: StateFlow<Boolean> = _isOptimizingThali.asStateFlow()

    private val _thaliResult = MutableStateFlow<ThaliOptimizationResult?>(null)
    val thaliResult: StateFlow<ThaliOptimizationResult?> = _thaliResult.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openSearchForMeal(mealType: String) {
        _targetMealTypeForAdd.value = mealType
        _currentScreen.value = AppScreen.FOOD_SEARCH
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: FoodCategory) {
        _selectedCategory.value = category
    }

    fun logFoodItem(
        foodItem: FoodItem,
        multiplier: Float = 1.0f,
        mealType: String = _targetMealTypeForAdd.value
    ) {
        viewModelScope.launch {
            val entry = MealEntry(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = foodItem.name,
                hindiName = foodItem.hindiName,
                portionMultiplier = multiplier,
                portionUnit = foodItem.basePortionName,
                calories = (foodItem.calories * multiplier).toInt(),
                carbs = foodItem.carbs * multiplier,
                protein = foodItem.protein * multiplier,
                fat = foodItem.fat * multiplier,
                fiber = foodItem.fiber * multiplier,
                glycemicLoad = foodItem.glycemicLoad * multiplier
            )
            mealDao.insertEntry(entry)
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun logAiPlateItem(item: AiPlateItem, mealType: String = "LUNCH") {
        viewModelScope.launch {
            val entry = MealEntry(
                date = _selectedDate.value,
                mealType = mealType,
                foodName = item.dishName,
                hindiName = item.hindiName,
                portionMultiplier = 1.0f,
                portionUnit = item.portionEstimate,
                calories = item.calories,
                carbs = item.carbs,
                protein = item.protein,
                fat = item.fat,
                fiber = item.fiber,
                glycemicLoad = 8.0f
            )
            mealDao.insertEntry(entry)
        }
    }

    fun deleteMealEntry(entry: MealEntry) {
        viewModelScope.launch {
            mealDao.deleteEntry(entry)
        }
    }

    fun addWater(amountMl: Int = 250) {
        viewModelScope.launch {
            waterDao.insertWater(
                WaterEntry(
                    date = _selectedDate.value,
                    amountMl = amountMl
                )
            )
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            waterDao.removeLastWaterForDate(_selectedDate.value)
        }
    }

    // AI actions
    fun analyzePhotoMeal(imageBase64: String, mimeType: String = "image/jpeg", hint: String = "") {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            val result = aiService.analyzeMealPhoto(imageBase64, mimeType, hint)
            result.onSuccess {
                _photoAnalysisResult.value = it
            }
            _isAnalyzingPhoto.value = false
        }
    }

    fun clearPhotoAnalysis() {
        _photoAnalysisResult.value = null
    }

    fun quickAnalyzePreset(hint: String) {
        viewModelScope.launch {
            _isAnalyzingPhoto.value = true
            val result = aiService.analyzeMealPhoto(
                imageBase64 = "PRESET_DEMO",
                mimeType = "image/jpeg",
                hint = hint
            )
            result.onSuccess {
                _photoAnalysisResult.value = it
            }
            _isAnalyzingPhoto.value = false
        }
    }

    fun logAllPhotoDishes(mealType: String = "LUNCH") {
        viewModelScope.launch {
            val result = _photoAnalysisResult.value ?: return@launch
            result.dishes.forEach { item ->
                val entry = MealEntry(
                    date = _selectedDate.value,
                    mealType = mealType,
                    foodName = item.dishName,
                    hindiName = item.hindiName,
                    portionMultiplier = 1.0f,
                    portionUnit = item.portionEstimate,
                    calories = item.calories,
                    carbs = item.carbs,
                    protein = item.protein,
                    fat = item.fat,
                    fiber = item.fiber,
                    glycemicLoad = 8.0f
                )
                mealDao.insertEntry(entry)
            }
        }
    }

    fun auditRestaurant(text: String) {
        viewModelScope.launch {
            _isAuditingRestaurant.value = true
            val result = aiService.auditRestaurantOrder(text)
            result.onSuccess {
                _restaurantAuditResult.value = it
            }
            _isAuditingRestaurant.value = false
        }
    }

    fun auditPackaged(productName: String, ingredients: String) {
        viewModelScope.launch {
            _isAuditingPackaged.value = true
            val result = aiService.auditPackagedFood(productName, ingredients)
            result.onSuccess {
                _packagedFoodResult.value = it
            }
            _isAuditingPackaged.value = false
        }
    }

    fun generateMealRecommendations(mealType: String = "Dinner", dietPref: String = "Vegetarian") {
        viewModelScope.launch {
            _isLoadingPlanner.value = true
            val entries = dailyEntries.value
            val target = _macroTargets.value
            val consumedCal = entries.sumOf { it.calories }
            val consumedProtein = entries.sumOf { it.protein.toDouble() }.toFloat()
            val remainingCal = (target.calories - consumedCal).coerceAtLeast(250)
            val remainingProtein = (target.protein - consumedProtein).coerceAtLeast(15f)

            val result = aiService.recommendNextMeals(remainingCal, remainingProtein, mealType, dietPref)
            result.onSuccess {
                _plannerRecommendations.value = it
            }
            _isLoadingPlanner.value = false
        }
    }

    fun optimizeThali(components: String) {
        viewModelScope.launch {
            _isOptimizingThali.value = true
            val result = aiService.optimizeThali(components)
            result.onSuccess {
                _thaliResult.value = it
            }
            _isOptimizingThali.value = false
        }
    }
}

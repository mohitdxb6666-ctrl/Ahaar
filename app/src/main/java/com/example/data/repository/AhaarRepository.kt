package com.example.data.repository

import com.example.data.local.CustomFoodDao
import com.example.data.local.DailyTargetDao
import com.example.data.local.MealLogDao
import com.example.data.model.CustomFoodEntity
import com.example.data.model.DailyTargetEntity
import com.example.data.model.DietaryType
import com.example.data.model.IndianFoodItem
import com.example.data.model.IndianRegion
import com.example.data.model.MealLogEntity
import com.example.data.seed.IndianFoodDatabase
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AhaarRepository(
    private val mealLogDao: MealLogDao,
    private val dailyTargetDao: DailyTargetDao,
    private val customFoodDao: CustomFoodDao
) {
    val allCustomFoods: Flow<List<CustomFoodEntity>> = customFoodDao.getAllCustomFoods()

    fun getLogsForDate(dateString: String): Flow<List<MealLogEntity>> =
        mealLogDao.getLogsForDate(dateString)

    fun getAllLogs(): Flow<List<MealLogEntity>> =
        mealLogDao.getAllLogs()

    fun getTargetForDate(dateString: String): Flow<DailyTargetEntity?> =
        dailyTargetDao.getTargetForDate(dateString)

    fun getRecentTargets(): Flow<List<DailyTargetEntity>> =
        dailyTargetDao.getRecentTargets()

    suspend fun logMeal(mealLog: MealLogEntity): Long =
        mealLogDao.insertMealLog(mealLog)

    suspend fun deleteMealLog(id: Long) =
        mealLogDao.deleteLogById(id)

    suspend fun saveDailyTarget(target: DailyTargetEntity) =
        dailyTargetDao.insertOrUpdateTarget(target)

    suspend fun updateWaterConsumed(dateString: String, waterMl: Int) =
        dailyTargetDao.updateWaterConsumed(dateString, waterMl)

    suspend fun updateFastingState(dateString: String, isActive: Boolean, startTime: Long) =
        dailyTargetDao.updateFastingState(dateString, isActive, startTime)

    suspend fun updateWeight(dateString: String, weightKg: Double) =
        dailyTargetDao.updateWeight(dateString, weightKg)

    suspend fun addCustomFood(food: CustomFoodEntity): Long =
        customFoodDao.insertCustomFood(food)

    suspend fun deleteCustomFood(food: CustomFoodEntity) =
        customFoodDao.deleteCustomFood(food)

    fun searchIndianFoods(
        query: String,
        region: IndianRegion? = null,
        dietaryType: DietaryType? = null
    ): List<IndianFoodItem> {
        return IndianFoodDatabase.searchFoods(query, region, dietaryType)
    }

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomFoodEntity
import com.example.data.model.DailyTargetEntity
import com.example.data.model.MealLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealLogDao {
    @Query("SELECT * FROM meal_logs WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getLogsForDate(dateString: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<MealLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(log: MealLogEntity): Long

    @Update
    suspend fun updateMealLog(log: MealLogEntity)

    @Delete
    suspend fun deleteMealLog(log: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("SELECT SUM(calories) FROM meal_logs WHERE dateString = :dateString")
    fun getTotalCaloriesForDate(dateString: String): Flow<Double?>
}

@Dao
interface DailyTargetDao {
    @Query("SELECT * FROM daily_targets WHERE dateString = :dateString LIMIT 1")
    fun getTargetForDate(dateString: String): Flow<DailyTargetEntity?>

    @Query("SELECT * FROM daily_targets ORDER BY dateString DESC LIMIT 30")
    fun getRecentTargets(): Flow<List<DailyTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTarget(target: DailyTargetEntity)

    @Query("UPDATE daily_targets SET waterConsumedMl = :waterMl WHERE dateString = :dateString")
    suspend fun updateWaterConsumed(dateString: String, waterMl: Int)

    @Query("UPDATE daily_targets SET isFastingActive = :isActive, fastStartTimeMillis = :startTime WHERE dateString = :dateString")
    suspend fun updateFastingState(dateString: String, isActive: Boolean, startTime: Long)

    @Query("UPDATE daily_targets SET currentWeightKg = :weightKg WHERE dateString = :dateString")
    suspend fun updateWeight(dateString: String, weightKg: Double)
}

@Dao
interface CustomFoodDao {
    @Query("SELECT * FROM custom_foods ORDER BY id DESC")
    fun getAllCustomFoods(): Flow<List<CustomFoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomFood(food: CustomFoodEntity): Long

    @Delete
    suspend fun deleteCustomFood(food: CustomFoodEntity)
}

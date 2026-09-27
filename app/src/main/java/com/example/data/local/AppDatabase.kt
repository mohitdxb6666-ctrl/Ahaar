package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CustomFoodEntity
import com.example.data.model.DailyTargetEntity
import com.example.data.model.MealLogEntity

@Database(
    entities = [
        MealLogEntity::class,
        DailyTargetEntity::class,
        CustomFoodEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealLogDao(): MealLogDao
    abstract fun dailyTargetDao(): DailyTargetDao
    abstract fun customFoodDao(): CustomFoodDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ahaar_calorie_tracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

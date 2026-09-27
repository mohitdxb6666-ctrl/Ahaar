package com.example.data.ai

import com.example.data.model.AiMealRecommendation
import com.example.data.model.AiPhotoAnalysisResult
import com.example.data.model.PackagedFoodAudit
import com.example.data.model.RestaurantBillAudit
import com.example.data.model.ThaliOptimizationResult

class GeminiAiService(
    private val visionService: AiVisionMealService = AiVisionMealService(),
    private val restaurantAndLabelService: AiRestaurantAndLabelService = AiRestaurantAndLabelService(),
    private val thaliAndPlannerService: AiThaliAndPlannerService = AiThaliAndPlannerService()
) {
    suspend fun analyzeMealPhoto(
        imageBase64: String,
        mimeType: String = "image/jpeg",
        hint: String = ""
    ): Result<AiPhotoAnalysisResult> {
        return visionService.analyzeMealPhoto(imageBase64, mimeType, hint)
    }

    suspend fun auditRestaurantOrder(orderText: String): Result<RestaurantBillAudit> {
        return restaurantAndLabelService.auditRestaurantOrder(orderText)
    }

    suspend fun auditPackagedFood(productName: String, ingredientText: String): Result<PackagedFoodAudit> {
        return restaurantAndLabelService.auditPackagedFood(productName, ingredientText)
    }

    suspend fun recommendNextMeals(
        remainingCalories: Int,
        remainingProtein: Float,
        mealType: String = "Dinner",
        dietPreference: String = "Vegetarian"
    ): Result<List<AiMealRecommendation>> {
        return thaliAndPlannerService.recommendNextMeals(remainingCalories, remainingProtein, mealType, dietPreference)
    }

    suspend fun optimizeThali(thaliComponents: String): Result<ThaliOptimizationResult> {
        return thaliAndPlannerService.optimizeThali(thaliComponents)
    }
}

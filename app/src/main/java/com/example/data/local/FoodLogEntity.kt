package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String,
    val portionGrams: Int,
    val caloriesKcal: Int,
    val sugarGrams: Float,
    val carbsGrams: Float,
    val proteinGrams: Float,
    val fatGrams: Float,
    val saturatedFatGrams: Float,
    val sodiumMg: Float,
    val warningSeals: String, // Comma-separated names of WarningSeal
    val healthScore: Int,
    val verdictSummary: String,
    val benefits: String, // Delimited by pipe '|'
    val cons: String, // Delimited by pipe '|'
    val personalizedAdvice: String,
    val imageBase64: String? = null
)

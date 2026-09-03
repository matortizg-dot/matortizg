package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.HealthGoal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val goal: HealthGoal = HealthGoal.PERDER_GRASA,
    val targetCalories: Int = HealthGoal.PERDER_GRASA.defaultCalories,
    val maxSugarGrams: Float = HealthGoal.PERDER_GRASA.maxSugarGrams,
    val targetProteinGrams: Float = HealthGoal.PERDER_GRASA.targetProteinGrams,
    val maxSodiumMg: Float = HealthGoal.PERDER_GRASA.maxSodiumMg,
    val maxDailyWarningSeals: Int = HealthGoal.PERDER_GRASA.defaultMaxSeals,
    val userName: String = "Usuario"
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_nutri_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        val goalName = prefs.getString("goal", HealthGoal.PERDER_GRASA.name) ?: HealthGoal.PERDER_GRASA.name
        val goal = try {
            HealthGoal.valueOf(goalName)
        } catch (e: Exception) {
            HealthGoal.PERDER_GRASA
        }

        return UserProfile(
            goal = goal,
            targetCalories = prefs.getInt("targetCalories", goal.defaultCalories),
            maxSugarGrams = prefs.getFloat("maxSugarGrams", goal.maxSugarGrams),
            targetProteinGrams = prefs.getFloat("targetProteinGrams", goal.targetProteinGrams),
            maxSodiumMg = prefs.getFloat("maxSodiumMg", goal.maxSodiumMg),
            maxDailyWarningSeals = prefs.getInt("maxDailyWarningSeals", goal.defaultMaxSeals),
            userName = prefs.getString("userName", "Usuario") ?: "Usuario"
        )
    }

    fun updateGoal(newGoal: HealthGoal) {
        prefs.edit()
            .putString("goal", newGoal.name)
            .putInt("targetCalories", newGoal.defaultCalories)
            .putFloat("maxSugarGrams", newGoal.maxSugarGrams)
            .putFloat("targetProteinGrams", newGoal.targetProteinGrams)
            .putFloat("maxSodiumMg", newGoal.maxSodiumMg)
            .putInt("maxDailyWarningSeals", newGoal.defaultMaxSeals)
            .apply()
        _userProfile.value = loadProfile()
    }

    fun updateCustomTargets(
        targetCalories: Int,
        maxSugarGrams: Float,
        targetProteinGrams: Float,
        maxSodiumMg: Float,
        maxDailyWarningSeals: Int = 0
    ) {
        prefs.edit()
            .putInt("targetCalories", targetCalories)
            .putFloat("maxSugarGrams", maxSugarGrams)
            .putFloat("targetProteinGrams", targetProteinGrams)
            .putFloat("maxSodiumMg", maxSodiumMg)
            .putInt("maxDailyWarningSeals", maxDailyWarningSeals)
            .apply()
        _userProfile.value = loadProfile()
    }
}

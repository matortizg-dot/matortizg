package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.local.FoodLogDao
import com.example.data.local.FoodLogEntity
import com.example.data.remote.GeminiNutritionService
import com.example.model.DetailedNutritionReport
import com.example.model.HealthGoal
import com.example.model.MealType
import com.example.model.WarningSeal
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class DayIntakeSummary(
    val dayOfWeek: String, // "Lun", "Mar", "Mié", etc.
    val dateLabel: String, // "2 Sep"
    val timestamp: Long,
    val totalCalories: Int,
    val totalSugarGrams: Float,
    val totalProteinGrams: Float,
    val totalCarbsGrams: Float,
    val totalFatGrams: Float,
    val totalSodiumMg: Float,
    val sealsCount: Int,
    val logsCount: Int
)

data class WeeklySummary(
    val totalCalories: Int,
    val averageDailyCalories: Int,
    val totalSugarGrams: Float,
    val averageDailySugarGrams: Float,
    val totalProteinGrams: Float,
    val averageDailyProteinGrams: Float,
    val totalCarbsGrams: Float,
    val averageDailyCarbsGrams: Float,
    val totalFatGrams: Float,
    val averageDailyFatGrams: Float,
    val totalWarningSeals: Int,
    val sealsBreakdown: Map<WarningSeal, Int>,
    val days: List<DayIntakeSummary>,
    val averageHealthScore: Int,
    val goalEvaluation: String
)

class NutritionRepository(
    private val foodLogDao: FoodLogDao,
    private val geminiService: GeminiNutritionService = GeminiNutritionService()
) {

    suspend fun analyzeFood(
        foodImage: Bitmap?,
        labelImage: Bitmap?,
        notes: String?,
        goal: HealthGoal,
        mealType: MealType
    ): Result<DetailedNutritionReport> {
        return geminiService.analyzeMealAndLabel(foodImage, labelImage, notes, goal, mealType)
    }

    suspend fun saveFoodLog(report: DetailedNutritionReport): Long {
        val entity = FoodLogEntity(
            foodName = report.foodName,
            timestamp = System.currentTimeMillis(),
            mealType = report.mealType.name,
            portionGrams = report.portionGrams,
            caloriesKcal = report.caloriesKcal,
            sugarGrams = report.sugarGrams,
            carbsGrams = report.carbsGrams,
            proteinGrams = report.proteinGrams,
            fatGrams = report.fatGrams,
            saturatedFatGrams = report.saturatedFatGrams,
            sodiumMg = report.sodiumMg,
            warningSeals = report.warningSeals.joinToString(",") { it.name },
            healthScore = report.healthScore,
            verdictSummary = report.verdictSummary,
            benefits = report.benefits.joinToString("|"),
            cons = report.cons.joinToString("|"),
            personalizedAdvice = report.personalizedAdvice,
            imageBase64 = report.imageBase64
        )
        return foodLogDao.insertLog(entity)
    }

    fun getAllLogs(): Flow<List<FoodLogEntity>> = foodLogDao.getAllLogs()

    fun getWeeklyLogs(): Flow<List<FoodLogEntity>> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startTime = cal.timeInMillis
        val endTime = System.currentTimeMillis() + 86400000L
        return foodLogDao.getLogsBetween(startTime, endTime)
    }

    suspend fun deleteLog(id: Long) {
        foodLogDao.deleteLogById(id)
    }

    fun calculateWeeklySummary(logs: List<FoodLogEntity>, goal: HealthGoal): WeeklySummary {
        val now = Calendar.getInstance()
        val dayNames = arrayOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")

        // Build last 7 days buckets
        val daysList = mutableListOf<DayIntakeSummary>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + 86400000L - 1L

            val dayOfWeekIndex = dayCal.get(Calendar.DAY_OF_WEEK) - 1
            val dayName = dayNames.getOrElse(dayOfWeekIndex) { "Día" }
            val dateLabel = "${dayCal.get(Calendar.DAY_OF_MONTH)}/${dayCal.get(Calendar.MONTH) + 1}"

            val dayLogs = logs.filter { it.timestamp in dayStart..dayEnd }
            val cals = dayLogs.sumOf { it.caloriesKcal }
            val sugar = dayLogs.sumOf { it.sugarGrams.toDouble() }.toFloat()
            val protein = dayLogs.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val carbs = dayLogs.sumOf { it.carbsGrams.toDouble() }.toFloat()
            val fat = dayLogs.sumOf { it.fatGrams.toDouble() }.toFloat()
            val sodium = dayLogs.sumOf { it.sodiumMg.toDouble() }.toFloat()
            val seals = dayLogs.sumOf { entity ->
                if (entity.warningSeals.isBlank()) 0 else entity.warningSeals.split(",").size
            }

            daysList.add(
                DayIntakeSummary(
                    dayOfWeek = dayName,
                    dateLabel = dateLabel,
                    timestamp = dayStart,
                    totalCalories = cals,
                    totalSugarGrams = sugar,
                    totalProteinGrams = protein,
                    totalCarbsGrams = carbs,
                    totalFatGrams = fat,
                    totalSodiumMg = sodium,
                    sealsCount = seals,
                    logsCount = dayLogs.size
                )
            )
        }

        val totalCalories = logs.sumOf { it.caloriesKcal }
        val daysCount = 7
        val avgCalories = totalCalories / daysCount
        val totalSugar = logs.sumOf { it.sugarGrams.toDouble() }.toFloat()
        val avgSugar = totalSugar / daysCount
        val totalProtein = logs.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val avgProtein = totalProtein / daysCount
        val totalCarbs = logs.sumOf { it.carbsGrams.toDouble() }.toFloat()
        val avgCarbs = totalCarbs / daysCount
        val totalFat = logs.sumOf { it.fatGrams.toDouble() }.toFloat()
        val avgFat = totalFat / daysCount

        val sealsMap = mutableMapOf<WarningSeal, Int>()
        logs.forEach { log ->
            if (log.warningSeals.isNotBlank()) {
                log.warningSeals.split(",").forEach { sealStr ->
                    WarningSeal.fromName(sealStr)?.let { seal ->
                        sealsMap[seal] = (sealsMap[seal] ?: 0) + 1
                    }
                }
            }
        }
        val totalWarningSeals = sealsMap.values.sum()
        val avgHealthScore = if (logs.isNotEmpty()) (logs.sumOf { it.healthScore } / logs.size) else 80

        val goalEvaluation = buildString {
            when (goal) {
                HealthGoal.PERDER_GRASA -> {
                    if (avgSugar > goal.maxSugarGrams) {
                        append("Tu promedio semanal de azúcar (${avgSugar.toInt()}g/día) supera el límite de ${goal.maxSugarGrams.toInt()}g. Reducir los alimentos con sello de Exceso de Azúcar acelerará tu déficit.")
                    } else if (avgCalories > goal.defaultCalories) {
                        append("Promedio calórico ligeramente por encima del objetivo de pérdida de grasa. Ajusta las porciones en meriendas o cenas.")
                    } else {
                        append("¡Excelente control calórico y de azúcares esta semana! Mantén este ritmo para asegurar la pérdida de grasa.")
                    }
                }
                HealthGoal.GANAR_MUSCULO -> {
                    val avgProtein = if (logs.isNotEmpty()) logs.sumOf { it.proteinGrams.toDouble() } / daysCount else 0.0
                    if (avgProtein < goal.targetProteinGrams) {
                        append("Tu ingesta proteica (${avgProtein.toInt()}g/día) está por debajo de tu meta (${goal.targetProteinGrams.toInt()}g). Aumenta huevos, pescados, legumbres o carnes magras.")
                    } else {
                        append("¡Gran aporte de nutrientes y proteínas para estimular la síntesis muscular!")
                    }
                }
                HealthGoal.CONTROL_AZUCAR -> {
                    val sugarSeals = sealsMap[WarningSeal.EXCESO_AZUCARES] ?: 0
                    if (sugarSeals > 2) {
                        append("Has acumulado $sugarSeals sellos de 'Exceso de Azúcares' esta semana. Prioriza carbohidratos complejos y alimentos con bajo índice glucémico.")
                    } else {
                        append("Buen control de sellos de azúcar esta semana. Estabilidad glucémica favorable.")
                    }
                }
                HealthGoal.SALUD_CARDIOVASCULAR -> {
                    val sodiumSeals = sealsMap[WarningSeal.EXCESO_SODIO] ?: 0
                    if (sodiumSeals > 2) {
                        append("Atención al sodio: acumulaste $sodiumSeals sellos de 'Exceso de Sodio'. Favorece alimentos frescos y condimentos herbales sin sal.")
                    } else {
                        append("Buena protección vascular esta semana con bajo número de sellos de sodio y grasas saturadas.")
                    }
                }
                HealthGoal.ALIMENTACION_LIMPIA -> {
                    if (totalWarningSeals > 3) {
                        append("Registraste $totalWarningSeals sellos de advertencia en total. Recuerda que la comida real sin procesar no tiene sellos.")
                    } else {
                        append("¡Fantástico! Tu ingesta se compone mayoritariamente de alimentos libres de sellos de advertencia.")
                    }
                }
            }
        }

        return WeeklySummary(
            totalCalories = totalCalories,
            averageDailyCalories = avgCalories,
            totalSugarGrams = totalSugar,
            averageDailySugarGrams = avgSugar,
            totalProteinGrams = totalProtein,
            averageDailyProteinGrams = avgProtein,
            totalCarbsGrams = totalCarbs,
            averageDailyCarbsGrams = avgCarbs,
            totalFatGrams = totalFat,
            averageDailyFatGrams = avgFat,
            totalWarningSeals = totalWarningSeals,
            sealsBreakdown = sealsMap,
            days = daysList,
            averageHealthScore = avgHealthScore,
            goalEvaluation = goalEvaluation
        )
    }
}

package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FoodLogEntity
import com.example.data.local.NutriScanDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserProfile
import com.example.data.repository.NutritionRepository
import com.example.data.repository.WeeklySummary
import com.example.model.DetailedNutritionReport
import com.example.model.GoalComparisonResult
import com.example.model.HealthGoal
import com.example.model.MealType
import com.example.model.MetricComparison
import java.util.Calendar
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data object Loading : ScanUiState
    data class Success(val report: DetailedNutritionReport) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class NutriScanViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NutriScanDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val repository = NutritionRepository(database.foodLogDao())

    val userProfile: StateFlow<UserProfile> = preferencesRepository.userProfile

    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    // Current inputs on scan screen
    val foodImage = MutableStateFlow<Bitmap?>(null)
    val labelImage = MutableStateFlow<Bitmap?>(null)
    val foodNotes = MutableStateFlow("")
    val selectedMealType = MutableStateFlow(MealType.ALMUERZO)

    // SnackBar / Toast events
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // All logs from Room
    val allFoodLogs: StateFlow<List<FoodLogEntity>> = repository.getAllLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Weekly Summary calculated dynamically from logs + active goal
    val weeklySummary: StateFlow<WeeklySummary> = combine(allFoodLogs, userProfile) { logs, profile ->
        repository.calculateWeeklySummary(logs, profile.goal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = repository.calculateWeeklySummary(emptyList(), HealthGoal.PERDER_GRASA)
    )

    fun analyzeMeal() {
        val fImg = foodImage.value
        val lImg = labelImage.value
        val notes = foodNotes.value.trim()
        val goal = userProfile.value.goal
        val mealType = selectedMealType.value

        if (fImg == null && lImg == null && notes.isEmpty()) {
            viewModelScope.launch {
                _userMessage.emit("Por favor toma una foto de la comida o etiqueta, o escribe el nombre del alimento.")
            }
            return
        }

        _scanUiState.value = ScanUiState.Loading

        viewModelScope.launch {
            val result = repository.analyzeFood(
                foodImage = fImg,
                labelImage = lImg,
                notes = notes.ifEmpty { null },
                goal = goal,
                mealType = mealType
            )

            result.onSuccess { report ->
                _scanUiState.value = ScanUiState.Success(report)
            }.onFailure { error ->
                _scanUiState.value = ScanUiState.Error(error.localizedMessage ?: "Error al analizar el alimento.")
            }
        }
    }

    fun saveReportToHistory(report: DetailedNutritionReport) {
        viewModelScope.launch {
            val id = repository.saveFoodLog(report)
            if (id > 0) {
                _userMessage.emit("¡Comida guardada en tu historial semanal con éxito!")
            }
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            repository.deleteLog(id)
            _userMessage.emit("Registro eliminado del historial.")
        }
    }

    fun setGoal(goal: HealthGoal) {
        preferencesRepository.updateGoal(goal)
        viewModelScope.launch {
            _userMessage.emit("Objetivo actualizado a: ${goal.title}")
        }
    }

    fun updateTargets(calories: Int, sugar: Float, protein: Float, sodium: Float, maxSeals: Int = 0) {
        preferencesRepository.updateCustomTargets(calories, sugar, protein, sodium, maxSeals)
        viewModelScope.launch {
            _userMessage.emit("Metas nutricionales y límite de sellos guardados.")
        }
    }

    fun evaluateMealAgainstGoals(report: DetailedNutritionReport): GoalComparisonResult {
        val profile = userProfile.value
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = todayCal.timeInMillis
        val todayLogs = allFoodLogs.value.filter { it.timestamp >= todayStart }

        val todayCaloriesBefore = todayLogs.sumOf { it.caloriesKcal }
        val todaySugarBefore = todayLogs.sumOf { it.sugarGrams.toDouble() }.toFloat()
        val todaySodiumBefore = todayLogs.sumOf { it.sodiumMg.toDouble() }.toFloat()
        val todayProteinBefore = todayLogs.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val todaySealsBefore = todayLogs.sumOf { if (it.warningSeals.isBlank()) 0 else it.warningSeals.split(",").size }

        val mealSealsCount = report.warningSeals.size
        val projectedTotalSeals = todaySealsBefore + mealSealsCount

        // Seal excess is triggered if:
        // 1) The meal has warning seals exceeding user daily tolerance OR
        // 2) The daily accumulated seals + this meal exceed user daily tolerance OR
        // 3) Daily tolerance is 0 and this meal contains any warning seal
        val hasSealExcess = mealSealsCount > profile.maxDailyWarningSeals ||
                projectedTotalSeals > profile.maxDailyWarningSeals ||
                (profile.maxDailyWarningSeals == 0 && mealSealsCount > 0)

        val calorieComp = MetricComparison(
            label = "Calorías",
            mealValue = report.caloriesKcal.toFloat(),
            todayBeforeValue = todayCaloriesBefore.toFloat(),
            projectedValue = (todayCaloriesBefore + report.caloriesKcal).toFloat(),
            targetLimit = profile.targetCalories.toFloat(),
            unit = "kcal",
            isExceeded = (todayCaloriesBefore + report.caloriesKcal) > profile.targetCalories,
            percentageOfDaily = if (profile.targetCalories > 0) ((report.caloriesKcal.toFloat() / profile.targetCalories) * 100).toInt() else 0
        )

        val sugarComp = MetricComparison(
            label = "Azúcar",
            mealValue = report.sugarGrams,
            todayBeforeValue = todaySugarBefore,
            projectedValue = todaySugarBefore + report.sugarGrams,
            targetLimit = profile.maxSugarGrams,
            unit = "g",
            isExceeded = (todaySugarBefore + report.sugarGrams) > profile.maxSugarGrams || report.sugarGrams > profile.maxSugarGrams,
            percentageOfDaily = if (profile.maxSugarGrams > 0) ((report.sugarGrams / profile.maxSugarGrams) * 100).toInt() else 0
        )

        val sodiumComp = MetricComparison(
            label = "Sodio",
            mealValue = report.sodiumMg,
            todayBeforeValue = todaySodiumBefore,
            projectedValue = todaySodiumBefore + report.sodiumMg,
            targetLimit = profile.maxSodiumMg,
            unit = "mg",
            isExceeded = (todaySodiumBefore + report.sodiumMg) > profile.maxSodiumMg || report.sodiumMg > profile.maxSodiumMg,
            percentageOfDaily = if (profile.maxSodiumMg > 0) ((report.sodiumMg / profile.maxSodiumMg) * 100).toInt() else 0
        )

        val proteinComp = MetricComparison(
            label = "Proteína",
            mealValue = report.proteinGrams,
            todayBeforeValue = todayProteinBefore,
            projectedValue = todayProteinBefore + report.proteinGrams,
            targetLimit = profile.targetProteinGrams,
            unit = "g",
            isExceeded = false,
            percentageOfDaily = if (profile.targetProteinGrams > 0) ((report.proteinGrams / profile.targetProteinGrams) * 100).toInt() else 0
        )

        val isAnyMetricExceeded = calorieComp.isExceeded || sugarComp.isExceeded || sodiumComp.isExceeded

        val (sealTitle, sealDetail) = when {
            mealSealsCount > 0 && hasSealExcess -> {
                val sealNames = report.warningSeals.joinToString(", ") { "${it.title} ${it.subtitle}" }
                Pair(
                    "¡Alerta de Sellos de Salud Excedidos!",
                    "Este alimento presenta $mealSealsCount sello(s) de advertencia ($sealNames). Supera tu límite configurado de ${profile.maxDailyWarningSeals} sello(s) por día."
                )
            }
            mealSealsCount > 0 -> {
                Pair(
                    "Sellos de Advertencia Detectados",
                    "Contiene $mealSealsCount sello(s), dentro de tu margen diario configurado (${profile.maxDailyWarningSeals} máx)."
                )
            }
            isAnyMetricExceeded -> {
                Pair(
                    "¡Límites Diarios Excedidos!",
                    "Aunque el alimento no presenta sellos de octágono, su aporte supera tus topes diarios establecidos."
                )
            }
            else -> {
                Pair(
                    "¡Excelente Elección Nutricional!",
                    "Alimento libre de sellos de salud y perfectamente alineado con tu meta de ${profile.goal.title}."
                )
            }
        }

        val advice = when {
            sugarComp.isExceeded && hasSealExcess ->
                "Sugerencia: Reduce la porción a la mitad o acompáñalo con fibra/agua abundante para amortiguar el pico de glucosa e insulina."
            calorieComp.isExceeded ->
                "Sugerencia: Modera las calorías de tus próximas comidas del día para mantener tu balance energético."
            sodiumComp.isExceeded ->
                "Sugerencia: Aumenta la ingesta de agua natural hoy y evita añadir sal procesada en tus siguientes platos."
            hasSealExcess ->
                "Sugerencia: Alimentos con sellos octogonales deben ser de consumo ocasional. Procura optar por versiones naturales sin procesar."
            else ->
                "¡Muy bien! Este platillo respeta tus metas diarias y protege tu salud metabólica."
        }

        return GoalComparisonResult(
            hasSealExcess = hasSealExcess,
            mealSealsCount = mealSealsCount,
            todaySealsBefore = todaySealsBefore,
            projectedTotalSeals = projectedTotalSeals,
            maxDailyWarningSeals = profile.maxDailyWarningSeals,
            exceededSeals = report.warningSeals,
            sealWarningTitle = sealTitle,
            sealWarningDetail = sealDetail,
            calorieComparison = calorieComp,
            sugarComparison = sugarComp,
            sodiumComparison = sodiumComp,
            proteinComparison = proteinComp,
            isAnyMetricExceeded = isAnyMetricExceeded,
            actionableAdvice = advice
        )
    }

    fun clearScanInputs() {
        foodImage.value = null
        labelImage.value = null
        foodNotes.value = ""
        _scanUiState.value = ScanUiState.Idle
    }

    fun loadSampleMeal(sampleTitle: String, sampleNotes: String, type: MealType) {
        foodNotes.value = sampleNotes
        selectedMealType.value = type
        analyzeMeal()
    }
}

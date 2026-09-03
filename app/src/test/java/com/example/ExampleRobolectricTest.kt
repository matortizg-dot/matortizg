package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.DetailedNutritionReport
import com.example.model.HealthGoal
import com.example.model.MealType
import com.example.model.WarningSeal
import com.example.ui.NutriScanViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("NutriScan", appName)
  }

  @Test
  fun `evaluateMealAgainstGoals triggers warning when health seals exceed daily tolerance`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = NutriScanViewModel(app)

    // Set goal to ALIMENTACION_LIMPIA (maxDailyWarningSeals = 0)
    viewModel.setGoal(HealthGoal.ALIMENTACION_LIMPIA)

    val unhealthyReport = DetailedNutritionReport(
      foodName = "Bebida Azucarada Ultraprocesada",
      portionGrams = 350,
      caloriesKcal = 180,
      sugarGrams = 42f,
      carbsGrams = 45f,
      proteinGrams = 0f,
      fatGrams = 0f,
      saturatedFatGrams = 0f,
      sodiumMg = 120f,
      warningSeals = listOf(WarningSeal.EXCESO_AZUCARES, WarningSeal.EXCESO_CALORIAS),
      healthScore = 20,
      verdictSummary = "Alto impacto metabólico",
      benefits = emptyList(),
      cons = listOf("Exceso de azúcares libres"),
      personalizedAdvice = "Evitar consumo regular"
    )

    val comparison = viewModel.evaluateMealAgainstGoals(unhealthyReport)

    // Must trigger seal excess warning
    assertTrue("Should trigger seal excess", comparison.hasSealExcess)
    assertEquals(2, comparison.mealSealsCount)
    assertTrue("Sugar must be exceeded", comparison.sugarComparison.isExceeded)
    assertTrue("Warning title must indicate seals exceeded", comparison.sealWarningTitle.contains("Sellos"))
  }

  @Test
  fun `evaluateMealAgainstGoals confirms compliance when no seals and within limits`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = NutriScanViewModel(app)

    viewModel.setGoal(HealthGoal.ALIMENTACION_LIMPIA)

    val cleanReport = DetailedNutritionReport(
      foodName = "Ensalada con Pechuga de Pollo y Aguacate",
      portionGrams = 280,
      caloriesKcal = 380,
      sugarGrams = 3f,
      carbsGrams = 12f,
      proteinGrams = 35f,
      fatGrams = 14f,
      saturatedFatGrams = 2f,
      sodiumMg = 320f,
      warningSeals = emptyList(),
      healthScore = 95,
      verdictSummary = "Excelente balance de micronutrientes y proteína limpia",
      benefits = listOf("Aporte de grasas saludables", "Alta saciedad"),
      cons = emptyList(),
      personalizedAdvice = "Ideal para tus metas de salud"
    )

    val comparison = viewModel.evaluateMealAgainstGoals(cleanReport)

    assertFalse("Should not trigger seal excess", comparison.hasSealExcess)
    assertEquals(0, comparison.mealSealsCount)
    assertFalse("Should not exceed sugar", comparison.sugarComparison.isExceeded)
    assertFalse("Should not exceed calories", comparison.calorieComparison.isExceeded)
    assertTrue("Title must indicate excellent choice", comparison.sealWarningTitle.contains("Excelente Elección"))
  }
}


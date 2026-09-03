package com.example.model

enum class WarningSeal(
    val title: String,
    val subtitle: String,
    val description: String,
    val colorHex: Long = 0xFF121212
) {
    EXCESO_CALORIAS(
        title = "EXCESO",
        subtitle = "CALORÍAS",
        description = "Supera el límite recomendado de energía por 100g/porción. Puede favorecer aumento de grasa corporal."
    ),
    EXCESO_AZUCARES(
        title = "EXCESO",
        subtitle = "AZÚCARES",
        description = "Aporta azúcares libres excesivos (>10% kcal). Puede causar picos de glucosa e inflamación celular."
    ),
    EXCESO_GRASAS_SATURADAS(
        title = "EXCESO",
        subtitle = "GRASAS SATURADAS",
        description = "Contiene más del 10% de energía en grasas saturadas. Puede elevar el colesterol LDL en sangre."
    ),
    EXCESO_GRASAS_TRANS(
        title = "EXCESO",
        subtitle = "GRASAS TRANS",
        description = "Grasas hidrogenadas perjudiciales para la salud cardiovascular y endotelial."
    ),
    EXCESO_SODIO(
        title = "EXCESO",
        subtitle = "SODIO",
        description = "Supera 1mg de sodio por kcal o 300mg/100g. Factor de riesgo para hipertensión arterial."
    ),
    CONTIENE_EDULCORANTES(
        title = "CONTIENE",
        subtitle = "EDULCORANTES",
        description = "Contiene sustitutos artificiales de azúcar. No recomendado en niños y puede alterar la microbiota."
    ),
    CONTIENE_CAFEINA(
        title = "CONTIENE",
        subtitle = "CAFEÍNA",
        description = "Estimulante del sistema nervioso. Evitar en niños y personas sensibles."
    );

    companion object {
        fun fromName(name: String): WarningSeal? {
            val normalized = name.trim().uppercase().replace(" ", "_").replace("Í", "I").replace("Á", "A").replace("É", "E").replace("Ú", "U")
            return entries.firstOrNull { 
                it.name == normalized || 
                normalized.contains(it.name) ||
                (normalized.contains("CALORIA") && it == EXCESO_CALORIAS) ||
                (normalized.contains("AZUCAR") && it == EXCESO_AZUCARES) ||
                (normalized.contains("SATURADA") && it == EXCESO_GRASAS_SATURADAS) ||
                (normalized.contains("TRANS") && it == EXCESO_GRASAS_TRANS) ||
                (normalized.contains("SODIO") && it == EXCESO_SODIO) ||
                (normalized.contains("EDULCORANTE") && it == CONTIENE_EDULCORANTES) ||
                (normalized.contains("CAFEINA") && it == CONTIENE_CAFEINA)
            }
        }
    }
}

enum class MealType(val displayName: String, val iconName: String) {
    DESAYUNO("Desayuno", "wb_twilight"),
    ALMUERZO("Almuerzo", "wb_sunny"),
    MERIENDA("Merienda", "coffee"),
    CENA("Cena", "nights_stay"),
    SNACK("Snack / Picoteo", "restaurant")
}

enum class HealthGoal(
    val title: String,
    val description: String,
    val defaultCalories: Int,
    val maxSugarGrams: Float,
    val targetProteinGrams: Float,
    val maxSodiumMg: Float,
    val defaultMaxSeals: Int = 0
) {
    PERDER_GRASA(
        title = "Pérdida de Grasa y Peso",
        description = "Déficit calórico controlado, restricción estricta de azúcares libres y alto contenido de fibra.",
        defaultCalories = 1750,
        maxSugarGrams = 20f,
        targetProteinGrams = 110f,
        maxSodiumMg = 2000f,
        defaultMaxSeals = 0
    ),
    GANAR_MUSCULO(
        title = "Ganancia Muscular e Hipertrofia",
        description = "Superávit energético moderado, alta ingesta proteica y fuentes densas de nutrientes.",
        defaultCalories = 2500,
        maxSugarGrams = 35f,
        targetProteinGrams = 150f,
        maxSodiumMg = 2300f,
        defaultMaxSeals = 1
    ),
    CONTROL_AZUCAR(
        title = "Control Glucémico y Anti-Picos",
        description = "Evitar picos rápidos de glucosa e insulina, minimizar alimentos con sello de Exceso de Azúcar.",
        defaultCalories = 1900,
        maxSugarGrams = 15f,
        targetProteinGrams = 90f,
        maxSodiumMg = 2000f,
        defaultMaxSeals = 0
    ),
    SALUD_CARDIOVASCULAR(
        title = "Salud Cardiovascular y Bajo en Sodio",
        description = "Reducción de sodio, cero grasas trans, grasas monoinsaturadas saludables y protección arterial.",
        defaultCalories = 1950,
        maxSugarGrams = 22f,
        targetProteinGrams = 85f,
        maxSodiumMg = 1500f,
        defaultMaxSeals = 0
    ),
    ALIMENTACION_LIMPIA(
        title = "Alimentación Limpia (Cero Sellos)",
        description = "Comida real no ultraprocesada, cero sellos octogonales y máxima densidad de micronutrientes.",
        defaultCalories = 2000,
        maxSugarGrams = 18f,
        targetProteinGrams = 100f,
        maxSodiumMg = 1800f,
        defaultMaxSeals = 0
    )
}

data class DetailedNutritionReport(
    val foodName: String,
    val portionGrams: Int,
    val caloriesKcal: Int,
    val sugarGrams: Float,
    val carbsGrams: Float,
    val proteinGrams: Float,
    val fatGrams: Float,
    val saturatedFatGrams: Float,
    val sodiumMg: Float,
    val warningSeals: List<WarningSeal>,
    val healthScore: Int, // 1 to 100
    val verdictSummary: String,
    val benefits: List<String>,
    val cons: List<String>,
    val personalizedAdvice: String,
    val mealType: MealType = MealType.ALMUERZO,
    val imageBase64: String? = null
)

data class MetricComparison(
    val label: String,
    val mealValue: Float,
    val todayBeforeValue: Float,
    val projectedValue: Float,
    val targetLimit: Float,
    val unit: String,
    val isExceeded: Boolean,
    val percentageOfDaily: Int
)

data class GoalComparisonResult(
    val hasSealExcess: Boolean,
    val mealSealsCount: Int,
    val todaySealsBefore: Int,
    val projectedTotalSeals: Int,
    val maxDailyWarningSeals: Int,
    val exceededSeals: List<WarningSeal>,
    val sealWarningTitle: String,
    val sealWarningDetail: String,
    val calorieComparison: MetricComparison,
    val sugarComparison: MetricComparison,
    val sodiumComparison: MetricComparison,
    val proteinComparison: MetricComparison,
    val isAnyMetricExceeded: Boolean,
    val actionableAdvice: String
)

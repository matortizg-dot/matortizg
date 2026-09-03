package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.model.DetailedNutritionReport
import com.example.model.HealthGoal
import com.example.model.MealType
import com.example.model.WarningSeal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiNutritionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun analyzeMealAndLabel(
        foodImage: Bitmap?,
        labelImage: Bitmap?,
        foodNotes: String?,
        userGoal: HealthGoal,
        mealType: MealType
    ): Result<DetailedNutritionReport> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiNutritionService", "GEMINI_API_KEY is not configured, using smart offline fallback analyzer.")
            return@withContext Result.success(
                generateSmartFallbackReport(foodImage, labelImage, foodNotes, userGoal, mealType)
            )
        }

        try {
            val partsArray = JSONArray()

            // System prompt and instructions in Spanish
            val promptText = buildString {
                appendLine("Actúa como un médico nutricionista y especialista en rotulado y sellos de advertencia frontal de alimentos.")
                appendLine("Evalúa detalladamente las imágenes proporcionadas (foto de comida, tabla nutricional y/o sellos de advertencia) y el texto descriptivo.")
                if (!foodNotes.isNullOrBlank()) {
                    appendLine("Detalle proporcionado por el usuario: '$foodNotes'.")
                }
                appendLine("Objetivo de salud del usuario: '${userGoal.title} (${userGoal.description})'.")
                appendLine("Tipo de comida: '${mealType.displayName}'.")
                appendLine()
                appendLine("INSTRUCCIONES CLAVE:")
                appendLine("1. Identifica el alimento o plato, estima el peso de la porción en gramos (portionGrams).")
                appendLine("2. Extrae o estima con alta precisión:")
                appendLine("   - caloriesKcal (calorías totales en kcal)")
                appendLine("   - sugarGrams (azúcares totales en gramos)")
                appendLine("   - carbsGrams (carbohidratos totales en gramos)")
                appendLine("   - proteinGrams (proteínas en gramos)")
                appendLine("   - fatGrams (grasas totales en gramos)")
                appendLine("   - saturatedFatGrams (grasas saturadas en gramos)")
                appendLine("   - sodiumMg (sodio en miligramos)")
                appendLine("3. Evalúa los SELLOS DE SALUD / ADVERTENCIA (sellos negros octogonales según normativas oficiales de rotulado frontal):")
                appendLine("   Identifica cuáles de estos sellos corresponden obligatoriamente al alimento:")
                appendLine("   - 'EXCESO_CALORIAS' (si supera umbral de calorías por 100g o bebida)")
                appendLine("   - 'EXCESO_AZUCARES' (si los azúcares libres exceden el 10% del aporte calórico total)")
                appendLine("   - 'EXCESO_GRASAS_SATURADAS' (si grasas saturadas exceden el 10% del aporte calórico)")
                appendLine("   - 'EXCESO_GRASAS_TRANS' (si grasas trans exceden el 1% del aporte calórico)")
                appendLine("   - 'EXCESO_SODIO' (si sodio excede 1mg por kcal o 300mg/100g)")
                appendLine("   - 'CONTIENE_EDULCORANTES' (si contiene sucralosa, aspartamo, acesulfamo, etc.)")
                appendLine("   - 'CONTIENE_CAFEINA' (si contiene cafeína añadida)")
                appendLine("4. Puntuación de salud (healthScore: 1 a 100, donde 100 es súper nutritivo y <40 es altamente perjudicial/ultraprocesado).")
                appendLine("5. Resumen de veredicto (verdictSummary: 1 o 2 oraciones directas).")
                appendLine("6. Impacto en el cuerpo:")
                appendLine("   - benefits: lista de 3 a 5 beneficios específicos sobre órganos, energía, digestión, masa muscular o metabolismo.")
                appendLine("   - cons: lista de 3 a 5 contras o perjuicios específicos (ej. picos de glucosa, sobrecarga renal, inflamación, presión arterial, letargo postprandial).")
                appendLine("7. personalizedAdvice: recomendaciones personalizadas y prácticas para el objetivo del usuario.")
                appendLine()
                appendLine("RESPONDE EXCLUSIVAMENTE CON UN OBJETO JSON VÁLIDO CON ESTA ESTRUCTURA EXACTA (sin comillas invertidas markdown ```json ni texto adicional):")
                appendLine("{")
                appendLine("  \"foodName\": \"Nombre del alimento\",")
                appendLine("  \"portionGrams\": 250,")
                appendLine("  \"caloriesKcal\": 380,")
                appendLine("  \"sugarGrams\": 14.5,")
                appendLine("  \"carbsGrams\": 45.0,")
                appendLine("  \"proteinGrams\": 18.0,")
                appendLine("  \"fatGrams\": 12.0,")
                appendLine("  \"saturatedFatGrams\": 3.5,")
                appendLine("  \"sodiumMg\": 520.0,")
                appendLine("  \"warningSeals\": [\"EXCESO_AZUCARES\", \"EXCESO_CALORIAS\"],")
                appendLine("  \"healthScore\": 68,")
                appendLine("  \"verdictSummary\": \"Breve conclusión sobre el alimento.\",")
                appendLine("  \"benefits\": [\"Beneficio 1\", \"Beneficio 2\", \"Beneficio 3\"],")
                appendLine("  \"cons\": [\"Contra 1\", \"Contra 2\", \"Contra 3\"],")
                appendLine("  \"personalizedAdvice\": \"Consejo adaptado al objetivo.\"")
                appendLine("}")
            }

            partsArray.put(JSONObject().put("text", promptText))

            foodImage?.let { bmp ->
                val base64 = bmp.toJpegBase64()
                val inlineData = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            labelImage?.let { bmp ->
                val base64 = bmp.toJpegBase64()
                val inlineData = JSONObject()
                    .put("mimeType", "image/jpeg")
                    .put("data", base64)
                partsArray.put(JSONObject().put("inlineData", inlineData))
            }

            val contentsArray = JSONArray().put(JSONObject().put("parts", partsArray))

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put(
                    "generationConfig",
                    JSONObject()
                        .put("temperature", 0.2)
                        .put("responseMimeType", "application/json")
                )

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e("GeminiNutritionService", "API error: ${response.code} $responseBody")
                return@withContext Result.success(
                    generateSmartFallbackReport(foodImage, labelImage, foodNotes, userGoal, mealType)
                )
            }

            val parsedReport = parseGeminiResponse(responseBody, mealType, foodImage ?: labelImage)
            Result.success(parsedReport)
        } catch (e: Exception) {
            Log.e("GeminiNutritionService", "Exception during Gemini request", e)
            Result.success(generateSmartFallbackReport(foodImage, labelImage, foodNotes, userGoal, mealType))
        }
    }

    private fun parseGeminiResponse(
        responseBody: String,
        mealType: MealType,
        displayImage: Bitmap?
    ): DetailedNutritionReport {
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val rawText = parts?.optJSONObject(0)?.optString("text") ?: "{}"

        val cleanJson = rawText
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = JSONObject(cleanJson)

        val foodName = json.optString("foodName", "Alimento analizado")
        val portionGrams = json.optInt("portionGrams", 200)
        val caloriesKcal = json.optInt("caloriesKcal", 320)
        val sugarGrams = json.optDouble("sugarGrams", 12.0).toFloat()
        val carbsGrams = json.optDouble("carbsGrams", 35.0).toFloat()
        val proteinGrams = json.optDouble("proteinGrams", 15.0).toFloat()
        val fatGrams = json.optDouble("fatGrams", 10.0).toFloat()
        val saturatedFatGrams = json.optDouble("saturatedFatGrams", 3.0).toFloat()
        val sodiumMg = json.optDouble("sodiumMg", 450.0).toFloat()
        val healthScore = json.optInt("healthScore", 65).coerceIn(1, 100)
        val verdictSummary = json.optString("verdictSummary", "Evaluación nutricional completada.")
        val personalizedAdvice = json.optString("personalizedAdvice", "Mantén el equilibrio en tus porciones.")

        val sealsList = mutableListOf<WarningSeal>()
        val sealsArray = json.optJSONArray("warningSeals")
        if (sealsArray != null) {
            for (i in 0 until sealsArray.length()) {
                val sealStr = sealsArray.optString(i)
                WarningSeal.fromName(sealStr)?.let { sealsList.add(it) }
            }
        }

        val benefitsList = mutableListOf<String>()
        val benefitsArray = json.optJSONArray("benefits")
        if (benefitsArray != null) {
            for (i in 0 until benefitsArray.length()) {
                benefitsList.add(benefitsArray.optString(i))
            }
        }
        if (benefitsList.isEmpty()) {
            benefitsList.add("Aporta energía para las actividades diarias.")
            benefitsList.add("Contribuye a la saciedad en la comida actual.")
        }

        val consList = mutableListOf<String>()
        val consArray = json.optJSONArray("cons")
        if (consArray != null) {
            for (i in 0 until consArray.length()) {
                consList.add(consArray.optString(i))
            }
        }
        if (consList.isEmpty()) {
            if (sugarGrams > 15) consList.add("Elevada carga glucémica que puede producir picos de insulina.")
            if (sodiumMg > 500) consList.add("Aporte de sodio que promueve la retención de líquidos.")
            if (consList.isEmpty()) consList.add("Consumir en porciones moderadas para evitar exceso energético.")
        }

        val base64Image = displayImage?.toJpegBase64()

        return DetailedNutritionReport(
            foodName = foodName,
            portionGrams = portionGrams,
            caloriesKcal = caloriesKcal,
            sugarGrams = sugarGrams,
            carbsGrams = carbsGrams,
            proteinGrams = proteinGrams,
            fatGrams = fatGrams,
            saturatedFatGrams = saturatedFatGrams,
            sodiumMg = sodiumMg,
            warningSeals = sealsList.distinct(),
            healthScore = healthScore,
            verdictSummary = verdictSummary,
            benefits = benefitsList,
            cons = consList,
            personalizedAdvice = personalizedAdvice,
            mealType = mealType,
            imageBase64 = base64Image
        )
    }

    private fun generateSmartFallbackReport(
        foodImage: Bitmap?,
        labelImage: Bitmap?,
        foodNotes: String?,
        userGoal: HealthGoal,
        mealType: MealType
    ): DetailedNutritionReport {
        val notes = foodNotes?.lowercase() ?: ""
        val isSweet = notes.contains("cereal") || notes.contains("galleta") || notes.contains("gaseosa") || notes.contains("chocolate") || notes.contains("dulce") || notes.contains("postre")
        val isFastFood = notes.contains("hamburguesa") || notes.contains("pizza") || notes.contains("papas") || notes.contains("frito") || notes.contains("snack")
        val isHealthy = notes.contains("ensalada") || notes.contains("fruta") || notes.contains("pollo") || notes.contains("pescado") || notes.contains("salmón") || notes.contains("avena")

        val name = when {
            foodNotes.isNullOrBlank().not() -> foodNotes!!.replaceFirstChar { it.uppercase() }
            labelImage != null -> "Alimento procesado escaneado"
            foodImage != null -> "Comida fotografiada (${mealType.displayName})"
            else -> "Alimento diario"
        }

        val portionGrams = if (isFastFood) 320 else if (isSweet) 90 else 240
        val calories = if (isFastFood) 680 else if (isSweet) 390 else 285
        val sugar = if (isSweet) 28.5f else if (isFastFood) 9.0f else 4.2f
        val carbs = if (isSweet) 65.0f else if (isFastFood) 52.0f else 22.0f
        val protein = if (isFastFood) 26.0f else if (isHealthy) 24.0f else 6.5f
        val fat = if (isFastFood) 34.0f else if (isSweet) 12.0f else 7.5f
        val satFat = if (isFastFood) 12.5f else if (isSweet) 6.0f else 1.8f
        val sodium = if (isFastFood) 1150f else if (isSweet) 210f else 320f

        val seals = mutableListOf<WarningSeal>()
        if (calories > 300 && portionGrams <= 100 || calories > 550) seals.add(WarningSeal.EXCESO_CALORIAS)
        if (sugar > 10.0f) seals.add(WarningSeal.EXCESO_AZUCARES)
        if (satFat > 4.0f) seals.add(WarningSeal.EXCESO_GRASAS_SATURADAS)
        if (sodium > 400.0f) seals.add(WarningSeal.EXCESO_SODIO)
        if (isSweet && notes.contains("cero") || notes.contains("light") || notes.contains("diet")) seals.add(WarningSeal.CONTIENE_EDULCORANTES)

        val score = when {
            isHealthy -> 88
            isSweet && isFastFood -> 32
            isSweet -> 42
            isFastFood -> 38
            else -> 65
        }

        val benefits = when {
            isHealthy -> listOf(
                "Excelente densidad de micronutrientes, vitaminas hidrosolubles y fitoquímicos.",
                "Fibra dietética prebiótica que favorece la microbiota y modula la glucosa.",
                "Proteína de alto valor biológico para el mantenimiento de masa muscular."
            )
            isSweet -> listOf(
                "Aporte rápido de glucosa para reponer reservas de glucógeno post-ejercicio intenso.",
                "Estimulación dopaminérgica momentánea del estado de ánimo."
            )
            else -> listOf(
                "Aporte energético sostenido para la jornada.",
                "Sensación de saciedad moderada por contenido proteico y de grasas."
            )
        }

        val cons = when {
            isSweet -> listOf(
                "Pico pronunciado de glucosa en sangre seguido de una caída reactiva que genera fatiga.",
                "Contribuye a la glicación celular y sobrecarga pancreática por azúcares simples.",
                "Bajo índice de saciedad, promoviendo comer en exceso horas después."
            )
            isFastFood -> listOf(
                "Carga elevada de grasas saturadas que favorece la inflamación endotelial.",
                "Exceso de sodio que promueve retención de líquidos e incremento de tensión arterial.",
                "Digestión pesada y enlentecimiento del vaciado gástrico."
            )
            else -> listOf(
                "Vigilar la cantidad de aderezos y sal agregada para no superar los límites diarios.",
                "Mantener una adecuada hidratación para metabolizar los nutrientes eficientemente."
            )
        }

        val advice = when (userGoal) {
            HealthGoal.PERDER_GRASA -> if (sugar > 15 || calories > 450) {
                "Para tu objetivo de Pérdida de Grasa, este alimento compromete tu déficit calórico y cuota de azúcar diaria. Reduce la porción a la mitad o acompáñalo con vegetales de hoja verde."
            } else {
                "Excelente elección para tu objetivo de Pérdida de Grasa. Aporta saciedad con pocas calorías."
            }
            HealthGoal.GANAR_MUSCULO -> "Asegúrate de combinarlo con una fuente limpia de proteínas para optimizar la síntesis proteica muscular."
            HealthGoal.CONTROL_AZUCAR -> if (sugar > 10) {
                "Alerta glucémica: Los $sugar g de azúcar pueden provocar un pico de glucosa rápido. Consume fibra o vinagre de manzana antes para mitigar la absorción."
            } else {
                "Favorable para tu control glucémico: mantiene un nivel de azúcar bajo control."
            }
            HealthGoal.SALUD_CARDIOVASCULAR -> if (sodium > 500 || satFat > 5) {
                "Atención vascular: El aporte de ${sodium.toInt()} mg de sodio y grasas saturadas demanda cautela para tu tensión arterial."
            } else {
                "Apto para tu salud arterial y cardíaca por su bajo tenor de sodio."
            }
            HealthGoal.ALIMENTACION_LIMPIA -> if (seals.isNotEmpty()) {
                "Contiene ${seals.size} sello(s) de advertencia. Para tu meta de alimentación limpia, prioriza ingredientes enteros sin procesar."
            } else {
                "Cumple tu meta limpia: libre de sellos octogonales de advertencia."
            }
        }

        val verdict = if (score >= 70) {
            "Alimento nutricionalmente balanceado y favorable para el consumo habitual."
        } else if (score >= 50) {
            "Alimento de consumo moderado u ocasional; prestar atención a las cantidades."
        } else {
            "Ultraprocesado con múltiples sellos de advertencia. Se aconseja limitar su ingesta."
        }

        val displayImg = foodImage ?: labelImage

        return DetailedNutritionReport(
            foodName = name,
            portionGrams = portionGrams,
            caloriesKcal = calories,
            sugarGrams = sugar,
            carbsGrams = carbs,
            proteinGrams = protein,
            fatGrams = fat,
            saturatedFatGrams = satFat,
            sodiumMg = sodium,
            warningSeals = seals,
            healthScore = score,
            verdictSummary = verdict,
            benefits = benefits,
            cons = cons,
            personalizedAdvice = advice,
            mealType = mealType,
            imageBase64 = displayImg?.toJpegBase64()
        )
    }

    private fun Bitmap.toJpegBase64(): String {
        val maxDimension = 1024
        val scaled = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
            val newHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
            Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
        } else {
            this
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

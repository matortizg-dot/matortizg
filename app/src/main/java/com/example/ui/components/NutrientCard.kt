package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetailedNutritionReport
import java.util.Locale

@Composable
fun PrimaryNutrientHighlights(
    report: DetailedNutritionReport,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Valores Clave de Ingesta",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // 3 Key Cards: Grams, Calories, Sugar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Grams
            HighlightMetricCard(
                title = "Gramos",
                value = "${report.portionGrams}g",
                subtitle = "Porción total",
                icon = Icons.Default.Scale,
                accentColor = Color(0xFF00897B),
                containerColor = Color(0xFFE0F2F1),
                modifier = Modifier.weight(1f).testTag("grams_metric_card")
            )

            // Calories
            HighlightMetricCard(
                title = "Calorías",
                value = "${report.caloriesKcal}",
                subtitle = "kcal totales",
                icon = Icons.Default.ElectricBolt,
                accentColor = Color(0xFFE65100),
                containerColor = Color(0xFFFBE9E7),
                modifier = Modifier.weight(1f).testTag("calories_metric_card")
            )

            // Sugar
            val isHighSugar = report.sugarGrams > 15f
            HighlightMetricCard(
                title = "Azúcar",
                value = String.format(Locale.US, "%.1fg", report.sugarGrams),
                subtitle = if (isHighSugar) "¡Nivel Alto!" else "Moderado",
                icon = Icons.Default.WaterDrop,
                accentColor = if (isHighSugar) Color(0xFFC62828) else Color(0xFF1565C0),
                containerColor = if (isHighSugar) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                modifier = Modifier.weight(1f).testTag("sugar_metric_card")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Detailed Macronutrient Breakdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("macronutrients_breakdown_card"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Desglose Macronutricional Completo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                MacroProgressRow(
                    label = "Proteínas",
                    value = String.format(Locale.US, "%.1f g", report.proteinGrams),
                    progress = (report.proteinGrams / 60f).coerceIn(0f, 1f),
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(8.dp))

                MacroProgressRow(
                    label = "Carbohidratos",
                    value = String.format(Locale.US, "%.1f g", report.carbsGrams),
                    progress = (report.carbsGrams / 120f).coerceIn(0f, 1f),
                    color = Color(0xFFF57C00)
                )
                Spacer(modifier = Modifier.height(8.dp))

                MacroProgressRow(
                    label = "Grasas Totales",
                    value = String.format(Locale.US, "%.1f g", report.fatGrams),
                    progress = (report.fatGrams / 50f).coerceIn(0f, 1f),
                    color = Color(0xFFC2185B)
                )
                Spacer(modifier = Modifier.height(8.dp))

                MacroProgressRow(
                    label = "Grasas Saturadas",
                    value = String.format(Locale.US, "%.1f g", report.saturatedFatGrams),
                    progress = (report.saturatedFatGrams / 20f).coerceIn(0f, 1f),
                    color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.height(8.dp))

                MacroProgressRow(
                    label = "Sodio",
                    value = String.format(Locale.US, "%.0f mg", report.sodiumMg),
                    progress = (report.sodiumMg / 2000f).coerceIn(0f, 1f),
                    color = Color(0xFF5C6BC0)
                )
            }
        }
    }
}

@Composable
fun HighlightMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor.copy(alpha = 0.9f)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun MacroProgressRow(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

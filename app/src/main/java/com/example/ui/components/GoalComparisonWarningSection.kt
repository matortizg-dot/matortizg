package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GoalComparisonResult
import com.example.model.MetricComparison
import java.util.Locale

@Composable
fun GoalComparisonWarningSection(
    comparison: GoalComparisonResult,
    modifier: Modifier = Modifier
) {
    var isDetailsExpanded by remember { mutableStateOf(true) }

    val hasCriticalWarning = comparison.hasSealExcess || comparison.isAnyMetricExceeded

    val headerBgColor = when {
        comparison.hasSealExcess -> Color(0xFFD32F2F)
        comparison.isAnyMetricExceeded -> Color(0xFFE65100)
        else -> Color(0xFF2E7D32)
    }

    val cardBgColor = when {
        comparison.hasSealExcess -> Color(0xFFFFEBEE)
        comparison.isAnyMetricExceeded -> Color(0xFFFFF3E0)
        else -> Color(0xFFE8F5E9)
    }

    val borderColor = when {
        comparison.hasSealExcess -> Color(0xFFE53935)
        comparison.isAnyMetricExceeded -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .testTag("goal_comparison_warning_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Visual Warning / Status Banner Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(headerBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasCriticalWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = headerBgColor
                        ) {
                            Text(
                                text = if (comparison.hasSealExcess) "⚠️ ADVERTENCIA DE SELLOS"
                                else if (comparison.isAnyMetricExceeded) "⚠️ EXCESO DE META DIARIA"
                                else "✅ ALINEADO CON METAS",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = comparison.sealWarningTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color(0xFF1E1E1E)
                        )
                    }
                }

                // Expand/collapse toggle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { isDetailsExpanded = !isDetailsExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDetailsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Alternar detalles",
                        tint = headerBgColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warning explanation detail
            Text(
                text = comparison.sealWarningDetail,
                fontSize = 13.sp,
                color = Color(0xFF37474F),
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )

            // Violated seals badges (if any)
            if (comparison.exceededSeals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sellos detectados en esta porción:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF455A64)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    comparison.exceededSeals.take(3).forEach { seal ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF212121),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${seal.title} ${seal.subtitle}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Expandable Comparison Section
            AnimatedVisibility(
                visible = isDetailsExpanded,
                enter = fadeIn() + expandVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    // Title for breakdown
                    Text(
                        text = "Comparativa: Esta Comida vs Tus Límites Diarios",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF263238)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calorie meter
                    ComparisonMeterItem(
                        metric = comparison.calorieComparison,
                        accentColor = Color(0xFFE65100)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sugar meter
                    ComparisonMeterItem(
                        metric = comparison.sugarComparison,
                        accentColor = Color(0xFFC2185B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sodium meter
                    ComparisonMeterItem(
                        metric = comparison.sodiumComparison,
                        accentColor = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Protein meter
                    ComparisonMeterItem(
                        metric = comparison.proteinComparison,
                        accentColor = Color(0xFF388E3C),
                        isMinimumTarget = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actionable AI Advice Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFF57F17),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Recomendación para Balancear:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF37474F)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comparison.actionableAdvice,
                                    fontSize = 12.sp,
                                    color = Color(0xFF455A64),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonMeterItem(
    metric: MetricComparison,
    accentColor: Color,
    isMinimumTarget: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress = (metric.percentageOfDaily / 100f).coerceIn(0f, 1f)
    val isOverLimit = !isMinimumTarget && (metric.percentageOfDaily > 100 || metric.isExceeded)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF263238)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val formattedMeal = if (metric.mealValue % 1f == 0f) {
                        metric.mealValue.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.1f", metric.mealValue)
                    }
                    val formattedTarget = if (metric.targetLimit % 1f == 0f) {
                        metric.targetLimit.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.1f", metric.targetLimit)
                    }

                    Text(
                        text = "$formattedMeal ${metric.unit} / $formattedTarget ${metric.unit} límite",
                        fontSize = 11.sp,
                        color = Color(0xFF546E7A)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOverLimit) Color(0xFFFFCDD2) else accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isOverLimit) "+${metric.percentageOfDaily}% ⚠️"
                            else "${metric.percentageOfDaily}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isOverLimit) Color(0xFFC62828) else accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isOverLimit) Color(0xFFD32F2F) else accentColor,
                trackColor = Color(0xFFECEFF1),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

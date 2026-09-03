package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EggAlt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FoodLogEntity
import com.example.data.repository.DayIntakeSummary
import com.example.model.WarningSeal
import com.example.ui.NutriScanViewModel
import com.example.ui.components.WarningSealBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyHistoryScreen(
    viewModel: NutriScanViewModel,
    modifier: Modifier = Modifier
) {
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val allLogs by viewModel.allFoodLogs.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var selectedDayFilter by remember { mutableStateOf<DayIntakeSummary?>(null) }
    var logToDelete by remember { mutableStateOf<FoodLogEntity?>(null) }
    // 0: Calorías, 1: Proteína, 2: Carbohidratos, 3: Grasas
    var selectedMetricTab by remember { mutableIntStateOf(0) }

    val filteredLogs = remember(allLogs, selectedDayFilter) {
        if (selectedDayFilter == null) {
            allLogs
        } else {
            val start = selectedDayFilter!!.timestamp
            val end = start + 86400000L - 1L
            allLogs.filter { it.timestamp in start..end }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historial Semanal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calorías y macronutrientes consumidos por día",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Weekly Intake Chart (Interactive 7-day bar representation with metric switcher)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = when (selectedMetricTab) {
                                    0 -> "Calorías por día (kcal)"
                                    1 -> "Proteínas por día (g)"
                                    2 -> "Carbohidratos por día (g)"
                                    else -> "Grasas por día (g)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toca cualquier día para desglosar sus macronutrientes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (selectedDayFilter != null) {
                            TextButton(onClick = { selectedDayFilter = null }) {
                                Text("Ver todos", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metric selector tabs
                    TabRow(
                        selectedTabIndex = selectedMetricTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedMetricTab == 0,
                            onClick = { selectedMetricTab = 0 },
                            text = { Text("Calorías", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedMetricTab == 1,
                            onClick = { selectedMetricTab = 1 },
                            text = { Text("Proteína", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedMetricTab == 2,
                            onClick = { selectedMetricTab = 2 },
                            text = { Text("Carbos", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedMetricTab == 3,
                            onClick = { selectedMetricTab = 3 },
                            text = { Text("Grasas", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7 Day Bars calculation according to selected metric
                    val currentValues = weeklySummary.days.map { day ->
                        when (selectedMetricTab) {
                            0 -> day.totalCalories.toFloat()
                            1 -> day.totalProteinGrams
                            2 -> day.totalCarbsGrams
                            else -> day.totalFatGrams
                        }
                    }
                    val defaultMax = when (selectedMetricTab) {
                        0 -> 2000f
                        1 -> 120f
                        2 -> 250f
                        else -> 80f
                    }
                    val maxValue = (currentValues.maxOrNull() ?: defaultMax).coerceAtLeast(defaultMax * 0.75f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklySummary.days.forEach { day ->
                            val isSelected = selectedDayFilter?.timestamp == day.timestamp
                            val metricVal = when (selectedMetricTab) {
                                0 -> day.totalCalories.toFloat()
                                1 -> day.totalProteinGrams
                                2 -> day.totalCarbsGrams
                                else -> day.totalFatGrams
                            }
                            val heightFraction = if (maxValue > 0) (metricVal / maxValue).coerceIn(0.08f, 1f) else 0.08f

                            val barColor = when (selectedMetricTab) {
                                0 -> when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    day.sealsCount > 0 -> Color(0xFFFF8A65)
                                    day.totalCalories > 0 -> Color(0xFF81C784)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                                1 -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF64B5F6)
                                2 -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFFFB74D)
                                else -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFBA68C8)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        selectedDayFilter = if (isSelected) null else day
                                    }
                                    .padding(horizontal = 2.dp)
                            ) {
                                // Value label
                                if (metricVal > 0) {
                                    Text(
                                        text = if (selectedMetricTab == 0) "${day.totalCalories}" else String.format(Locale.US, "%.0f", metricVal),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                // Bar
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height((105 * heightFraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(barColor)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Day name
                                Text(
                                    text = day.dayOfWeek,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = day.dateLabel,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        when (selectedMetricTab) {
                            0 -> {
                                LegendItem(color = Color(0xFF81C784), label = "Sin sellos")
                                LegendItem(color = Color(0xFFFF8A65), label = "Con sellos")
                                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Seleccionado")
                            }
                            1 -> {
                                LegendItem(color = Color(0xFF64B5F6), label = "Proteína")
                                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Día seleccionado")
                            }
                            2 -> {
                                LegendItem(color = Color(0xFFFFB74D), label = "Carbohidratos")
                                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Día seleccionado")
                            }
                            else -> {
                                LegendItem(color = Color(0xFFBA68C8), label = "Grasas totales")
                                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Día seleccionado")
                            }
                        }
                    }
                }
            }
        }

        // Daily Selected Breakdown Card (or Weekly Average Macronutrients)
        item {
            val focusDay = selectedDayFilter
            if (focusDay != null) {
                // Detailed Macro Breakdown for selected day
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Desglose diario: ${focusDay.dayOfWeek} ${focusDay.dateLabel}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${focusDay.logsCount} comidas registradas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            AssistChip(
                                onClick = { selectedDayFilter = null },
                                label = { Text("Quitar filtro", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DayMacroStat(
                                label = "Calorías",
                                value = "${focusDay.totalCalories}",
                                unit = "kcal",
                                color = Color(0xFFE65100),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Proteína",
                                value = String.format(Locale.US, "%.1f", focusDay.totalProteinGrams),
                                unit = "g",
                                color = Color(0xFF1565C0),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Carbos",
                                value = String.format(Locale.US, "%.1f", focusDay.totalCarbsGrams),
                                unit = "g",
                                color = Color(0xFFE65100),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Grasas",
                                value = String.format(Locale.US, "%.1f", focusDay.totalFatGrams),
                                unit = "g",
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress to daily calorie target
                        val calProgress = (focusDay.totalCalories.toFloat() / userProfile.targetCalories.toFloat()).coerceIn(0f, 1.2f)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progreso calórico hacia la meta (${userProfile.targetCalories} kcal)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${((focusDay.totalCalories.toFloat() / userProfile.targetCalories) * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (focusDay.totalCalories > userProfile.targetCalories * 1.1f) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { calProgress.coerceAtMost(1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (focusDay.totalCalories > userProfile.targetCalories * 1.1f) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Weekly Average Macronutrients Overview
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Promedio Diario de Macronutrientes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DayMacroStat(
                                label = "Proteína",
                                value = String.format(Locale.US, "%.1f", weeklySummary.averageDailyProteinGrams),
                                unit = "g/d",
                                color = Color(0xFF1565C0),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Carbohidratos",
                                value = String.format(Locale.US, "%.1f", weeklySummary.averageDailyCarbsGrams),
                                unit = "g/d",
                                color = Color(0xFFE65100),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Grasas",
                                value = String.format(Locale.US, "%.1f", weeklySummary.averageDailyFatGrams),
                                unit = "g/d",
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.weight(1f)
                            )
                            DayMacroStat(
                                label = "Azúcar",
                                value = String.format(Locale.US, "%.1f", weeklySummary.averageDailySugarGrams),
                                unit = "g/d",
                                color = if (weeklySummary.averageDailySugarGrams > userProfile.maxSugarGrams) Color(0xFFC62828) else Color(0xFF00695C),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Weekly Metric Cards Summary: Calories, Sugar, Warning Seals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avg Calories
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calorías/d",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${weeklySummary.averageDailyCalories} kcal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBF360C)
                        )
                        Text(
                            text = "Meta: ${userProfile.targetCalories} kcal",
                            fontSize = 10.sp,
                            color = Color(0xFFE65100).copy(alpha = 0.8f)
                        )
                    }
                }

                // Avg Sugar
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Azúcar/d",
                                fontSize = 11.sp,
                                color = Color(0xFF1565C0),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f g", weeklySummary.averageDailySugarGrams),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0D47A1)
                        )
                        Text(
                            text = "Límite: ${userProfile.maxSugarGrams.toInt()} g",
                            fontSize = 10.sp,
                            color = Color(0xFF1565C0).copy(alpha = 0.8f)
                        )
                    }
                }

                // Total Warning Seals
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sellos",
                                fontSize = 11.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${weeklySummary.totalWarningSeals}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFB71C1C)
                        )
                        Text(
                            text = if (weeklySummary.totalWarningSeals == 0) "¡Limpio!" else "Acumulados",
                            fontSize = 10.sp,
                            color = Color(0xFFC62828).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section Title: Food logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDayFilter == null) "Comidas Registradas (${filteredLogs.size})" else "Comidas de ${selectedDayFilter!!.dayOfWeek} ${selectedDayFilter!!.dateLabel} (${filteredLogs.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .testTag("empty_history_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay registros en este período",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ve a la pestaña de 'Escanear' para fotografiar tu plato o tabla nutricional y guardarlo.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                FoodLogItemCard(
                    log = log,
                    onDeleteClick = { logToDelete = log }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    logToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("¿Eliminar registro?") },
            text = { Text("Se eliminará '${item.foodName}' del historial semanal.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLog(item.id)
                        logToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodLogItemCard(
    log: FoodLogEntity,
    onDeleteClick: () -> Unit
) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("EEE d MMM • HH:mm", Locale("es", "ES"))
        sdf.format(Date(log.timestamp)).replaceFirstChar { it.uppercase() }
    }

    val seals = remember(log.warningSeals) {
        if (log.warningSeals.isBlank()) emptyList()
        else log.warningSeals.split(",").mapNotNull { WarningSeal.fromName(it) }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_log_item_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            val itemBitmap: Bitmap? = remember(log.imageBase64) {
                log.imageBase64?.let { b64 ->
                    try {
                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (itemBitmap != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                bitmap = itemBitmap.asImageBitmap(),
                                contentDescription = log.foodName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Text(
                            text = log.foodName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$dateStr • ${log.mealType}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delete action button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlight pills: Grams, Calories, Sugar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricPill(
                    label = "Porción",
                    value = "${log.portionGrams}g",
                    containerColor = Color(0xFFE0F2F1),
                    contentColor = Color(0xFF00695C)
                )
                MetricPill(
                    label = "Calorías",
                    value = "${log.caloriesKcal} kcal",
                    containerColor = Color(0xFFFBE9E7),
                    contentColor = Color(0xFFD84315)
                )
                MetricPill(
                    label = "Azúcar",
                    value = String.format(Locale.US, "%.1fg", log.sugarGrams),
                    containerColor = if (log.sugarGrams > 15) Color(0xFFFFEBEE) else Color(0xFFE3F2FD),
                    contentColor = if (log.sugarGrams > 15) Color(0xFFC62828) else Color(0xFF1565C0)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Macronutrient details: Protein, Carbs, Fat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricPill(
                    label = "Prot",
                    value = String.format(Locale.US, "%.1fg", log.proteinGrams),
                    containerColor = Color(0xFFE8EAF6),
                    contentColor = Color(0xFF283593)
                )
                MetricPill(
                    label = "Carb",
                    value = String.format(Locale.US, "%.1fg", log.carbsGrams),
                    containerColor = Color(0xFFFFF8E1),
                    contentColor = Color(0xFFF57F17)
                )
                MetricPill(
                    label = "Grasa",
                    value = String.format(Locale.US, "%.1fg", log.fatGrams),
                    containerColor = Color(0xFFF3E5F5),
                    contentColor = Color(0xFF6A1B9A)
                )
            }

            // Warning Seals in item
            if (seals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    seals.forEach { seal ->
                        WarningSealBadge(seal = seal, size = 52.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = contentColor.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DayMacroStat(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = unit,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = color.copy(alpha = 0.75f)
                )
            }
        }
    }
}


package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WarningSeal

val OctagonShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val cut = w * 0.2929f // (1 - 1/sqrt(2)) = approx 0.2929

    moveTo(cut, 0f)
    lineTo(w - cut, 0f)
    lineTo(w, cut)
    lineTo(w, h - cut)
    lineTo(w - cut, h)
    lineTo(cut, h)
    lineTo(0f, h - cut)
    lineTo(0f, cut)
    close()
}

@Composable
fun WarningSealBadge(
    seal: WarningSeal,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    onClick: (() -> Unit)? = null
) {
    val isOctagon = seal != WarningSeal.CONTIENE_EDULCORANTES && seal != WarningSeal.CONTIENE_CAFEINA

    if (isOctagon) {
        Box(
            modifier = modifier
                .size(size)
                .clip(OctagonShape)
                .background(Color(0xFF0F0F10))
                .border(2.dp, Color.White, OctagonShape)
                .padding(4.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = seal.title,
                    color = Color.White,
                    fontSize = if (size < 80.dp) 8.sp else 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = seal.subtitle,
                    color = Color.White,
                    fontSize = if (size < 80.dp) 7.sp else 9.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = if (size < 80.dp) 8.sp else 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Warning rectangle for sweeteners or caffeine
        Box(
            modifier = modifier
                .width(size * 1.3f)
                .height(size * 0.55f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F0F10))
                .border(2.dp, Color.White, RoundedCornerShape(4.dp))
                .padding(4.dp)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${seal.title} ${seal.subtitle}",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "EVITAR EN NIÑOS",
                    color = Color(0xFFFFD54F),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WarningSealsRow(
    seals: List<WarningSeal>,
    modifier: Modifier = Modifier
) {
    var selectedSealForDetails by remember { mutableStateOf<WarningSeal?>(null) }

    if (seals.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("no_warning_seals_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E3A2B)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF388E3C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Libre de sellos",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Libre de sellos de advertencia",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "No supera los umbrales críticos de calorías, azúcares, sodio o grasas saturadas.",
                        color = Color(0xFFC8E6C9),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("warning_seals_card"),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF261815)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Sellos de advertencia",
                        tint = Color(0xFFFF7043),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sellos de Salud y Advertencia (${seals.size})",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFCCBC),
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toca cada sello para ver su impacto específico en tu organismo:",
                    fontSize = 12.sp,
                    color = Color(0xFFB0BEC5)
                )
                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    seals.forEach { seal ->
                        WarningSealBadge(
                            seal = seal,
                            size = 88.dp,
                            onClick = { selectedSealForDetails = seal }
                        )
                    }
                }
            }
        }
    }

    selectedSealForDetails?.let { seal ->
        AlertDialog(
            onDismissRequest = { selectedSealForDetails = null },
            icon = {
                WarningSealBadge(seal = seal, size = 70.dp)
            },
            title = {
                Text(
                    text = "${seal.title} ${seal.subtitle}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = seal.description,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Normativa oficial de rotulado frontal para la protección de la salud del consumidor.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSealForDetails = null }) {
                    Text("Entendido")
                }
            }
        )
    }
}

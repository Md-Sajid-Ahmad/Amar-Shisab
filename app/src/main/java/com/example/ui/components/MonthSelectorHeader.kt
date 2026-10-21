package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Navy800
import com.example.ui.theme.White
import com.example.ui.theme.YellowBright
import com.example.ui.theme.YellowGold
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun MonthSelectorHeader(
    currentYearMonth: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectYearMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPickerDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_selector_header"),
        color = Navy800,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_prev_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "পূর্ববর্তী মাস",
                    tint = YellowBright,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showPickerDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("month_picker_trigger")
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = YellowBright,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = DateUtils.formatYearMonthBengali(currentYearMonth),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = White,
                        fontSize = 17.sp
                    )
                )
            }

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_next_month")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "পরবর্তী মাস",
                    tint = YellowBright,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showPickerDialog) {
        MonthYearPickerDialog(
            initialYearMonth = currentYearMonth,
            onDismiss = { showPickerDialog = false },
            onConfirm = { newYm ->
                onSelectYearMonth(newYm)
                showPickerDialog = false
            }
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    initialYearMonth: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = remember(initialYearMonth) {
        try {
            val p = initialYearMonth.split("-")
            Pair(p[0].toInt(), p[1].toInt())
        } catch (e: Exception) {
            Pair(2026, 10)
        }
    }

    var selectedYear by remember { mutableIntStateOf(parts.first) }
    var selectedMonth by remember { mutableIntStateOf(parts.second) }

    val monthNames = listOf(
        "জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল",
        "মে", "জুন", "জুলাই", "আগস্ট",
        "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "মাস ও বছর নির্বাচন করুন",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Year selector row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear-- },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "পূর্ববর্তী বছর"
                        )
                    }

                    Text(
                        text = DateUtils.toBengaliDigits(selectedYear),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )

                    IconButton(
                        onClick = { selectedYear++ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "পরবর্তী বছর"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Months grid (3 columns x 4 rows)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (col in 0 until 3) {
                                val monthIdx = row * 3 + col + 1
                                val isSelected = monthIdx == selectedMonth
                                val monthName = monthNames[monthIdx - 1]

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedMonth = monthIdx },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Navy800 else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = monthName,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) YellowBright else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val formatted = String.format(Locale.US, "%04d-%02d", selectedYear, selectedMonth)
                    onConfirm(formatted)
                }
            ) {
                Text("ঠিক আছে", fontWeight = FontWeight.Bold, color = Navy800)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

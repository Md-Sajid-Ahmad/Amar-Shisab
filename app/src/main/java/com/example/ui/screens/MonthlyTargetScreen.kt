package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MonthlyBudgetEntity
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.theme.Navy800
import com.example.ui.theme.Slate600
import com.example.ui.theme.White
import com.example.ui.theme.YellowBright
import com.example.ui.theme.YellowGold
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.DateUtils

@Composable
fun MonthlyTargetScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val currentBudget by viewModel.currentMonthBudget.collectAsStateWithLifecycle()
    val allBudgets by viewModel.allBudgets.collectAsStateWithLifecycle()

    var targetBudgetInput by remember { mutableStateOf("") }
    var startingBalanceInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    // Synchronize inputs with currentBudget when selectedYearMonth changes
    LaunchedEffect(currentBudget, selectedYearMonth) {
        targetBudgetInput = if (currentBudget != null && currentBudget!!.targetBudget > 0) {
            currentBudget!!.targetBudget.toString()
        } else {
            ""
        }
        startingBalanceInput = if (currentBudget != null && currentBudget!!.startingBalance != 0L) {
            currentBudget!!.startingBalance.toString()
        } else {
            ""
        }
        inputError = null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector Header
        item {
            MonthSelectorHeader(
                currentYearMonth = selectedYearMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                onSelectYearMonth = { viewModel.setSelectedYearMonth(it) }
            )
        }

        // Configure Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_config_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Navy800.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = Navy800,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "বাজেট ও ব্যালেন্স নির্ধারণ",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                )
                            )
                            Text(
                                text = "${DateUtils.formatYearMonthBengali(selectedYearMonth)} মাসের জন্য",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Input 1: Starting Balance
                    Text(
                        text = "প্রারম্ভিক ব্যালেন্স (Starting Balance)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Navy800
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = startingBalanceInput,
                        onValueChange = {
                            startingBalanceInput = it
                            inputError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_starting_balance"),
                        placeholder = { Text("যেমন: ২৫০০০") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Navy800
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "৳",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                ),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy800,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input 2: Monthly Target Budget
                    Text(
                        text = "মাসিক বাজেট লক্ষ্য (Monthly Target Budget)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Navy800
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetBudgetInput,
                        onValueChange = {
                            targetBudgetInput = it
                            inputError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_target_budget"),
                        placeholder = { Text("যেমন: ২০০০০") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = YellowGold
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "৳",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = YellowGold
                                ),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Navy800,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    if (inputError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = inputError!!,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val target = DateUtils.parseAmount(targetBudgetInput) ?: 0L
                            val starting = DateUtils.parseAmount(startingBalanceInput) ?: 0L

                            if (target < 0L) {
                                inputError = "বাজেট লক্ষ্য ০ বা তার বেশি হতে হবে"
                                return@Button
                            }

                            viewModel.saveMonthlyBudget(
                                yearMonth = selectedYearMonth,
                                targetBudget = target,
                                startingBalance = starting,
                                onSuccess = {}
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_save_monthly_budget"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Navy800,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = YellowBright,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বাজেট সেভ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Section: Configured Past Months History
        item {
            Text(
                text = "অন্যান্য মাসের বাজেট তালিকা",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy800
                )
            )
        }

        if (allBudgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এখনও কোনো মাসের বাজেট নির্ধারিত হয়নি",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    }
                }
            }
        } else {
            items(allBudgets, key = { it.yearMonth }) { budget ->
                ConfiguredBudgetCard(
                    budget = budget,
                    isSelected = budget.yearMonth == selectedYearMonth,
                    onSelect = { viewModel.setSelectedYearMonth(budget.yearMonth) }
                )
            }
        }
    }
}

@Composable
private fun ConfiguredBudgetCard(
    budget: MonthlyBudgetEntity,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("configured_budget_${budget.yearMonth}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Navy800.copy(alpha = 0.06f) else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Navy800) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Navy800,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = DateUtils.formatYearMonthBengali(budget.yearMonth),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Navy800
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "প্রারম্ভিক: ${DateUtils.formatTaka(budget.startingBalance)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                    Text(
                        text = "বাজেট: ${DateUtils.formatTaka(budget.targetBudget)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = YellowGold,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) Navy800 else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected) White else Navy800
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isSelected) "বর্তমান" else "দেখুন",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

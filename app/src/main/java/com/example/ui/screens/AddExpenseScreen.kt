package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategories
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TYPE_REFUND
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate600
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White
import com.example.ui.theme.YellowBright
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategories.FOOD.nameBn) }
    var selectedType by remember { mutableStateOf(TYPE_EXPENSE) }
    var notesText by remember { mutableStateOf("") }

    var amountError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    val isExpense = selectedType == TYPE_EXPENSE

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("add_expense_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Transaction Type Segmented Toggle
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("type_segmented_button")
        ) {
            SegmentedButton(
                selected = selectedType == TYPE_EXPENSE,
                onClick = { selectedType = TYPE_EXPENSE },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = Navy800,
                    activeContentColor = White,
                    inactiveContainerColor = White,
                    inactiveContentColor = Navy800
                )
            ) {
                Text("খরচ (Expense)", fontWeight = FontWeight.Bold)
            }

            SegmentedButton(
                selected = selectedType == TYPE_REFUND,
                onClick = { selectedType = TYPE_REFUND },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = SuccessGreen,
                    activeContentColor = White,
                    inactiveContainerColor = White,
                    inactiveContentColor = SuccessGreen
                )
            ) {
                Text("রিফান্ড / ফেরত (Refund)", fontWeight = FontWeight.Bold)
            }
        }

        // Amount Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isExpense) "খরচের পরিমাণ (BDT, ৳) *" else "রিফান্ডের পরিমাণ (BDT, ৳) *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_amount"),
                    placeholder = { Text("০.০০", fontSize = 24.sp, color = MaterialTheme.colorScheme.outline) },
                    leadingIcon = {
                        Text(
                            text = "৳",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isExpense) DangerRed else SuccessGreen
                            ),
                            modifier = Modifier.padding(start = 14.dp, end = 4.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) DangerRed else SuccessGreen
                    ),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(text = amountError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isExpense) Navy800 else SuccessGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // Description and Category Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Description Field
                Text(
                    text = "বিবরণ (Description) *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it
                        descriptionError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_description"),
                    placeholder = { Text("যেমন: সকালের নাস্তা, কাঁচাবাজার, রিকশা ভাড়া...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Navy800
                        )
                    },
                    singleLine = true,
                    isError = descriptionError != null,
                    supportingText = {
                        if (descriptionError != null) {
                            Text(text = descriptionError!!, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy800,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "ক্যাটাগরি নির্বাচন করুন (Category)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable category chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategories.ALL.forEach { cat ->
                        val isSelected = selectedCategory == cat.nameBn
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.nameBn },
                            label = {
                                Text(
                                    text = cat.nameBn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) White else cat.color
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy800,
                                selectedLabelColor = White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Optional Notes
                Text(
                    text = "অতিরিক্ত নোট (ঐচ্ছিক)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Slate600
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_expense_notes"),
                    placeholder = { Text("স্থান, বিল নম্বর বা অন্যান্য তথ্য...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = Slate600
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Navy800,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }

        // Automatic Timestamp Info Notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "• সময় ও তারিখ স্বয়ংক্রিয়ভাবে Asia/Dhaka লোকাল টাইমে রেকর্ড করা হবে। ম্যানুয়াল এন্ট্রি প্রয়োজন নেই।",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                )
            }
        }

        // Submit Button: "খরচ সেভ করুন" / "রিফান্ড সেভ করুন"
        Button(
            onClick = {
                val parsedAmount = DateUtils.parseAmount(amountText)
                if (parsedAmount == null || parsedAmount <= 0L) {
                    amountError = "সঠিক পরিমাণ লিখুন (০ এর চেয়ে বেশি হতে হবে)"
                    return@Button
                }
                if (descriptionText.isBlank()) {
                    descriptionError = "খরচের বিবরণ অবশ্যই লিখুন"
                    return@Button
                }

                viewModel.addTransaction(
                    amount = parsedAmount,
                    description = descriptionText.trim(),
                    category = selectedCategory,
                    type = selectedType,
                    notes = notesText.trim(),
                    onSuccess = {
                        // Clear form only after success
                        amountText = ""
                        descriptionText = ""
                        notesText = ""
                        amountError = null
                        descriptionError = null
                        onNavigateBack()
                    }
                )
            },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("btn_save_expense"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isExpense) Navy800 else SuccessGreen,
                contentColor = White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("সংরক্ষণ করা হচ্ছে...", fontWeight = FontWeight.Bold)
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = YellowBright,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExpense) "খরচ সেভ করুন" else "রিফান্ড সেভ করুন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }
}

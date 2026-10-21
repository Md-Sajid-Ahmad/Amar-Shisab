package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategories
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TYPE_REFUND
import com.example.data.model.TransactionEntity
import com.example.ui.components.MonthSelectorHeader
import com.example.ui.components.TransactionItemCard
import com.example.ui.theme.DangerRed
import com.example.ui.theme.Navy800
import com.example.ui.theme.Slate600
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.White
import com.example.ui.theme.YellowBright
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.SortOrder
import com.example.util.DateUtils

@Composable
fun TransactionHistoryScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transaction_history_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Month Selector Header
        MonthSelectorHeader(
            currentYearMonth = selectedYearMonth,
            onPreviousMonth = { viewModel.previousMonth() },
            onNextMonth = { viewModel.nextMonth() },
            onSelectYearMonth = { viewModel.setSelectedYearMonth(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar & Sort Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_transactions_input"),
                placeholder = { Text("লেনদেন খুঁজুন...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "সার্চ করুন",
                        tint = Slate600
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "পরিষ্কার করুন"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Navy800,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Toggle Button
            OutlinedButton(
                onClick = {
                    val nextOrder = if (sortOrder == SortOrder.NEWEST_FIRST) {
                        SortOrder.OLDEST_FIRST
                    } else {
                        SortOrder.NEWEST_FIRST
                    }
                    viewModel.setSortOrder(nextOrder)
                },
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                modifier = Modifier.testTag("btn_sort_transactions")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "সর্ট করুন",
                    tint = Navy800,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (sortOrder == SortOrder.NEWEST_FIRST) "নতুন আগে" else "পুরাতন আগে",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Navy800
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isAllSelected = selectedCategory == null
            FilterChip(
                selected = isAllSelected,
                onClick = { viewModel.setCategoryFilter(null) },
                label = { Text("সকল ক্যাটাগরি", fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(10.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Navy800,
                    selectedLabelColor = White
                )
            )

            ExpenseCategories.ALL.forEach { cat ->
                val isSelected = selectedCategory == cat.nameBn
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.setCategoryFilter(if (isSelected) null else cat.nameBn)
                    },
                    label = { Text(cat.nameBn, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Navy800,
                        selectedLabelColor = White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Transactions List or Empty State
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Slate600,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != null) {
                                "কোনো ফলাফল পাওয়া যায়নি"
                            } else {
                                "এই মাসে কোনো লেনদেন নেই"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Navy800
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "অন্য শব্দ দিয়ে চেষ্টা করুন" else "নতুন খরচ যুক্ত করতে ড্যাশবোর্ডে যান",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->
                    TransactionItemCard(
                        transaction = tx,
                        onEdit = { transactionToEdit = it },
                        onDelete = { transactionToDelete = it }
                    )
                }
            }
        }
    }

    // Edit Transaction Dialog
    if (transactionToEdit != null) {
        EditTransactionDialog(
            transaction = transactionToEdit!!,
            onDismiss = { transactionToEdit = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(updated) {
                    transactionToEdit = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "লেনদেন মুছে ফেলা",
                    fontWeight = FontWeight.Bold,
                    color = DangerRed
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে '${transactionToDelete!!.description}' লেনদেনটি মুছে ফেলতে চান? এটি মুছে ফেললে মোট খরচের হিসাব তাৎক্ষণিকভাবে সমন্বয় হবে।"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transactionToDelete!!)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("মুছে ফেলুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun EditTransactionDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit
) {
    var amountText by remember { mutableStateOf(transaction.amount.toString()) }
    var descriptionText by remember { mutableStateOf(transaction.description) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var notesText by remember { mutableStateOf(transaction.notes) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isExpense = selectedType == TYPE_EXPENSE

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "লেনদেন সম্পাদনা করুন",
                fontWeight = FontWeight.Bold,
                color = Navy800
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorText = null
                    },
                    label = { Text("পরিমাণ (BDT, ৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it
                        errorText = null
                    },
                    label = { Text("বিবরণ") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Text(
                    text = "ক্যাটাগরি",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategories.ALL.forEach { cat ->
                        val isSelected = selectedCategory == cat.nameBn
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat.nameBn },
                            label = { Text(cat.nameBn, fontSize = 12.sp) }
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("নোট (ঐচ্ছিক)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = DateUtils.parseAmount(amountText)
                    if (amt == null || amt <= 0L) {
                        errorText = "সঠিক পরিমাণ লিখুন (০ এর বেশি হতে হবে)"
                        return@Button
                    }
                    if (descriptionText.isBlank()) {
                        errorText = "বিবরণ অবশ্যই লিখুন"
                        return@Button
                    }

                    onConfirm(
                        transaction.copy(
                            amount = amt,
                            description = descriptionText.trim(),
                            category = selectedCategory,
                            type = selectedType,
                            notes = notesText.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800)
            ) {
                Text("আপডেট করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

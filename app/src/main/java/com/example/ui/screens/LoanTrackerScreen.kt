package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LOAN_TYPE_BORROWED
import com.example.data.model.LOAN_TYPE_LENT
import com.example.data.model.LoanEntity
import com.example.ui.theme.HindSiliguriFontFamily
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.LocalIsBengali
import com.example.ui.theme.White
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.util.AppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Super aesthetic, clear, and child-friendly Debt & Loan Tracker (ধার-দেনা খাতা).
 * Simple enough for a class-5 student to instantly understand:
 * - 🟢 আমি পাবো (ধার দেওয়া টাকা)
 * - 🔴 আমি দেবো (ধার নেওয়া টাকা)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanTrackerScreen(
    viewModel: ExpenseViewModel,
    onBackToDaily: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val isBn = LocalIsBengali.current

    val filteredLoans by viewModel.filteredLoans.collectAsStateWithLifecycle()
    val totalLent by viewModel.totalLentRemaining.collectAsStateWithLifecycle()
    val totalBorrowed by viewModel.totalBorrowedRemaining.collectAsStateWithLifecycle()
    val currentFilter by viewModel.loanFilterType.collectAsStateWithLifecycle()
    val searchQuery by viewModel.loanSearchQuery.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueLoanCount.collectAsStateWithLifecycle()

    var showAddLoanSheet by remember { mutableStateOf(false) }
    var presetLoanType by remember { mutableStateOf(LOAN_TYPE_LENT) }
    var loanForPayment by remember { mutableStateOf<LoanEntity?>(null) }
    var loanToDelete by remember { mutableStateOf<LoanEntity?>(null) }

    // System back returns to Daily Ledger
    BackHandler {
        onBackToDaily()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. AESTHETIC SUMMARY HEADER: পাবো vs দেবো + ব্যালেন্স
            item {
                KidFriendlySummaryHeader(
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    overdueCount = overdueCount,
                    isBn = isBn
                )
            }

            // 2. TWO GIANT INTUITIVE ACTION BUTTONS: "টাকা দিলাম (পাবো)" & "টাকা নিলাম (দেবো)"
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Green: টাকা ধার দিলাম (আমি পাবো)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                presetLoanType = LOAN_TYPE_LENT
                                showAddLoanSheet = true
                            }
                            .testTag("btn_quick_lent"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF10B981),
                                            Color(0xFF059669)
                                        )
                                    )
                                )
                                .padding(vertical = 14.dp, horizontal = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "ধার দিলাম" else "Lent (+)",
                                        fontFamily = HindSiliguriFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = White
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBn) "টাকা ফেরত পাবো 🟢" else "I will receive 🟢",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = White.copy(alpha = 0.95f)
                                )
                            }
                        }
                    }

                    // Coral/Red: টাকা ধার নিলাম (আমাকে দিতে হবে)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                presetLoanType = LOAN_TYPE_BORROWED
                                showAddLoanSheet = true
                            }
                            .testTag("btn_quick_borrowed"),
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFFF43F5E),
                                            Color(0xFFE11D48)
                                        )
                                    )
                                )
                                .padding(vertical = 14.dp, horizontal = 10.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(White.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "ধার নিলাম" else "Borrowed (-)",
                                        fontFamily = HindSiliguriFontFamily,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = White
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBn) "টাকা শোধ দিতে হবে 🔴" else "I will repay 🔴",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = White.copy(alpha = 0.95f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. SEARCH & EASY FILTER PILLS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search box with clean aesthetic styling
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setLoanSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_loan"),
                        placeholder = {
                            Text(
                                text = if (isBn) "🔍 নাম বা মোবাইল নাম্বার দিয়ে খুঁজুন..." else "🔍 Search by name or phone...",
                                fontFamily = HindSiliguriFontFamily,
                                color = theme.textMuted.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setLoanSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = theme.textMuted
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = White,
                            unfocusedContainerColor = White,
                            focusedBorderColor = theme.primary,
                            unfocusedBorderColor = theme.border
                        )
                    )

                    // Filter chips row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SimpleFilterPill(
                                label = if (isBn) "সব হিসাব (${filteredLoans.size})" else "All",
                                isSelected = currentFilter == "ALL",
                                onClick = { viewModel.setLoanFilter("ALL") }
                            )
                        }
                        item {
                            SimpleFilterPill(
                                label = if (isBn) "🟢 আমি পাবো" else "🟢 To Receive",
                                isSelected = currentFilter == "LENT",
                                activeColor = Color(0xFF10B981),
                                onClick = { viewModel.setLoanFilter("LENT") }
                            )
                        }
                        item {
                            SimpleFilterPill(
                                label = if (isBn) "🔴 আমি দেবো" else "🔴 To Pay",
                                isSelected = currentFilter == "BORROWED",
                                activeColor = Color(0xFFF43F5E),
                                onClick = { viewModel.setLoanFilter("BORROWED") }
                            )
                        }
                        item {
                            SimpleFilterPill(
                                label = if (isBn) "⏰ তাগাদা/বাকি" else "⏰ Due",
                                isSelected = currentFilter == "OVERDUE",
                                activeColor = Color(0xFFD97706),
                                onClick = { viewModel.setLoanFilter("OVERDUE") }
                            )
                        }
                        item {
                            SimpleFilterPill(
                                label = if (isBn) "🎉 শোধ হয়েছে" else "🎉 Settled",
                                isSelected = currentFilter == "SETTLED",
                                activeColor = theme.primaryDark,
                                onClick = { viewModel.setLoanFilter("SETTLED") }
                            )
                        }
                    }
                }
            }

            // 4. LOAN LIST
            if (filteredLoans.isEmpty()) {
                item {
                    EmptyLoanView(
                        isBn = isBn,
                        onAddClick = {
                            presetLoanType = LOAN_TYPE_LENT
                            showAddLoanSheet = true
                        }
                    )
                }
            } else {
                items(filteredLoans, key = { it.id }) { loan ->
                    SimpleLoanCard(
                        loan = loan,
                        isBn = isBn,
                        onPaymentClick = { loanForPayment = loan },
                        onDeleteClick = { loanToDelete = loan },
                        onSettleToggle = { viewModel.settleLoan(loan.id, !loan.isSettled) },
                        onCallClick = { phone -> dialPhoneNumber(context, phone) },
                        onSmsClick = { phone, name, rem, due ->
                            val msg = AppStrings.buildSmsReminderMessage(
                                personName = name,
                                remainingAmount = rem,
                                dueDateFormatted = due
                            )
                            sendSmsReminder(context, phone, msg)
                        }
                    )
                }
            }
        }

        // Floating Action Button for Adding Loan
        ExtendedFloatingActionButton(
            onClick = {
                presetLoanType = LOAN_TYPE_LENT
                showAddLoanSheet = true
            },
            containerColor = theme.primary,
            contentColor = White,
            icon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Loan",
                    tint = White
                )
            },
            text = {
                Text(
                    text = if (isBn) "নতুন ধার লিখুন" else "Record Loan",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = White
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("fab_add_loan")
        )
    }

    // Modal: Add Loan Bottom Sheet (Easy enough for class-5)
    if (showAddLoanSheet) {
        KidFriendlyAddLoanSheet(
            initialType = presetLoanType,
            onDismiss = { showAddLoanSheet = false },
            onSave = { name, phone, type, amount, dueDate, notes ->
                viewModel.addLoan(
                    personName = name,
                    phoneNumber = phone,
                    type = type,
                    amount = amount,
                    dueDate = dueDate,
                    notes = notes,
                    onSuccess = { showAddLoanSheet = false }
                )
            },
            isBn = isBn
        )
    }

    // Modal: Record Payment Dialog
    loanForPayment?.let { loan ->
        KidFriendlyPaymentDialog(
            loan = loan,
            isBn = isBn,
            onDismiss = { loanForPayment = null },
            onConfirm = { paymentAmount ->
                viewModel.recordLoanPayment(
                    loanId = loan.id,
                    paymentAmount = paymentAmount,
                    onSuccess = { loanForPayment = null }
                )
            }
        )
    }

    // Modal: Delete Dialog
    loanToDelete?.let { loan ->
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = {
                Text(
                    text = if (isBn) "ধার মুছে ফেলতে চান?" else "Delete Record?",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) {
                        "${loan.personName}-এর ৳${loan.amount} টাকার ধারের হিসাবটি কি তালিকা থেকে মুছে ফেলবেন?"
                    } else {
                        "Are you sure you want to delete the loan record of ৳${loan.amount} for ${loan.personName}?"
                    },
                    fontFamily = HindSiliguriFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLoan(loan) {
                            loanToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.expense)
                ) {
                    Text(
                        text = if (isBn) "হ্যাঁ, মুছুন" else "Delete",
                        fontFamily = HindSiliguriFontFamily,
                        color = White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) {
                    Text(
                        text = if (isBn) "না, থাক" else "Cancel",
                        fontFamily = HindSiliguriFontFamily
                    )
                }
            }
        )
    }
}

/**
 * Aesthetic top summary header displaying Total Lent (আমি পাবো) vs Total Borrowed (আমি দেবো)
 * and an overall net balance indicator.
 */
@Composable
private fun KidFriendlySummaryHeader(
    totalLent: Long,
    totalBorrowed: Long,
    overdueCount: Int,
    isBn: Boolean
) {
    val theme = LocalAppTheme.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Two colorful cards side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Lent Card: "আমি পাবো" (Emerald/Green)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_total_lent"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.2.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🙋‍♂️", fontSize = 18.sp)
                        }
                        Text(
                            text = if (isBn) "আমি পাবো" else "To Receive",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "৳ $totalLent",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF15803D)
                    )
                    Text(
                        text = if (isBn) "কাউকে ধার দেওয়া টাকা" else "Money lent out",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
            }

            // Borrowed Card: "আমি দেবো" (Rose/Peach)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("card_total_borrowed"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                border = BorderStroke(1.2.dp, Color(0xFFFECDD3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFE4E6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🤝", fontSize = 18.sp)
                        }
                        Text(
                            text = if (isBn) "আমি দেবো" else "To Pay Back",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBE123C)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "৳ $totalBorrowed",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFBE123C)
                    )
                    Text(
                        text = if (isBn) "কারও থেকে ধার নেওয়া" else "Money borrowed",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                }
            }
        }

        // Net Balance Pill / Friendly summary line
        val net = totalLent - totalBorrowed
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = White,
            border = BorderStroke(1.dp, theme.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "⚖️", fontSize = 16.sp)
                    Text(
                        text = if (isBn) "মোট হিসাবের অবস্থা:" else "Net Status:",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textMuted
                    )
                }
                Text(
                    text = when {
                        net > 0 -> if (isBn) "✨ আপনি ৳$net বেশি পাবেন" else "+৳$net to receive"
                        net < 0 -> if (isBn) "⚠️ আপনি ৳${-net} বেশি দেনাদার" else "-৳${-net} to pay"
                        totalLent == 0L && totalBorrowed == 0L -> if (isBn) "কোনো ধার-দেনা নেই" else "No active debts"
                        else -> if (isBn) "পাওনা ও দেনা সমান সমান" else "Balanced"
                    },
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        net > 0 -> Color(0xFF15803D)
                        net < 0 -> Color(0xFFBE123C)
                        else -> theme.primary
                    }
                )
            }
        }

        // Overdue Alert Banner (If any)
        if (overdueCount > 0) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "⏰", fontSize = 20.sp)
                    Text(
                        text = if (isBn) {
                            "$overdueCount টি ধারের ফেরত দেওয়ার সময় হয়েছে! মনে করিয়ে দিন।"
                        } else {
                            "$overdueCount loan repayment(s) are overdue!"
                        },
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

@Composable
private fun SimpleFilterPill(
    label: String,
    isSelected: Boolean,
    activeColor: Color = LocalAppTheme.current.primary,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) activeColor else White,
        border = BorderStroke(
            1.dp,
            if (isSelected) activeColor else theme.border
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontFamily = HindSiliguriFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isSelected) White else theme.textPrimary
        )
    }
}

/**
 * Super clear, aesthetic Loan Card for individual borrower/lender
 */
@Composable
private fun SimpleLoanCard(
    loan: LoanEntity,
    isBn: Boolean,
    onPaymentClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSettleToggle: () -> Unit,
    onCallClick: (String) -> Unit,
    onSmsClick: (phone: String, name: String, remaining: Long, dueFormatted: String) -> Unit
) {
    val theme = LocalAppTheme.current
    var showMenu by remember { mutableStateOf(false) }

    val isLent = loan.type == LOAN_TYPE_LENT
    val accentColor = if (isLent) Color(0xFF10B981) else Color(0xFFF43F5E)
    val badgeTitle = if (isLent) {
        if (isBn) "🟢 আমি পাবো" else "🟢 To Receive"
    } else {
        if (isBn) "🔴 আমি দেবো" else "🔴 To Pay"
    }

    val dueDateFormatted = if (loan.dueDate > 0L) {
        SimpleDateFormat("dd MMM yyyy", if (isBn) Locale("bn", "BD") else Locale.getDefault())
            .format(Date(loan.dueDate))
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loan_card_${loan.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = BorderStroke(1.dp, if (loan.isSettled) Color(0xFFE2E8F0) else theme.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Avatar, Name & Phone, Type Badge, Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Friendly Big Initial Avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loan.personName.take(1).uppercase(),
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Phone
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.personName,
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (loan.phoneNumber.isNotBlank()) {
                        Text(
                            text = "📞 ${loan.phoneNumber}",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }
                }

                // Type Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeTitle,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

                // More Menu (Settle / Delete)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = theme.textMuted
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (loan.isSettled) "পুনরায় চালু করুন" else "সম্পূর্ণ শোধ চিহ্নিত করুন",
                                    fontFamily = HindSiliguriFontFamily
                                )
                            },
                            onClick = {
                                showMenu = false
                                onSettleToggle()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (loan.isSettled) Icons.Default.Refresh else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = theme.primary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "মুছে ফেলুন",
                                    fontFamily = HindSiliguriFontFamily,
                                    color = theme.expense
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = theme.expense
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CENTER HIGHLIGHT BOX: কত টাকা বাকি আছে (Clean and big)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (loan.isSettled) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                border = BorderStroke(
                    1.dp,
                    if (loan.isSettled) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (loan.isSettled) "🎉 সম্পূর্ণ পরিশোধ হয়েছে" else "এখনও বাকি আছে:",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (loan.isSettled) Color(0xFF15803D) else theme.textMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (loan.isSettled) "৳ ০ (খালাস)" else "৳ ${loan.remainingAmount}",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (loan.isSettled) Color(0xFF15803D) else accentColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট ধার: ৳${loan.amount}",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                        Text(
                            text = "পরিশোধ: ৳${loan.paidAmount}",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            // Visual Progress Bar
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { loan.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (loan.isSettled) Color(0xFF10B981) else accentColor,
                    trackColor = Color(0xFFE2E8F0)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(loan.progress * 100).toInt()}% পরিশোধ হয়েছে",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 11.sp,
                        color = theme.textMuted
                    )
                    if (loan.dueDate > 0L) {
                        Text(
                            text = "📅 $dueDateFormatted",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 11.sp,
                            color = if (loan.isOverdue) Color(0xFFDC2626) else theme.textMuted,
                            fontWeight = if (loan.isOverdue) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Status Badge / Overdue Warning or Notes
            if (loan.isOverdue && !loan.isSettled) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "⚠️", fontSize = 12.sp)
                        Text(
                            text = "ফেরত দেওয়ার সম্ভাব্য সময় পার হয়েছে!",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            if (loan.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📝 ${loan.notes}",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 12.sp,
                    color = theme.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // BIG ACTION BUTTONS: কল, তাগাদা SMS, টাকা পরিশোধ
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Button (If phone number exists)
                if (loan.phoneNumber.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onCallClick(loan.phoneNumber) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_call_${loan.id}"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = White,
                            contentColor = theme.primary
                        ),
                        border = BorderStroke(1.dp, theme.primary.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = theme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "কল",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary
                        )
                    }

                    // SMS Reminder Button (If money is lent and still pending)
                    if (isLent && !loan.isSettled) {
                        OutlinedButton(
                            onClick = {
                                onSmsClick(
                                    loan.phoneNumber,
                                    loan.personName,
                                    loan.remainingAmount,
                                    dueDateFormatted
                                )
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("btn_sms_${loan.id}"),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFFF0FDF4),
                                contentColor = Color(0xFF15803D)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = "SMS",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "তাগাদা SMS",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                // Record Payment Button
                if (!loan.isSettled) {
                    Button(
                        onClick = onPaymentClick,
                        modifier = Modifier
                            .weight(1.5f)
                            .height(44.dp)
                            .testTag("btn_pay_${loan.id}"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Pay",
                            tint = White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "টাকা শোধ লিখুন",
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                } else {
                    // Settled badge
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "সম্পূর্ণ শোধ হয়েছে (হিসাব শেষ)",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty view with friendly graphics and advice
 */
@Composable
private fun EmptyLoanView(isBn: Boolean, onAddClick: () -> Unit) {
    val theme = LocalAppTheme.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, theme.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🤝", fontSize = 42.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isBn) "ধার-দেনার খাতা খালি" else "No Loans Recorded",
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = theme.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isBn) {
                    "কাউকে টাকা ধার দিলে বা কারও কাছ থেকে ধার নিলে উপরের সবুজ ও লাল বাটনে চাপ দিয়ে সহজেই খাতায় লিখে রাখুন।"
                } else {
                    "Tap the green or red button above to easily record money lent or borrowed."
                },
                fontFamily = HindSiliguriFontFamily,
                fontSize = 13.sp,
                color = theme.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "নতুন ধার যোগ করুন" else "Add Loan",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

/**
 * Child-friendly BottomSheet to add new Loan
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KidFriendlyAddLoanSheet(
    initialType: String = LOAN_TYPE_LENT,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, type: String, amount: Long, dueDate: Long, notes: String) -> Unit,
    isBn: Boolean
) {
    val theme = LocalAppTheme.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var loanType by remember { mutableStateOf(initialType) }
    var personName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var selectedDueDate by remember { mutableLongStateOf(0L) }
    var showDatePicker by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val isLent = loanType == LOAN_TYPE_LENT
    val activeColor = if (isLent) Color(0xFF10B981) else Color(0xFFF43F5E)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLent) "কাউকে ধার দিলাম (পাবো) 🟢" else "কারও থেকে ধার নিলাম (দেবো) 🔴",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Big 2-Button Toggle: ধার দিলাম vs ধার নিলাম
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isLentSelected = loanType == LOAN_TYPE_LENT
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { loanType = LOAN_TYPE_LENT },
                    color = if (isLentSelected) Color(0xFF10B981) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🟢 ধার দিলাম (পাবো)",
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isLentSelected) White else theme.textMuted,
                        textAlign = TextAlign.Center
                    )
                }

                val isBorrowedSelected = loanType == LOAN_TYPE_BORROWED
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { loanType = LOAN_TYPE_BORROWED },
                    color = if (isBorrowedSelected) Color(0xFFF43F5E) else Color.Transparent,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🔴 ধার নিলাম (দেবো)",
                        modifier = Modifier.padding(vertical = 10.dp),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isBorrowedSelected) White else theme.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Person Name (কার নাম?)
            OutlinedTextField(
                value = personName,
                onValueChange = {
                    personName = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_loan_person_name"),
                label = { Text("কার নাম? (ব্যক্তি বা প্রতিষ্ঠান)", fontFamily = HindSiliguriFontFamily, fontWeight = FontWeight.Bold) },
                placeholder = { Text("যেমন: রাহিম চাচা, সজীব, মুদির দোকান", fontFamily = HindSiliguriFontFamily) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = activeColor)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = theme.border
                )
            )

            // Amount Input (কত টাকা?)
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        amountText = it
                        errorMessage = null
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_loan_amount"),
                label = { Text("কত টাকা?", fontFamily = HindSiliguriFontFamily, fontWeight = FontWeight.Bold) },
                placeholder = { Text("টাকার পরিমাণ লিখুন", fontFamily = HindSiliguriFontFamily) },
                leadingIcon = {
                    Text(
                        text = "৳",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = theme.border
                )
            )

            // Quick Money Chips: +৫০, +১০০, +৫০০, +১০০০, +২০০০
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(50L, 100L, 500L, 1000L, 2000L).forEach { amt ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                val current = amountText.toLongOrNull() ?: 0L
                                amountText = (current + amt).toString()
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = "+$amt",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontFamily = HindSiliguriFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Phone Number (ঐচ্ছিক)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_loan_phone"),
                label = { Text("মোবাইল নাম্বার (ঐচ্ছিক - কল ও SMS এর জন্য)", fontFamily = HindSiliguriFontFamily) },
                placeholder = { Text("যেমন: 017xxxxxxxx", fontFamily = HindSiliguriFontFamily) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = theme.textMuted)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = theme.border
                )
            )

            // Return Date Chips: ৭ দিন, ১৫ দিন, ১ মাস, ক্যালেন্ডার
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "কবে টাকা ফেরত দেবে/নেবে? (ঐচ্ছিক)",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(7 to "১ সপ্তাহ", 15 to "১৫ দিন", 30 to "১ মাস").forEach { (days, label) ->
                        val targetTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(days.toLong())
                        val isSelected = selectedDueDate > 0L &&
                                Math.abs(selectedDueDate - targetTime) < TimeUnit.DAYS.toMillis(1)

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedDueDate = targetTime },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) activeColor else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) activeColor else Color(0xFFE2E8F0))
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 8.dp),
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) White else theme.textPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Calendar button
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedDueDate > 0L) activeColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = "Date",
                                tint = activeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (selectedDueDate > 0L) {
                                    SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(selectedDueDate))
                                } else {
                                    "ক্যালেন্ডার"
                                },
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeColor
                            )
                        }
                    }
                }
            }

            // Notes / Reason
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_loan_notes"),
                label = { Text("কীসের জন্য? (নোট - ঐচ্ছিক)", fontFamily = HindSiliguriFontFamily) },
                placeholder = { Text("যেমন: টিফিনের টাকা / বই কিনতে / জরুরি", fontFamily = HindSiliguriFontFamily) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeColor,
                    unfocusedBorderColor = theme.border
                )
            )

            // Error notice
            errorMessage?.let { err ->
                Text(
                    text = "⚠️ $err",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }

            // Big Save Button
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (personName.isBlank()) {
                        errorMessage = "দয়া করে কার নাম তা লিখুন"
                        return@Button
                    }
                    val amt = amountText.toLongOrNull() ?: 0L
                    if (amt <= 0L) {
                        errorMessage = "টাকার পরিমাণ লিখুন (০ এর বেশি)"
                        return@Button
                    }
                    onSave(
                        personName.trim(),
                        phoneNumber.trim(),
                        loanType,
                        amt,
                        selectedDueDate,
                        notesText.trim()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_loan"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "খাতায় লিখে রাখুন ✓",
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (selectedDueDate > 0L) selectedDueDate else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDueDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ঠিক আছে", fontFamily = HindSiliguriFontFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল", fontFamily = HindSiliguriFontFamily)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Child-friendly Payment Dialog: Pay full with 1 click or enter partial payment
 */
@Composable
fun KidFriendlyPaymentDialog(
    loan: LoanEntity,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val theme = LocalAppTheme.current
    var paymentAmountText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val isLent = loan.type == LOAN_TYPE_LENT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💵", fontSize = 22.sp)
                Text(
                    text = "টাকা শোধ লিখুন",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info block
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${loan.personName} (${if (isLent) "পাবো" else "দেবো"})",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = theme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বর্তমানে বাকি আছে:",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.sp,
                                color = theme.textMuted
                            )
                            Text(
                                text = "৳ ${loan.remainingAmount}",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = if (isLent) Color(0xFF10B981) else Color(0xFFF43F5E)
                            )
                        }
                    }
                }

                // Super easy 1-click button: "পুরো টাকা শোধ হয়ে গেছে"
                Button(
                    onClick = { paymentAmountText = loan.remainingAmount.toString() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981)
                    )
                ) {
                    Text(
                        text = "✓ পুরো ৳${loan.remainingAmount} শোধ হয়ে গেছে",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = White
                    )
                }

                // Or enter custom amount
                OutlinedTextField(
                    value = paymentAmountText,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) {
                            paymentAmountText = it
                            errorMsg = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_repayment_amount"),
                    label = { Text("অথবা কিছু টাকা শোধ হলে লিখুন", fontFamily = HindSiliguriFontFamily) },
                    placeholder = { Text("যেমন: ৫০, ১০০, ২০০", fontFamily = HindSiliguriFontFamily) },
                    leadingIcon = {
                        Text(
                            text = "৳",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = theme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                errorMsg?.let {
                    Text(
                        text = "⚠️ $it",
                        fontFamily = HindSiliguriFontFamily,
                        fontSize = 12.sp,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = paymentAmountText.toLongOrNull() ?: 0L
                    if (amt <= 0L) {
                        errorMsg = "টাকার পরিমাণ ০ এর বেশি লিখুন"
                        return@Button
                    }
                    onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
            ) {
                Text(
                    text = "পরিশোধ নিশ্চিত করুন ✓",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "বাতিল",
                    fontFamily = HindSiliguriFontFamily
                )
            }
        }
    )
}

// Dial phone intent
private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "কল করতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// Send SMS reminder intent
private fun sendSmsReminder(context: Context, phoneNumber: String, messageText: String) {
    try {
        val cleanNumber = phoneNumber.replace("[^0-9+]".toRegex(), "")
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanNumber")
            putExtra("sms_body", messageText)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "SMS পাঠাতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

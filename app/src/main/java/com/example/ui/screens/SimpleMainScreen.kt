package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.ui.auth.GoogleSignInButton
import com.example.ui.theme.DangerRed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AccountEntity
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TYPE_REFUND
import com.example.data.model.TransactionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.theme.AppThemeColors
import com.example.ui.theme.AppThemes
import com.example.ui.theme.HindSiliguriFontFamily
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.LocalIsBengali
import com.example.ui.theme.White
import com.example.ui.viewmodel.ExpenseViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.AppStrings
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMainScreen(
    viewModel: ExpenseViewModel,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val theme = LocalAppTheme.current
    val isBn = LocalIsBengali.current

    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle()
    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    val transactions by viewModel.currentMonthTransactions.collectAsStateWithLifecycle()
    val summary by viewModel.financialSummary.collectAsStateWithLifecycle()

    var showAddSheetForType by remember { mutableStateOf<String?>(null) } // "EXPENSE" or "REFUND"
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Section switcher: 0 = Daily Ledger, 1 = Loans (ধার-দেনা খাতা)
    var currentSection by remember { mutableIntStateOf(0) }

    // Account Switcher and Creation Modals
    var showAccountSwitcherDialog by remember { mutableStateOf(false) }
    var showCreateAccountSheet by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    // Filter: "ALL", "EXPENSE", "REFUND"
    var filterType by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(transactions, filterType) {
        when (filterType) {
            "EXPENSE" -> transactions.filter { it.type == TYPE_EXPENSE }
            "REFUND" -> transactions.filter { it.type == TYPE_REFUND }
            else -> transactions
        }
    }

    // Listen for toast/error events
    LaunchedEffect(viewModel) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowError -> snackbarHostState.showSnackbar(if (isBn) "ত্রুটি: ${event.message}" else "Error: ${event.message}")
            }
        }
    }

    // Helper to generate & share/open PDF
    fun downloadPdf() {
        coroutineScope.launch {
            try {
                val accName = currentAccount?.name ?: (if (isBn) "আমার হাতখরচ" else "Pocket Money")
                val currentLoans = viewModel.allLoans.value
                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generateReport(
                        context = context,
                        yearMonth = selectedYearMonth,
                        summary = summary,
                        transactions = transactions,
                        accountName = accName,
                        loans = currentLoans
                    )
                }
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "$accName - PDF Report"))
            } catch (e: Exception) {
                val err = if (isBn) "PDF তৈরি করা সম্ভব হয়নি: ${e.message}" else "Failed to generate PDF: ${e.message}"
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareBackup() {
        coroutineScope.launch {
            try {
                val json = viewModel.exportBackupJson()
                val backupDir = File(context.cacheDir, "backups")
                if (!backupDir.exists()) backupDir.mkdirs()
                val timeStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFile = File(backupDir, "Amar_Hisab_$timeStr.json")
                backupFile.writeText(json)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", backupFile)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, if (isBn) "আমার হিসাব ব্যাকআপ" else "Amar Hisab Backup")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, if (isBn) "ব্যাকআপ ফাইল শেয়ার করুন" else "Share Backup File"))
            } catch (e: Exception) {
                val err = if (isBn) "ব্যাকআপ শেয়ারে ব্যর্থ: ${e.message}" else "Failed to share backup: ${e.message}"
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background),
        containerColor = theme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (currentSection == 1) "🤝" else "৳",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = theme.highlight,
                                fontSize = if (currentSection == 1) 18.sp else 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (currentSection == 1) AppStrings.loanSectionTitle(isBn) else AppStrings.appName(isBn),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = White,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    // Cloud Sync Button
                    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
                    IconButton(
                        onClick = { viewModel.syncWithCloud(isManual = true) },
                        modifier = Modifier.testTag("btn_top_cloud_sync")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (syncStatus.isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = "Cloud Sync",
                                    tint = if (syncStatus.isSuccess) theme.highlight else White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("btn_top_settings")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = AppStrings.settings(isBn),
                                tint = White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // PDF Download Button
                    IconButton(
                        onClick = { downloadPdf() },
                        modifier = Modifier.testTag("btn_top_pdf")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "PDF",
                                tint = White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // 3-dots Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(White)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "⚙️ ${AppStrings.settings(isBn)}",
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showSettingsSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "➕ ${AppStrings.newBook(isBn)}",
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showCreateAccountSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.pdfReport(isBn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    downloadPdf()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.backupShare(isBn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    shareBackup()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.clearAll(isBn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.expense
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showClearConfirm = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = theme.topBarBg
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentSection == 0,
                    onClick = { currentSection = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = AppStrings.tabDaily(isBn)
                        )
                    },
                    label = {
                        Text(
                            text = AppStrings.tabDaily(isBn),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = if (currentSection == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primary,
                        selectedTextColor = theme.primary,
                        indicatorColor = theme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = theme.textMuted,
                        unselectedTextColor = theme.textMuted
                    ),
                    modifier = Modifier.testTag("nav_item_daily")
                )

                val overdueCount by viewModel.overdueLoanCount.collectAsStateWithLifecycle()
                NavigationBarItem(
                    selected = currentSection == 1,
                    onClick = { currentSection = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (overdueCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFD32F2F),
                                        contentColor = White
                                    ) {
                                        Text("$overdueCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = AppStrings.tabLoans(isBn)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = AppStrings.tabLoans(isBn),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = if (currentSection == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = theme.primary,
                        selectedTextColor = theme.primary,
                        indicatorColor = theme.primary.copy(alpha = 0.15f),
                        unselectedIconColor = theme.textMuted,
                        unselectedTextColor = theme.textMuted
                    ),
                    modifier = Modifier.testTag("nav_item_loans")
                )
            }
        }
    ) { innerPadding ->
        if (currentSection == 1) {
            LoanTrackerScreen(
                viewModel = viewModel,
                onBackToDaily = { currentSection = 0 },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // 1. Sleek Account / Ledger Switcher Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Current Account Switcher Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showAccountSwitcherDialog = true }
                                .background(theme.container)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("btn_switch_account")
                        ) {
                            Text(
                                text = currentAccount?.iconEmoji ?: "👛",
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = AppStrings.activeBook(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = theme.textMuted
                                )
                                Text(
                                    text = currentAccount?.name ?: (if (isBn) "আমার হাতখরচ" else "Pocket Money"),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = theme.primaryDark
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch",
                                tint = theme.primaryDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // "+ New Book" Quick Action Pill Button
                        Button(
                            onClick = { showCreateAccountSheet = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.primary,
                                contentColor = White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("btn_create_new_account_top")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.newBook(isBn),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = White
                            )
                        }
                    }
                }
            }

            // 2. Month Selector Header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.surface,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 1.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMonth() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.background)
                                .testTag("btn_prev_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Prev",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showMonthPicker = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = theme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DateUtils.formatYearMonth(selectedYearMonth, isBn),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 16.sp
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.background)
                                .testTag("btn_next_month")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Next",
                                tint = theme.textPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // 3. Hero Balance Card (Dynamic Theme Gradient)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_balance_card"),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(colors = theme.heroGradient)
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Ledger Label Badge
                            Surface(
                                color = White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentAccount?.iconEmoji ?: "👛",
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${currentAccount?.name ?: (if (isBn) "প্রধান হিসাব" else "Main")} • ${AppStrings.cashInHand(isBn)}",
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Large Main Balance
                            Text(
                                text = DateUtils.formatTaka(summary.actualRemainingBalance, isBn = isBn),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = White,
                                fontSize = 36.sp,
                                lineHeight = 44.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sub-metric Cards: Received (+) & Spent (-)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Income Pill Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = AppStrings.moneyReceived(isBn),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+ ${DateUtils.formatTaka(summary.totalRefunds, isBn = isBn)}",
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.highlight,
                                            fontSize = 16.sp
                                        )
                                    }
                                }

                                // Expense Pill Card
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = AppStrings.moneySpent(isBn),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = White.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "- ${DateUtils.formatTaka(summary.totalExpenses, isBn = isBn)}",
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = White,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Two Big Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Red Action: খরচ করেছি (-)
                    Button(
                        onClick = { showAddSheetForType = TYPE_EXPENSE },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("btn_add_expense_big"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.expense,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircle,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "খরচ করেছি\n(-)" else "Spent\n(-)",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                color = White
                            )
                        }
                    }

                    // Green Action: টাকা পেয়েছি (+)
                    Button(
                        onClick = { showAddSheetForType = TYPE_REFUND },
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .testTag("btn_add_income_big"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.income,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "টাকা পেয়েছি\n(+)" else "Received\n(+)",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                color = White
                            )
                        }
                    }
                }
            }

            // 5. Transactions Header & Filter Chips
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val accName = currentAccount?.name ?: (if (isBn) "হিসাব" else "Records")
                        val countStr = DateUtils.formatDigits(filteredTransactions.size, isBn)
                        Text(
                            text = AppStrings.listTitle(isBn, accName, countStr),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 17.sp
                        )
                    }

                    // Filter chips: All, Expenses, Income
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == "ALL",
                            onClick = { filterType = "ALL" },
                            label = {
                                Text(
                                    text = "${AppStrings.allFilter(isBn)} (${DateUtils.formatDigits(transactions.size, isBn)})",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.primaryDark,
                                selectedLabelColor = White
                            )
                        )

                        FilterChip(
                            selected = filterType == "EXPENSE",
                            onClick = { filterType = "EXPENSE" },
                            label = {
                                Text(
                                    text = AppStrings.expensesFilter(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.expense,
                                selectedLabelColor = White
                            )
                        )

                        FilterChip(
                            selected = filterType == "REFUND",
                            onClick = { filterType = "REFUND" },
                            label = {
                                Text(
                                    text = AppStrings.incomeFilter(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.income,
                                selectedLabelColor = White
                            )
                        )
                    }
                }
            }

            // 6. Transactions List or Empty State
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(theme.container),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentAccount?.iconEmoji ?: "👛",
                                    fontSize = 32.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            val accName = currentAccount?.name ?: (if (isBn) "এই হিসাব" else "this book")
                            Text(
                                text = AppStrings.emptyTitle(isBn, accName),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = AppStrings.emptySubtitle(isBn),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = theme.textMuted,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { tx ->
                    SimpleTransactionCard(
                        transaction = tx,
                        theme = theme,
                        isBn = isBn,
                        onDelete = { transactionToDelete = tx }
                    )
                }
            }
        }
    }
}

    // Modal: Settings Bottom Sheet (Language & Color Theme)
    if (showSettingsSheet) {
        SettingsSheet(
            theme = theme,
            isBn = isBn,
            viewModel = viewModel,
            onSignOut = onSignOut,
            onDismiss = { showSettingsSheet = false }
        )
    }

    // Modal: Account Switcher Dialog (হিসাবের খাতা বদলান)
    if (showAccountSwitcherDialog) {
        AlertDialog(
            onDismissRequest = { showAccountSwitcherDialog = false },
            containerColor = White,
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.container),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = AppStrings.accountsModalTitle(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = AppStrings.chooseBookToRecord(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted,
                        fontSize = 13.sp
                    )

                    allAccounts.forEach { acc ->
                        val isSelected = acc.id == currentAccount?.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    viewModel.selectAccount(acc.id)
                                    showAccountSwitcherDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) theme.container else theme.background
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, theme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = acc.iconEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = acc.name,
                                            fontFamily = HindSiliguriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary,
                                            fontSize = 15.sp
                                        )
                                        if (isSelected) {
                                            Text(
                                                text = AppStrings.activePill(isBn),
                                                fontFamily = HindSiliguriFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.income,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                if (allAccounts.size > 1 && !isSelected) {
                                    IconButton(
                                        onClick = { accountToDelete = acc },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = theme.expense,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Button inside modal to create another account
                    Button(
                        onClick = {
                            showAccountSwitcherDialog = false
                            showCreateAccountSheet = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "➕ ${AppStrings.newBook(isBn)}",
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountSwitcherDialog = false }) {
                    Text(
                        text = AppStrings.close(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }
        )
    }

    // Modal: Create New Account Sheet
    if (showCreateAccountSheet) {
        CreateNewAccountSheet(
            theme = theme,
            isBn = isBn,
            onDismiss = { showCreateAccountSheet = false },
            onConfirm = { name, emoji ->
                viewModel.createAccount(name = name, iconEmoji = emoji) {
                    showCreateAccountSheet = false
                }
            }
        )
    }

    // Modal: Add Expense / Income Sheet
    if (showAddSheetForType != null) {
        val type = showAddSheetForType!!
        KidFriendlyAddSheet(
            isExpense = type == TYPE_EXPENSE,
            accountName = currentAccount?.name ?: (if (isBn) "হিসাব" else "Account"),
            theme = theme,
            isBn = isBn,
            onDismiss = { showAddSheetForType = null },
            onSave = { amount, desc, category ->
                viewModel.addTransaction(
                    amount = amount,
                    description = desc,
                    category = category,
                    type = type,
                    notes = "",
                    onSuccess = {
                        showAddSheetForType = null
                    }
                )
            }
        )
    }

    // Delete Account Confirmation
    if (accountToDelete != null) {
        val target = accountToDelete!!
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            containerColor = White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = AppStrings.deleteConfirmTitle(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.expense
                )
            },
            text = {
                Text(
                    text = AppStrings.deleteBookConfirm(isBn, target.name),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(target) {
                            accountToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.expense),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = AppStrings.deleteBook(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text(
                        text = AppStrings.cancel(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }
        )
    }

    // Delete Confirmation for a transaction
    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        val amtStr = DateUtils.formatTaka(tx.amount, isBn = isBn)
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            containerColor = White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = AppStrings.deleteConfirmTitle(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.expense
                )
            },
            text = {
                Text(
                    text = AppStrings.deleteTxConfirm(isBn, tx.description, amtStr),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.expense),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = AppStrings.yesDelete(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(
                        text = AppStrings.keepIt(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }
        )
    }

    // Clear All Confirmation
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = AppStrings.clearAll(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.expense
                )
            },
            text = {
                Text(
                    text = AppStrings.clearAllConfirm(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData {
                            showClearConfirm = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.expense),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = AppStrings.yesDelete(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(
                        text = AppStrings.cancel(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }
        )
    }

    // Month Selector Dialog
    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYearMonth = selectedYearMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                viewModel.setSelectedYearMonth(it)
                showMonthPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    theme: AppThemeColors,
    isBn: Boolean,
    viewModel: ExpenseViewModel,
    onSignOut: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val currentThemeKey by viewModel.selectedThemeKey.collectAsStateWithLifecycle()
    val currentLangKey by viewModel.selectedLanguageKey.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 5.dp),
                shape = CircleShape,
                color = theme.border
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(theme.container),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = AppStrings.settings(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        fontSize = 20.sp
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = AppStrings.close(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }

            // SECTION 1: LANGUAGE SELECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🌐 ${AppStrings.selectLanguage(isBn)}",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = AppStrings.languageDesc(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted,
                    fontSize = 12.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Bengali Card
                    val isBnSelected = currentLangKey == UserPreferencesRepository.LANG_BN
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setLanguage(UserPreferencesRepository.LANG_BN) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBnSelected) theme.container else theme.background
                        ),
                        border = if (isBnSelected) androidx.compose.foundation.BorderStroke(1.8.dp, theme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🇧🇩", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "বাংলা",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            if (isBnSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // English Card
                    val isEnSelected = currentLangKey == UserPreferencesRepository.LANG_EN
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setLanguage(UserPreferencesRepository.LANG_EN) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEnSelected) theme.container else theme.background
                        ),
                        border = if (isEnSelected) androidx.compose.foundation.BorderStroke(1.8.dp, theme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🇬🇧", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "English",
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary,
                                    fontSize = 15.sp
                                )
                            }
                            if (isEnSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SECTION 2: COLOR THEME SELECTION
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🎨 ${AppStrings.selectTheme(isBn)}",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = AppStrings.themeDesc(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted,
                    fontSize = 12.sp
                )

                // Render 4 Themes
                AppThemes.allThemes.forEach { th ->
                    val isSelected = currentThemeKey.equals(th.key, ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setTheme(th.key) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) th.container else theme.background
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, th.primary) else androidx.compose.foundation.BorderStroke(1.dp, theme.border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = th.emoji, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isBn) th.nameBn else th.nameEn,
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = th.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Palette Swatches
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        th.previewSwatches.forEach { color ->
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    color = th.primary,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = AppStrings.activePill(isBn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SECTION 3: GOOGLE ACCOUNT & CLOUD SYNC
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "☁️ ${AppStrings.googleAccountSection(isBn)}",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 16.sp
                )
                Text(
                    text = AppStrings.cloudSyncDesc(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted,
                    fontSize = 12.sp
                )

                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
                var showSignOutConfirm by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.background),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (currentUser != null) {
                            // User Info Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = CircleShape,
                                    color = theme.primary.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = theme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: (if (isBn) "গুগল ব্যবহারকারী" else "Google User"),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        fontSize = 12.sp,
                                        color = theme.textMuted
                                    )
                                }
                            }

                            // Sync Status Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(theme.container, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (syncStatus.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = theme.primary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = AppStrings.syncing(isBn),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontSize = 13.sp,
                                            color = theme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = theme.income,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = syncStatus.message ?: AppStrings.syncedSuccess(isBn),
                                            fontFamily = HindSiliguriFontFamily,
                                            fontSize = 12.sp,
                                            color = theme.income,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = { viewModel.syncWithCloud(isManual = true) },
                                    enabled = !syncStatus.isSyncing
                                ) {
                                    Text(
                                        text = AppStrings.syncNow(isBn),
                                        fontFamily = HindSiliguriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.primary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Sign Out Button
                            OutlinedButton(
                                onClick = { showSignOutConfirm = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_sign_out"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.signOut(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = DangerRed,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Text(
                                text = if (isBn) "গুগল অ্যাকাউন্ট দিয়ে লগইন করে স্বয়ংক্রিয় ব্যাকআপ উপভোগ করুন।" else "Sign in with Google to enable automatic cloud backup.",
                                fontFamily = HindSiliguriFontFamily,
                                fontSize = 13.sp,
                                color = theme.textMuted
                            )
                            GoogleSignInButton(
                                onAuthSuccess = {
                                    viewModel.syncWithCloud(isManual = true)
                                },
                                onAuthError = { /* handled in button */ }
                            )
                        }
                    }
                }

                if (showSignOutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showSignOutConfirm = false },
                        containerColor = White,
                        shape = RoundedCornerShape(20.dp),
                        title = {
                            Text(
                                text = AppStrings.signOut(isBn),
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = AppStrings.signOutConfirm(isBn),
                                fontFamily = HindSiliguriFontFamily,
                                color = theme.textMuted,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSignOutConfirm = false
                                    onDismiss()
                                    onSignOut()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                            ) {
                                Text(
                                    text = AppStrings.signOut(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSignOutConfirm = false }) {
                                Text(
                                    text = AppStrings.cancel(isBn),
                                    fontFamily = HindSiliguriFontFamily,
                                    color = theme.textMuted
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewAccountSheet(
    theme: AppThemeColors,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String) -> Unit
) {
    var accountNameText by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🎒") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val emojis = listOf("🎒", "👛", "🏪", "🏠", "🚲", "💰", "📚", "⚽", "🎁", "📱", "💼", "🍕")
    val nameSuggestions = if (isBn) {
        listOf("স্কুলের খরচ", "দোকানের হিসাব", "টিউশনি", "জমানো টাকা", "বাসার বাজার", "সাইকেলের টাকা")
    } else {
        listOf("School", "Shop", "Tuition", "Savings", "Groceries", "Personal")
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 5.dp),
                shape = CircleShape,
                color = theme.border
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = AppStrings.newBook(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 20.sp
                )
                TextButton(onClick = onDismiss) {
                    Text(
                        text = AppStrings.cancel(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }

            Text(
                text = AppStrings.bookNamePrompt(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = accountNameText,
                onValueChange = {
                    accountNameText = it
                    errorMessage = null
                },
                placeholder = {
                    Text(
                        text = AppStrings.bookNamePlaceholder(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                },
                leadingIcon = {
                    Text(text = selectedEmoji, fontSize = 22.sp, modifier = Modifier.padding(start = 8.dp))
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.border
                )
            )

            // Quick Name Suggestions
            Text(
                text = AppStrings.suggestionsPrompt(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textMuted,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                nameSuggestions.forEach { suggestion ->
                    FilterChip(
                        selected = accountNameText == suggestion,
                        onClick = { accountNameText = suggestion },
                        label = {
                            Text(
                                text = suggestion,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.container,
                            selectedLabelColor = theme.primaryDark
                        )
                    )
                }
            }

            // Emoji Icon Picker
            Text(
                text = AppStrings.bookEmojiPrompt(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                fontSize = 14.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                emojis.forEach { emoji ->
                    val isChosen = selectedEmoji == emoji
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isChosen) theme.container else theme.background)
                            .border(
                                width = if (isChosen) 2.dp else 0.dp,
                                color = if (isChosen) theme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 24.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.expense,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (accountNameText.isBlank()) {
                        errorMessage = AppStrings.enterBookNameError(isBn)
                        return@Button
                    }
                    keyboardController?.hide()
                    onConfirm(accountNameText.trim(), selectedEmoji)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.createBook(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = White
                )
            }
        }
    }
}

@Composable
private fun SimpleTransactionCard(
    transaction: TransactionEntity,
    theme: AppThemeColors,
    isBn: Boolean,
    onDelete: () -> Unit
) {
    val isRefund = transaction.type == TYPE_REFUND

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("simple_tx_${transaction.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isRefund) theme.incomeBg else theme.expenseBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isRefund) "➕" else "➖",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    fontSize = 16.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${DateUtils.formatDate(transaction.timestamp, isBn)} • ${DateUtils.formatTime(transaction.timestamp, isBn)}",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.textMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                val color = if (isRefund) theme.income else theme.expense
                val sign = if (isRefund) "+" else "-"
                Text(
                    text = "$sign ${DateUtils.formatTaka(transaction.amount, isBn = isBn)}",
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Delete Icon
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = theme.textMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KidFriendlyAddSheet(
    isExpense: Boolean,
    accountName: String,
    theme: AppThemeColors,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSave: (amount: Long, desc: String, category: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(
            if (isExpense) (if (isBn) "খাবার ও নাস্তা" else "Food & Snacks")
            else (if (isBn) "হাতখরচ" else "Pocket Money")
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val expensePresets = if (isBn) {
        listOf("টিফিন", "খাতা-কলম", "আইসক্রিম", "চকলেট", "নাস্তা", "গাড়ি ভাড়া", "খেলনা")
    } else {
        listOf("Snacks", "Stationery", "Ice Cream", "Chocolate", "Breakfast", "Transport", "Toys")
    }

    val incomePresets = if (isBn) {
        listOf("হাতখরচ", "আব্বুর উপহার", "আম্মুর উপহার", "পুরস্কার", "সালামি")
    } else {
        listOf("Pocket Money", "Gift from Dad", "Gift from Mom", "Prize", "Salary")
    }

    val quickAmounts = if (isBn) listOf("১০", "২০", "৫০", "১০০", "৫০০") else listOf("10", "20", "50", "100", "500")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 44.dp, height = 5.dp),
                shape = CircleShape,
                color = theme.border
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isExpense) theme.expenseBg else theme.incomeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isExpense) "🔴" else "🟢", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isExpense) AppStrings.expenseRecordTitle(isBn) else AppStrings.incomeRecordTitle(isBn),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            text = AppStrings.bookLabel(isBn, accountName),
                            fontFamily = HindSiliguriFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = theme.textMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text(
                        text = AppStrings.cancel(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                }
            }

            // Amount Field
            Text(
                text = AppStrings.howMuch(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorText = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = AppStrings.amountPlaceholder(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                },
                leadingIcon = {
                    Text(
                        text = "৳",
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpense) theme.expense else theme.income,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isExpense) theme.expense else theme.income,
                    unfocusedBorderColor = theme.border
                )
            )

            // Quick Amount Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickAmounts.forEach { amt ->
                    FilterChip(
                        selected = amountText == amt,
                        onClick = { amountText = amt },
                        label = {
                            Text(
                                text = "৳ $amt",
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isExpense) theme.expenseBg else theme.incomeBg,
                            selectedLabelColor = if (isExpense) theme.expense else theme.income
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Description Field
            Text(
                text = AppStrings.forWhat(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = descText,
                onValueChange = {
                    descText = it
                    errorText = null
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isExpense) AppStrings.expenseDescPlaceholder(isBn) else AppStrings.incomeDescPlaceholder(isBn),
                        fontFamily = HindSiliguriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = theme.textMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.border
                )
            )

            // Quick presets buttons
            Text(
                text = AppStrings.quickPresetsPrompt(isBn),
                fontFamily = HindSiliguriFontFamily,
                fontWeight = FontWeight.Bold,
                color = theme.textMuted,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = if (isExpense) expensePresets else incomePresets
                presets.forEach { preset ->
                    FilterChip(
                        selected = descText == preset,
                        onClick = {
                            descText = preset
                            selectedCategory = preset
                        },
                        label = {
                            Text(
                                text = preset,
                                fontFamily = HindSiliguriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = theme.container,
                            selectedLabelColor = theme.primaryDark
                        )
                    )
                }
            }

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = theme.expense,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val parsed = DateUtils.parseAmount(amountText)
                    if (parsed == null || parsed <= 0L) {
                        errorText = AppStrings.enterAmountError(isBn)
                        return@Button
                    }
                    if (descText.isBlank()) {
                        errorText = AppStrings.enterDescError(isBn)
                        return@Button
                    }
                    keyboardController?.hide()
                    onSave(parsed, descText.trim(), selectedCategory)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) theme.expense else theme.income,
                    contentColor = White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.save(isBn),
                    fontFamily = HindSiliguriFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = White
                )
            }
        }
    }
}

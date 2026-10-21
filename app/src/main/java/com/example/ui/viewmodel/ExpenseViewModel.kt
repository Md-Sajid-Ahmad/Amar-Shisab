package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AccountEntity
import com.example.data.model.FinancialSummary
import com.example.data.model.LoanEntity
import com.example.data.model.LOAN_TYPE_BORROWED
import com.example.data.model.LOAN_TYPE_LENT
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.TYPE_EXPENSE
import com.example.data.model.TransactionEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.FirestoreSyncRepository
import com.example.util.DateUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowError(val message: String) : UiEvent()
}

data class SyncStatus(
    val isSyncing: Boolean = false,
    val isSuccess: Boolean = false,
    val lastSyncTime: Long? = null,
    val message: String? = null
)

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val preferencesRepository: UserPreferencesRepository? = null,
    private val syncRepository: FirestoreSyncRepository? = null
) : ViewModel() {

    private val _syncStatus = MutableStateFlow(SyncStatus())
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(Firebase.auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val selectedThemeKey: StateFlow<String> = preferencesRepository?.themeKey
        ?.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesRepository.THEME_BLUE)
        ?: MutableStateFlow(UserPreferencesRepository.THEME_BLUE).asStateFlow()

    val selectedLanguageKey: StateFlow<String> = preferencesRepository?.languageKey
        ?.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferencesRepository.LANG_BN)
        ?: MutableStateFlow(UserPreferencesRepository.LANG_BN).asStateFlow()

    fun setTheme(themeKey: String) {
        viewModelScope.launch {
            preferencesRepository?.setTheme(themeKey)
        }
    }

    fun setLanguage(langKey: String) {
        viewModelScope.launch {
            preferencesRepository?.setLanguage(langKey)
        }
    }

    private val _selectedAccountId = MutableStateFlow(1L)
    val selectedAccountId: StateFlow<Long> = _selectedAccountId.asStateFlow()

    private val _selectedYearMonth = MutableStateFlow(DateUtils.currentYearMonth())
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.NEWEST_FIRST)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    val allAccounts: StateFlow<List<AccountEntity>> = repository.getAllAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentAccount: StateFlow<AccountEntity?> = combine(allAccounts, _selectedAccountId) { accounts, accId ->
        accounts.find { it.id == accId } ?: accounts.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        viewModelScope.launch {
            val def = repository.ensureDefaultAccount()
            if (_selectedAccountId.value == 1L) {
                _selectedAccountId.value = def.id
            }
        }
        Firebase.auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _currentUser.value = user
            if (user != null) {
                syncWithCloud(isManual = false)
            }
        }
    }

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.getRecentTransactions(5)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<MonthlyBudgetEntity>> = repository.getAllBudgets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentMonthBudget: StateFlow<MonthlyBudgetEntity?> = _selectedYearMonth.flatMapLatest { ym ->
        repository.getBudgetForMonth(ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val currentMonthTransactions: StateFlow<List<TransactionEntity>> = combine(
        _selectedAccountId,
        _selectedYearMonth
    ) { accId, ym ->
        Pair(accId, ym)
    }.flatMapLatest { (accId, ym) ->
        repository.getTransactionsForAccountAndMonth(accId, ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val financialSummary: StateFlow<FinancialSummary> = combine(
        _selectedAccountId,
        _selectedYearMonth
    ) { accId, ym ->
        Pair(accId, ym)
    }.flatMapLatest { (accId, ym) ->
        repository.getFinancialSummaryForAccount(accId, ym)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary(
            yearMonth = DateUtils.currentYearMonth(),
            startingBalance = 0L,
            targetBudget = 0L,
            totalExpenses = 0L,
            totalRefunds = 0L,
            actualRemainingBalance = 0L,
            budgetRemaining = 0L,
            budgetUsagePercent = 0,
            transactionCount = 0,
            isOverBudget = false,
            isNegativeBalance = false
        )
    )

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        currentMonthTransactions,
        _searchQuery,
        _selectedCategoryFilter,
        _sortOrder
    ) { txList, query, catFilter, sort ->
        var list = txList

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.notes.lowercase().contains(q) ||
                        it.amount.toString().contains(q)
            }
        }

        if (catFilter != null) {
            list = list.filter { it.category == catFilter }
        }

        when (sort) {
            SortOrder.NEWEST_FIRST -> list.sortedByDescending { it.timestamp }
            SortOrder.OLDEST_FIRST -> list.sortedBy { it.timestamp }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectAccount(accountId: Long) {
        _selectedAccountId.value = accountId
    }

    fun createAccount(
        name: String,
        iconEmoji: String,
        colorHex: String = "#0F2537",
        onSuccess: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val newId = repository.createAccount(name, iconEmoji, colorHex)
                _selectedAccountId.value = newId
                _uiEvents.emit(UiEvent.ShowToast("নতুন হিসাবের খাতা '$name' খোলা হয়েছে!"))
                if (syncRepository?.isUserSignedIn() == true) {
                    val created = repository.accountDao.getAccountById(newId)
                    if (created != null) {
                        syncRepository.uploadAccount(created)
                    }
                }
                onSuccess(newId)
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "হিসাব খোলা সম্ভব হয়নি"))
            }
        }
    }

    fun updateAccount(account: AccountEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateAccount(account)
                _uiEvents.emit(UiEvent.ShowToast("হিসাবের নাম আপডেট হয়েছে!"))
                if (syncRepository?.isUserSignedIn() == true) {
                    syncRepository.uploadAccount(account)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "আপডেট করা সম্ভব হয়নি"))
            }
        }
    }

    fun deleteAccount(account: AccountEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(account)
                val remaining = repository.ensureDefaultAccount()
                _selectedAccountId.value = remaining.id
                _uiEvents.emit(UiEvent.ShowToast("'${account.name}' হিসাব মুছে ফেলা হয়েছে"))
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "হিসাব মুছে ফেলা সম্ভব হয়নি"))
            }
        }
    }

    fun setSelectedYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun previousMonth() {
        _selectedYearMonth.value = DateUtils.previousMonth(_selectedYearMonth.value)
    }

    fun nextMonth() {
        _selectedYearMonth.value = DateUtils.nextMonth(_selectedYearMonth.value)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = category
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun addTransaction(
        amount: Long,
        description: String,
        category: String,
        type: String = TYPE_EXPENSE,
        notes: String = "",
        onSuccess: () -> Unit
    ) {
        if (_isSaving.value) return
        _isSaving.value = true

        viewModelScope.launch {
            try {
                val newTxId = repository.addTransaction(
                    amount = amount,
                    description = description,
                    category = category,
                    type = type,
                    notes = notes,
                    accountId = _selectedAccountId.value
                )
                _isSaving.value = false
                val msg = if (type == TYPE_EXPENSE) "খরচ সফলভাবে সংরক্ষিত হয়েছে!" else "টাকা জমা সফলভাবে সংরক্ষিত হয়েছে!"
                _uiEvents.emit(UiEvent.ShowToast(msg))

                if (syncRepository?.isUserSignedIn() == true) {
                    val createdTx = repository.transactionDao.getAllTransactionsSync().find { it.id == newTxId }
                    if (createdTx != null) {
                        syncRepository.uploadTransaction(createdTx)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                _isSaving.value = false
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "সংরক্ষণ করা সম্ভব হয়নি"))
            }
        }
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateTransaction(transaction)
                _uiEvents.emit(UiEvent.ShowToast("লেনদেন আপডেট করা হয়েছে!"))
                if (syncRepository?.isUserSignedIn() == true) {
                    syncRepository.uploadTransaction(transaction)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "আপডেট করা সম্ভব হয়নি"))
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                _uiEvents.emit(UiEvent.ShowToast("লেনদেন মুছে ফেলা হয়েছে!"))
                if (syncRepository?.isUserSignedIn() == true) {
                    syncRepository.deleteRemoteTransaction(transaction.id)
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "মুছে ফেলা সম্ভব হয়নি"))
            }
        }
    }

    fun saveMonthlyBudget(
        yearMonth: String,
        targetBudget: Long,
        startingBalance: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.saveMonthlyBudget(yearMonth, targetBudget, startingBalance)
                _uiEvents.emit(UiEvent.ShowToast("মাসিক বাজেট সফলভাবে সেভ হয়েছে!"))
                if (syncRepository?.isUserSignedIn() == true) {
                    val budget = MonthlyBudgetEntity(
                        yearMonth = yearMonth,
                        targetBudget = targetBudget,
                        startingBalance = startingBalance
                    )
                    syncRepository.uploadBudget(budget)
                }
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "বাজেট সেভ করতে ত্রুটি হয়েছে"))
            }
        }
    }

    fun syncWithCloud(isManual: Boolean = false) {
        if (syncRepository == null || !syncRepository.isUserSignedIn()) return
        viewModelScope.launch {
            _syncStatus.value = _syncStatus.value.copy(isSyncing = true)
            val result = syncRepository.syncBidirectional(repository)
            result.onSuccess { summary ->
                _syncStatus.value = SyncStatus(
                    isSyncing = false,
                    isSuccess = true,
                    lastSyncTime = System.currentTimeMillis(),
                    message = "সিঙ্ক সম্পন্ন (${summary.transactionsSynced} লেনদেন, ${summary.accountsSynced} খাতা)"
                )
                if (isManual) {
                    _uiEvents.emit(UiEvent.ShowToast("ক্লাউডে সফলভাবে সিঙ্ক হয়েছে! ☁️"))
                }
            }.onFailure { err ->
                _syncStatus.value = SyncStatus(
                    isSyncing = false,
                    isSuccess = false,
                    message = err.message
                )
                if (isManual) {
                    _uiEvents.emit(UiEvent.ShowError("সিঙ্ক ব্যর্থ: ${err.message}"))
                }
            }
        }
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun restoreBackup(
        jsonString: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(jsonString)
            result.onSuccess { count ->
                val msg = "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে! ($count টি নতুন লেনদেন যুক্ত হয়েছে)"
                _uiEvents.emit(UiEvent.ShowToast(msg))
                onResult(true, msg)
            }.onFailure { err ->
                val msg = "রিস্টোর ব্যর্থ: ${err.message}"
                _uiEvents.emit(UiEvent.ShowError(msg))
                onResult(false, msg)
            }
        }
    }

    // --- Loan (ধার-দেনা খাতা) Logic ---
    val allLoans: StateFlow<List<LoanEntity>> = repository.getAllLoans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _loanFilterType = MutableStateFlow("ALL") // "ALL", "LENT", "BORROWED", "OVERDUE", "SETTLED"
    val loanFilterType: StateFlow<String> = _loanFilterType.asStateFlow()

    fun setLoanFilter(type: String) {
        _loanFilterType.value = type
    }

    private val _loanSearchQuery = MutableStateFlow("")
    val loanSearchQuery: StateFlow<String> = _loanSearchQuery.asStateFlow()

    fun setLoanSearchQuery(query: String) {
        _loanSearchQuery.value = query
    }

    val filteredLoans: StateFlow<List<LoanEntity>> = combine(
        allLoans,
        _loanFilterType,
        _loanSearchQuery
    ) { loans, filter, query ->
        loans.filter { loan ->
            val matchesFilter = when (filter) {
                "LENT" -> loan.type == LOAN_TYPE_LENT && !loan.isSettled
                "BORROWED" -> loan.type == LOAN_TYPE_BORROWED && !loan.isSettled
                "OVERDUE" -> loan.isOverdue
                "SETTLED" -> loan.isSettled
                else -> true // "ALL"
            }
            val matchesQuery = if (query.isBlank()) true else {
                loan.personName.contains(query, ignoreCase = true) ||
                loan.phoneNumber.contains(query, ignoreCase = true) ||
                loan.notes.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalLentRemaining: StateFlow<Long> = allLoans.map { list ->
        list.filter { it.type == LOAN_TYPE_LENT && !it.isSettled }.sumOf { it.remainingAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val totalBorrowedRemaining: StateFlow<Long> = allLoans.map { list ->
        list.filter { it.type == LOAN_TYPE_BORROWED && !it.isSettled }.sumOf { it.remainingAmount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val overdueLoanCount: StateFlow<Int> = allLoans.map { list ->
        list.count { it.isOverdue }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun addLoan(
        personName: String,
        phoneNumber: String = "",
        type: String = LOAN_TYPE_LENT,
        amount: Long,
        dueDate: Long = 0L,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (personName.isBlank()) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError("ব্যক্তির নাম অবশ্যই দিতে হবে")) }
            return
        }
        if (amount <= 0L) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError("টাকার পরিমাণ ০ এর চেয়ে বেশি হতে হবে")) }
            return
        }

        viewModelScope.launch {
            try {
                repository.createLoan(
                    personName = personName,
                    phoneNumber = phoneNumber,
                    type = type,
                    amount = amount,
                    dueDate = dueDate,
                    notes = notes
                )
                val typeMsg = if (type == LOAN_TYPE_LENT) "ধার দেওয়ার হিসাব যুক্ত হয়েছে" else "ধার নেওয়ার হিসাব যুক্ত হয়েছে"
                _uiEvents.emit(UiEvent.ShowToast(typeMsg))
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "ধার যুক্ত করতে সমস্যা হয়েছে"))
            }
        }
    }

    fun recordLoanPayment(
        loanId: Long,
        paymentAmount: Long,
        onSuccess: () -> Unit = {}
    ) {
        if (paymentAmount <= 0L) {
            viewModelScope.launch { _uiEvents.emit(UiEvent.ShowError("পরিশোধের পরিমাণ ০ এর চেয়ে বেশি হতে হবে")) }
            return
        }

        viewModelScope.launch {
            try {
                repository.recordLoanPayment(loanId, paymentAmount)
                _uiEvents.emit(UiEvent.ShowToast("পরিশোধের হিসাব সফলভাবে সংরক্ষিত হয়েছে"))
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "পরিশোধ সংরক্ষণ করতে সমস্যা হয়েছে"))
            }
        }
    }

    fun settleLoan(loanId: Long, isSettled: Boolean) {
        viewModelScope.launch {
            try {
                repository.settleLoan(loanId, isSettled)
                val msg = if (isSettled) "ধার সম্পন্ন হিসেবে চিহ্নিত করা হয়েছে" else "ধারটি পুনরায় উন্মুক্ত করা হয়েছে"
                _uiEvents.emit(UiEvent.ShowToast(msg))
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "পরিবর্তন করতে সমস্যা হয়েছে"))
            }
        }
    }

    fun deleteLoan(loan: LoanEntity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteLoan(loan)
                _uiEvents.emit(UiEvent.ShowToast("${loan.personName}-এর ধারের হিসাব মুছে ফেলা হয়েছে"))
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "ধার মুছতে সমস্যা হয়েছে"))
            }
        }
    }

    fun clearAllData(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _uiEvents.emit(UiEvent.ShowToast("সকল তথ্য মুছে ফেলা হয়েছে!"))
                onSuccess()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowError(e.message ?: "তথ্য মুছতে সমস্যা হয়েছে"))
            }
        }
    }
}

class ExpenseViewModelFactory(
    private val repository: ExpenseRepository,
    private val preferencesRepository: UserPreferencesRepository? = null,
    private val syncRepository: FirestoreSyncRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(repository, preferencesRepository, syncRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


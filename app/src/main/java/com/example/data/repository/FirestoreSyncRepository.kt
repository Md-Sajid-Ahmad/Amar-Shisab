package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AccountEntity
import com.example.data.model.MonthlyBudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.firestore.FirestoreAccount
import com.example.data.model.firestore.FirestoreMonthlyBudget
import com.example.data.model.firestore.FirestoreTransaction
import com.example.data.model.firestore.FirestoreUser
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.example.util.DateUtils

data class SyncSummary(
    val accountsSynced: Int = 0,
    val transactionsSynced: Int = 0,
    val budgetsSynced: Int = 0
)

class FirestoreSyncRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing cloud sync.")
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun getCurrentUserDisplayName(): String? = auth.currentUser?.displayName

    fun getCurrentUserPhotoUrl(): String? = auth.currentUser?.photoUrl?.toString()

    suspend fun saveUserProfile(displayName: String, email: String, photoUrl: String = ""): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val userDoc = firestore.collection("users").document(uid)
        return try {
            val data = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to displayName.ifBlank { "User" },
                "email" to email,
                "photoUrl" to photoUrl,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(data).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDoc.path)
            Result.failure(e)
        }
    }

    suspend fun uploadAccount(account: AccountEntity): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
            .collection("accounts").document("acc_${account.id}")
        return try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "name" to account.name,
                "iconEmoji" to account.iconEmoji,
                "colorHex" to account.colorHex,
                "localId" to account.id,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun uploadTransaction(transaction: TransactionEntity): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docId = if (transaction.id > 0L) "tx_${transaction.id}" else "tx_${transaction.timestamp}"
        val docRef = firestore.collection("users").document(uid)
            .collection("transactions").document(docId)
        return try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "amount" to transaction.amount,
                "description" to transaction.description,
                "category" to transaction.category,
                "type" to transaction.type,
                "notes" to transaction.notes,
                "timestamp" to transaction.timestamp,
                "yearMonth" to transaction.yearMonth,
                "localAccountId" to transaction.accountId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun uploadBudget(budget: MonthlyBudgetEntity): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
            .collection("budgets").document(budget.yearMonth)
        return try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "yearMonth" to budget.yearMonth,
                "targetBudget" to budget.targetBudget,
                "startingBalance" to budget.startingBalance,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteTransaction(transactionId: Long): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = firestore.collection("users").document(uid)
            .collection("transactions").document("tx_$transactionId")
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeRemoteTransactions(): Flow<List<FirestoreTransaction>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/transactions"
        val query = firestore.collection("users").document(uid).collection("transactions")
        query.snapshots()
            .map { snapshot -> snapshot.toObjects(FirestoreTransaction::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
            .collect { emit(it) }
    }

    suspend fun fetchAllRemoteTransactions(): List<FirestoreTransaction> {
        val uid = requireUserId()
        val path = "users/$uid/transactions"
        val ref = firestore.collection("users").document(uid).collection("transactions")
        return try {
            val snapshot = ref.get().await()
            snapshot.toObjects(FirestoreTransaction::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun fetchAllRemoteAccounts(): List<FirestoreAccount> {
        val uid = requireUserId()
        val path = "users/$uid/accounts"
        val ref = firestore.collection("users").document(uid).collection("accounts")
        return try {
            val snapshot = ref.get().await()
            snapshot.toObjects(FirestoreAccount::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun fetchAllRemoteBudgets(): List<FirestoreMonthlyBudget> {
        val uid = requireUserId()
        val path = "users/$uid/budgets"
        val ref = firestore.collection("users").document(uid).collection("budgets")
        return try {
            val snapshot = ref.get().await()
            snapshot.toObjects(FirestoreMonthlyBudget::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            emptyList()
        }
    }

    suspend fun syncAllLocalToRemote(
        accounts: List<AccountEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<MonthlyBudgetEntity>
    ): Result<SyncSummary> {
        return try {
            var accCount = 0
            for (acc in accounts) {
                if (uploadAccount(acc).isSuccess) accCount++
            }
            var txCount = 0
            for (tx in transactions) {
                if (uploadTransaction(tx).isSuccess) txCount++
            }
            var bgCount = 0
            for (bg in budgets) {
                if (uploadBudget(bg).isSuccess) bgCount++
            }
            Result.success(SyncSummary(accCount, txCount, bgCount))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncBidirectional(repository: ExpenseRepository): Result<SyncSummary> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        return try {
            val user = auth.currentUser
            if (user != null) {
                saveUserProfile(
                    displayName = user.displayName ?: "User",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: ""
                )
            }

            // 1. Fetch remote data
            val remoteAccounts = fetchAllRemoteAccounts()
            val remoteTransactions = fetchAllRemoteTransactions()
            val remoteBudgets = fetchAllRemoteBudgets()

            // 2. Merge remote accounts locally
            for (rAcc in remoteAccounts) {
                val localId = if (rAcc.localId > 0L) rAcc.localId else rAcc.id.removePrefix("acc_").toLongOrNull() ?: 1L
                val existing = repository.accountDao.getAccountById(localId)
                if (existing == null) {
                    repository.accountDao.insert(
                        AccountEntity(
                            id = localId,
                            name = rAcc.name.ifBlank { "প্রধান হিসাব" },
                            iconEmoji = rAcc.iconEmoji.ifBlank { "👛" },
                            colorHex = rAcc.colorHex.ifBlank { "#0F2537" }
                        )
                    )
                }
            }

            // 3. Merge remote budgets locally
            for (rB in remoteBudgets) {
                if (rB.yearMonth.isNotBlank()) {
                    val existing = repository.monthlyBudgetDao.getBudgetSync(rB.yearMonth)
                    if (existing == null) {
                        repository.monthlyBudgetDao.insertOrUpdate(
                            MonthlyBudgetEntity(
                                yearMonth = rB.yearMonth,
                                targetBudget = rB.targetBudget,
                                startingBalance = rB.startingBalance,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

            // 4. Merge remote transactions locally
            val localTransactions = repository.transactionDao.getAllTransactionsSync()
            val localSignatureSet = localTransactions.map {
                "${it.timestamp}_${it.description}_${it.amount}_${it.category}"
            }.toSet()

            val newToInsert = mutableListOf<TransactionEntity>()
            for (rTx in remoteTransactions) {
                if (rTx.amount > 0L && rTx.description.isNotBlank() && rTx.timestamp > 0L) {
                    val sig = "${rTx.timestamp}_${rTx.description}_${rTx.amount}_${rTx.category}"
                    if (!localSignatureSet.contains(sig)) {
                        val ym = rTx.yearMonth.ifBlank { DateUtils.getYearMonthFromTimestamp(rTx.timestamp) }
                        newToInsert.add(
                            TransactionEntity(
                                id = 0L,
                                amount = rTx.amount,
                                description = rTx.description,
                                category = rTx.category,
                                type = rTx.type,
                                notes = rTx.notes,
                                timestamp = rTx.timestamp,
                                yearMonth = ym,
                                accountId = if (rTx.localAccountId > 0L) rTx.localAccountId else 1L
                            )
                        )
                    }
                }
            }
            if (newToInsert.isNotEmpty()) {
                repository.transactionDao.insertAll(newToInsert)
            }

            // 5. Upload all local items to remote to ensure full synchronization
            val allAccounts = repository.accountDao.getAllAccountsSync()
            val allTransactions = repository.transactionDao.getAllTransactionsSync()
            val allBudgets = repository.monthlyBudgetDao.getAllBudgetsSync()

            var accSynced = 0
            for (acc in allAccounts) {
                if (uploadAccount(acc).isSuccess) accSynced++
            }
            var txSynced = 0
            for (tx in allTransactions) {
                if (uploadTransaction(tx).isSuccess) txSynced++
            }
            var bgSynced = 0
            for (bg in allBudgets) {
                if (uploadBudget(bg).isSuccess) bgSynced++
            }

            Result.success(SyncSummary(accSynced, txSynced, bgSynced))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


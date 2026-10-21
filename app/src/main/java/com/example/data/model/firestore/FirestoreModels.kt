package com.example.data.model.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreAccount(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val iconEmoji: String = "👛",
    val colorHex: String = "#0F2537",
    val localId: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val amount: Long = 0L,
    val description: String = "",
    val category: String = "",
    val type: String = "EXPENSE",
    val notes: String = "",
    val timestamp: Long = 0L,
    val yearMonth: String = "",
    val localAccountId: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreMonthlyBudget(
    @DocumentId val id: String = "",
    val userId: String = "",
    val yearMonth: String = "",
    val targetBudget: Long = 0L,
    val startingBalance: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "accounts")
@JsonClass(generateAdapter = true)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconEmoji: String = "👛",
    val colorHex: String = "#0F2537",
    val createdAt: Long = System.currentTimeMillis()
)

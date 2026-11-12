package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val currency: String = "INR",
    val convertedAmountInr: Double,
    val category: String,
    val date: Long,
    val notes: String = "",
    val receiptUri: String? = null
)

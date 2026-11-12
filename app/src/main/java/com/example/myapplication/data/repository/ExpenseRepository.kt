package com.example.myapplication.data.repository

import com.example.myapplication.data.local.ExpenseDao
import com.example.myapplication.data.local.ExpenseEntity
import com.example.myapplication.data.remote.RetrofitClient
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    fun getAllExpenses(userId: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getAllExpenses(userId)
    }

    fun getExpensesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateRange(userId, startDate, endDate)
    }

    suspend fun addExpense(
        userId: String,
        title: String,
        amount: Double,
        currency: String,
        category: String,
        date: Long,
        notes: String,
        receiptUri: String?
    ): Long {
        val convertedAmountInr = RetrofitClient.convertToInr(amount, currency)
        val entity = ExpenseEntity(
            userId = userId,
            title = title,
            amount = amount,
            currency = currency,
            convertedAmountInr = convertedAmountInr,
            category = category,
            date = date,
            notes = notes,
            receiptUri = receiptUri
        )
        return expenseDao.insertExpense(entity)
    }

    suspend fun updateExpense(
        expense: ExpenseEntity,
        newTitle: String,
        newAmount: Double,
        newCurrency: String,
        newCategory: String,
        newDate: Long,
        newNotes: String,
        newReceiptUri: String?
    ) {
        val convertedAmountInr = RetrofitClient.convertToInr(newAmount, newCurrency)
        val updatedEntity = expense.copy(
            title = newTitle,
            amount = newAmount,
            currency = newCurrency,
            convertedAmountInr = convertedAmountInr,
            category = newCategory,
            date = newDate,
            notes = newNotes,
            receiptUri = newReceiptUri
        )
        expenseDao.updateExpense(updatedEntity)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}

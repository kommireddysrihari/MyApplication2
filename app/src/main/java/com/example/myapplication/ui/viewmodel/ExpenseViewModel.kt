package com.example.myapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.ExpenseEntity
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.repository.BudgetRepository
import com.example.myapplication.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class BudgetAlertLevel {
    NORMAL,
    WARNING_80,
    CRITICAL_100
}

data class CategorySummary(
    val categoryName: String,
    val totalAmountInr: Double,
    val percentage: Float
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val expenseRepository = ExpenseRepository(db.expenseDao())
    private val budgetRepository = BudgetRepository(application)

    private val _currentUserId = MutableStateFlow("demo_user_123")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setUserId(uid: String) {
        _currentUserId.value = uid
    }

    // Monthly Budget state from DataStore
    val monthlyBudget: StateFlow<Double> = budgetRepository.monthlyBudget.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 25000.0
    )

    // Current Month Range (Start and End of current month in millis)
    private val _selectedMonthCalendar = MutableStateFlow(Calendar.getInstance())
    val selectedMonthCalendar: StateFlow<Calendar> = _selectedMonthCalendar.asStateFlow()

    // Filters
    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // All expenses for current user
    val allExpenses: StateFlow<List<ExpenseEntity>> = combine(_currentUserId) { userIdArray ->
        userIdArray[0]
    }.combine(expenseRepository.getAllExpenses(_currentUserId.value)) { _, list ->
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered expenses based on selected month, category, and search query
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        allExpenses,
        _selectedMonthCalendar,
        _selectedCategoryFilter,
        _searchQuery
    ) { expenses, monthCal, categoryFilter, query ->
        val (startOfMonth, endOfMonth) = getMonthBounds(monthCal)
        expenses.filter { expense ->
            val matchesMonth = expense.date in startOfMonth..endOfMonth
            val matchesCategory = categoryFilter == "All" || expense.category.equals(categoryFilter, ignoreCase = true)
            val matchesQuery = query.isBlank() || expense.title.contains(query, ignoreCase = true) || expense.notes.contains(query, ignoreCase = true)
            matchesMonth && matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Total Spent in INR for selected month
    val totalSpentCurrentMonth: StateFlow<Double> = combine(
        allExpenses,
        _selectedMonthCalendar
    ) { expenses, monthCal ->
        val (startOfMonth, endOfMonth) = getMonthBounds(monthCal)
        expenses.filter { it.date in startOfMonth..endOfMonth }
            .sumOf { it.convertedAmountInr }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Category breakdown list
    val categorySummaries: StateFlow<List<CategorySummary>> = combine(
        allExpenses,
        _selectedMonthCalendar,
        totalSpentCurrentMonth
    ) { expenses, monthCal, totalSpent ->
        val (startOfMonth, endOfMonth) = getMonthBounds(monthCal)
        val monthExpenses = expenses.filter { it.date in startOfMonth..endOfMonth }

        if (totalSpent <= 0.0) {
            emptyList()
        } else {
            monthExpenses.groupBy { it.category }
                .map { (cat, catExpenses) ->
                    val catTotal = catExpenses.sumOf { it.convertedAmountInr }
                    CategorySummary(
                        categoryName = cat,
                        totalAmountInr = catTotal,
                        percentage = ((catTotal / totalSpent) * 100).toFloat()
                    )
                }
                .sortedByDescending { it.totalAmountInr }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Budget alert status
    val budgetAlertLevel: StateFlow<BudgetAlertLevel> = combine(
        totalSpentCurrentMonth,
        monthlyBudget
    ) { spent, budget ->
        val percentage = if (budget > 0) (spent / budget) * 100 else 0.0
        when {
            percentage >= 100.0 -> BudgetAlertLevel.CRITICAL_100
            percentage >= 80.0 -> BudgetAlertLevel.WARNING_80
            else -> BudgetAlertLevel.NORMAL
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetAlertLevel.NORMAL
    )

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun changeMonth(monthOffset: Int) {
        val newCal = (_selectedMonthCalendar.value.clone() as Calendar).apply {
            add(Calendar.MONTH, monthOffset)
        }
        _selectedMonthCalendar.value = newCal
    }

    fun setMonth(year: Int, month: Int) {
        val newCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _selectedMonthCalendar.value = newCal
    }

    fun addExpense(
        title: String,
        amount: Double,
        currency: String,
        category: String,
        date: Long,
        notes: String,
        receiptUri: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.addExpense(
                userId = _currentUserId.value,
                title = title,
                amount = amount,
                currency = currency,
                category = category,
                date = date,
                notes = notes,
                receiptUri = receiptUri
            )
            onComplete()
        }
    }

    fun updateExpense(
        expense: ExpenseEntity,
        title: String,
        amount: Double,
        currency: String,
        category: String,
        date: Long,
        notes: String,
        receiptUri: String?,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.updateExpense(
                expense = expense,
                newTitle = title,
                newAmount = amount,
                newCurrency = currency,
                newCategory = category,
                newDate = date,
                newNotes = notes,
                newReceiptUri = receiptUri
            )
            onComplete()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun updateMonthlyBudget(newBudget: Double) {
        viewModelScope.launch {
            budgetRepository.setMonthlyBudget(newBudget)
        }
    }

    fun convertCurrencyRealtime(amount: Double, currency: String, onResult: (Double) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val converted = RetrofitClient.convertToInr(amount, currency)
            onResult(converted)
        }
    }

    private fun getMonthBounds(calendar: Calendar): Pair<Long, Long> {
        val startCal = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }
}

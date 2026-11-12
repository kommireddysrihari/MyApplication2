package com.example.myapplication.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.local.ExpenseEntity
import com.example.myapplication.data.model.ExpenseCategory
import com.example.myapplication.ui.components.AddEditExpenseDialog
import com.example.myapplication.ui.components.BudgetAlertCard
import com.example.myapplication.ui.components.ExpenseCard
import com.example.myapplication.ui.theme.BackgroundWhite
import com.example.myapplication.ui.theme.BlueLight
import com.example.myapplication.ui.theme.BluePrimary
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.ExpenseViewModel

@Composable
fun HomeScreen(
    expenseViewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val monthlyBudget by expenseViewModel.monthlyBudget.collectAsState()
    val totalSpent by expenseViewModel.totalSpentCurrentMonth.collectAsState()
    val alertLevel by expenseViewModel.budgetAlertLevel.collectAsState()
    val filteredExpenses by expenseViewModel.filteredExpenses.collectAsState()
    val searchQuery by expenseViewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by expenseViewModel.selectedCategoryFilter.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showAddExpenseDialog = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // User Greeting Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello, ${currentUser?.displayName ?: "User"} 👋",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            text = "Here is your monthly expense dashboard",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser?.displayName?.take(1) ?: "U").uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }
            }

            // Monthly Budget & Alert Banner Card
            item {
                BudgetAlertCard(
                    monthlyBudget = monthlyBudget,
                    totalSpent = totalSpent,
                    alertLevel = alertLevel
                )
            }

            // Quick Category Filters
            item {
                Column {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            val isAllSelected = selectedCategoryFilter == "All"
                            Card(
                                modifier = Modifier.clickable { expenseViewModel.setCategoryFilter("All") },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isAllSelected) BluePrimary else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = "All",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAllSelected) Color.White else BluePrimary
                                )
                            }
                        }

                        items(ExpenseCategory.getAllCategories()) { cat ->
                            val isSelected = selectedCategoryFilter.equals(cat.title, ignoreCase = true)
                            Card(
                                modifier = Modifier.clickable { expenseViewModel.setCategoryFilter(cat.title) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) cat.color else Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = cat.icon,
                                        contentDescription = cat.title,
                                        tint = if (isSelected) Color.White else cat.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = cat.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { expenseViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search expenses by title...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = BluePrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Recent Expenses Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToExpenses) {
                        Text(text = "View All", color = BluePrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Expenses List Items
            if (filteredExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No expenses found.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(filteredExpenses.take(8)) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onEditClick = {
                            expenseToEdit = it
                            showAddExpenseDialog = true
                        },
                        onDeleteClick = {
                            expenseViewModel.deleteExpense(it)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }
    }

    // Add or Edit Expense Dialog
    if (showAddExpenseDialog) {
        AddEditExpenseDialog(
            expenseToEdit = expenseToEdit,
            onDismiss = { showAddExpenseDialog = false },
            onSave = { title, amount, currency, category, date, notes, receiptUri ->
                if (expenseToEdit == null) {
                    expenseViewModel.addExpense(title, amount, currency, category, date, notes, receiptUri) {
                        showAddExpenseDialog = false
                    }
                } else {
                    expenseViewModel.updateExpense(expenseToEdit!!, title, amount, currency, category, date, notes, receiptUri) {
                        showAddExpenseDialog = false
                    }
                }
            },
            onConvertCurrency = { amount, currency, callback ->
                expenseViewModel.convertCurrencyRealtime(amount, currency, callback)
            }
        )
    }
}

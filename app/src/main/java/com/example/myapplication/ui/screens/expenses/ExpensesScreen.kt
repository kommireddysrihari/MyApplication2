package com.example.myapplication.ui.screens.expenses

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.local.ExpenseEntity
import com.example.myapplication.data.model.ExpenseCategory
import com.example.myapplication.ui.components.AddEditExpenseDialog
import com.example.myapplication.ui.components.ExpenseCard
import com.example.myapplication.ui.theme.BackgroundWhite
import com.example.myapplication.ui.theme.BluePrimary
import com.example.myapplication.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExpensesScreen(
    expenseViewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val filteredExpenses by expenseViewModel.filteredExpenses.collectAsState()
    val searchQuery by expenseViewModel.searchQuery.collectAsState()
    val selectedCategoryFilter by expenseViewModel.selectedCategoryFilter.collectAsState()
    val monthCal by expenseViewModel.selectedMonthCalendar.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }
    var viewingReceiptUri by remember { mutableStateOf<String?>(null) }

    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    expenseToEdit = null
                    showAddDialog = true
                },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Screen Header
            Text(
                text = "Expense History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Month Switcher Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { expenseViewModel.changeMonth(-1) }) {
                        Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = BluePrimary)
                    }

                    Text(
                        text = monthFormatter.format(monthCal.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = { expenseViewModel.changeMonth(1) }) {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = BluePrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { expenseViewModel.setSearchQuery(it) },
                placeholder = { Text("Search transactions...") },
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

            Spacer(modifier = Modifier.height(14.dp))

            // Category Filter Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAll = selectedCategoryFilter == "All"
                    Card(
                        modifier = Modifier.clickable { expenseViewModel.setCategoryFilter("All") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isAll) BluePrimary else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "All",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAll) Color.White else BluePrimary
                        )
                    }
                }

                items(ExpenseCategory.getAllCategories()) { cat ->
                    val isSelected = selectedCategoryFilter.equals(cat.title, ignoreCase = true)
                    Card(
                        modifier = Modifier.clickable { expenseViewModel.setCategoryFilter(cat.title) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) cat.color else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = cat.title,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expense List
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No expenses recorded for this month.", color = Color.Gray, fontSize = 15.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredExpenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEditClick = {
                                expenseToEdit = it
                                showAddDialog = true
                            },
                            onDeleteClick = {
                                expenseViewModel.deleteExpense(it)
                            },
                            onCardClick = { clicked ->
                                if (!clicked.receiptUri.isNullOrEmpty()) {
                                    viewingReceiptUri = clicked.receiptUri
                                }
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(70.dp)) }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        AddEditExpenseDialog(
            expenseToEdit = expenseToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, currency, category, date, notes, receiptUri ->
                if (expenseToEdit == null) {
                    expenseViewModel.addExpense(title, amount, currency, category, date, notes, receiptUri) {
                        showAddDialog = false
                    }
                } else {
                    expenseViewModel.updateExpense(expenseToEdit!!, title, amount, currency, category, date, notes, receiptUri) {
                        showAddDialog = false
                    }
                }
            },
            onConvertCurrency = { amount, currency, callback ->
                expenseViewModel.convertCurrencyRealtime(amount, currency, callback)
            }
        )
    }

    // Receipt Image Dialog
    if (viewingReceiptUri != null) {
        Dialog(onDismissRequest = { viewingReceiptUri = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Scanned Receipt Image", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewingReceiptUri = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Image(
                        painter = rememberAsyncImagePainter(viewingReceiptUri),
                        contentDescription = "Receipt Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(Color.Black),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

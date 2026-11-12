package com.example.myapplication.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ExpenseCategory
import com.example.myapplication.ui.components.CategoryPieChart
import com.example.myapplication.ui.theme.BackgroundWhite
import com.example.myapplication.ui.theme.BlueLight
import com.example.myapplication.ui.theme.BluePrimary
import com.example.myapplication.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ReportsScreen(
    expenseViewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val monthCal by expenseViewModel.selectedMonthCalendar.collectAsState()
    val totalSpent by expenseViewModel.totalSpentCurrentMonth.collectAsState()
    val categorySummaries by expenseViewModel.categorySummaries.collectAsState()
    val filteredExpenses by expenseViewModel.filteredExpenses.collectAsState()

    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val highestExpense = filteredExpenses.maxByOrNull { it.convertedAmountInr }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Spending Reports",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BluePrimary
        )
        Text(
            text = "Category breakdown & monthly analytics",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Month Navigation Card
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
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev", tint = BluePrimary)
                }

                Text(
                    text = monthFormatter.format(monthCal.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { expenseViewModel.changeMonth(1) }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = BluePrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Monthly Spend Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BluePrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Monthly Spending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", totalSpent),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.PieChart, contentDescription = "Chart", tint = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Donut / Pie Chart Component
        CategoryPieChart(categorySummaries = categorySummaries)

        Spacer(modifier = Modifier.height(20.dp))

        // Category Progress Bars Breakdown
        if (categorySummaries.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Category Progress Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    categorySummaries.forEach { summary ->
                        val cat = ExpenseCategory.fromString(summary.categoryName)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = cat.icon, contentDescription = cat.title, tint = cat.color, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = cat.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "₹ %,.2f (%.1f%%)", summary.totalAmountInr, summary.percentage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = cat.color
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { summary.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = cat.color,
                                trackColor = cat.color.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Highest Expense Card
        if (highestExpense != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Highest", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Highest Transaction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = highestExpense.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }

                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", highestExpense.convertedAmountInr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}

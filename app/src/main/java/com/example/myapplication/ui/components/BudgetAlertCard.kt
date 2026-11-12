package com.example.myapplication.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AlertCriticalRed
import com.example.myapplication.ui.theme.AlertNormalGreen
import com.example.myapplication.ui.theme.AlertWarningOrange
import com.example.myapplication.ui.theme.BluePrimary
import com.example.myapplication.ui.viewmodel.BudgetAlertLevel
import java.util.Locale

@Composable
fun BudgetAlertCard(
    monthlyBudget: Double,
    totalSpent: Double,
    alertLevel: BudgetAlertLevel,
    modifier: Modifier = Modifier
) {
    val remaining = monthlyBudget - totalSpent
    val percentage = if (monthlyBudget > 0) (totalSpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
    val percentageVal = if (monthlyBudget > 0) (totalSpent / monthlyBudget * 100) else 0.0

    val progressColor = when (alertLevel) {
        BudgetAlertLevel.NORMAL -> AlertNormalGreen
        BudgetAlertLevel.WARNING_80 -> AlertWarningOrange
        BudgetAlertLevel.CRITICAL_100 -> AlertCriticalRed
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "₹ %,.2f", monthlyBudget),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = Color.White.copy(alpha = 0.25f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Spent & Remaining Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", totalSpent),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (remaining >= 0) "Remaining" else "Over Budget",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹ %,.2f", Math.abs(remaining)),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (remaining < 0) Color(0xFFFF8A80) else Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Budget Alert Banners
            if (alertLevel != BudgetAlertLevel.NORMAL) {
                Spacer(modifier = Modifier.height(16.dp))

                val alertBg = if (alertLevel == BudgetAlertLevel.CRITICAL_100) AlertCriticalRed else AlertWarningOrange
                val alertIcon = if (alertLevel == BudgetAlertLevel.CRITICAL_100) Icons.Default.Error else Icons.Default.Warning
                val alertText = if (alertLevel == BudgetAlertLevel.CRITICAL_100) {
                    String.format(Locale.getDefault(), "🚨 Critical Alert: You have reached %.1f%% of your budget!", percentageVal)
                } else {
                    String.format(Locale.getDefault(), "⚠️ Warning: You have spent %.1f%% of your monthly budget!", percentageVal)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(alertBg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = alertIcon,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = alertText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

package com.example.myapplication.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food", Icons.Default.Fastfood, Color(0xFFEF5350)),
    TRAVEL("Travel", Icons.Default.DirectionsCar, Color(0xFF42A5F5)),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, Color(0xFFAB47BC)),
    EDUCATION("Education", Icons.Default.Book, Color(0xFFFFA726)),
    HEALTH("Health", Icons.Default.MedicalServices, Color(0xFF26A69A)),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, Color(0xFFEC407A)),
    OTHER("Other", Icons.Default.Category, Color(0xFF78909C));

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.find { it.title.equals(name, ignoreCase = true) } ?: OTHER
        }

        fun getAllCategories(): List<ExpenseCategory> = entries.toList()
    }
}

package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.ReceiptLong)
    object Reports : Screen("reports", "Reports", Icons.Default.PieChart)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)

    companion object {
        val bottomNavScreens = listOf(Home, Expenses, Reports, Profile)
    }
}

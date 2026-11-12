package com.example.myapplication.data.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_budget_prefs")

class BudgetRepository(private val context: Context) {

    private val BUDGET_KEY = doublePreferencesKey("monthly_budget_inr")

    val monthlyBudget: Flow<Double> = context.dataStore.data.map { preferences ->
        preferences[BUDGET_KEY] ?: 25000.0 // Default budget ₹ 25,000 INR
    }

    suspend fun setMonthlyBudget(newBudget: Double) {
        context.dataStore.edit { preferences ->
            preferences[BUDGET_KEY] = newBudget
        }
    }
}

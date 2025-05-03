package com.example.financetracker.utils

import android.content.Context
import com.example.financetracker.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefHelper {
    private const val PREF_NAME = "finance_prefs"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_BUDGET = "budget"

    fun saveTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(transactions)
        editor.putString(KEY_TRANSACTIONS, json)
        editor.apply()
    }

    fun loadTransactions(context: Context): MutableList<Transaction> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_TRANSACTIONS, null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
    fun saveBudget(context: Context, budget: Double) {
        val prefs = context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        prefs.edit().putFloat("budget", budget.toFloat()).apply()
    }

    fun getBudget(context: Context): Double {
        val prefs = context.getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        return prefs.getFloat("budget", 0f).toDouble()
    }

}

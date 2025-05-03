package com.example.financetracker.models

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String,
    val isExpense: Boolean = true,
    val paymentMethod: String? = null,
    val notes: String? = null
) : java.io.Serializable
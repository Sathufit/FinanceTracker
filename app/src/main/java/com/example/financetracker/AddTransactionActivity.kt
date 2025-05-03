package com.example.financetracker

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.financetracker.models.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var editTitle: EditText
    private lateinit var editAmount: EditText
    private lateinit var editNotes: EditText
    private lateinit var btnBack: ImageButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnExpense: MaterialButton
    private lateinit var btnIncome: MaterialButton
    private lateinit var categorySelector: LinearLayout
    private lateinit var textSelectedCategory: TextView
    private lateinit var dateSelector: LinearLayout
    private lateinit var textSelectedDate: TextView
    private lateinit var paymentMethodSelector: LinearLayout
    private lateinit var textSelectedPaymentMethod: TextView
    private lateinit var categoryIcon: ImageView

    private var isExpense = true
    private var selectedCategory = "Food & Groceries"
    private var selectedPaymentMethod = "Cash"
    private var selectedDate = ""
    private val calendar = Calendar.getInstance()

    private var isEditMode = false
    private var editTransaction: Transaction? = null
    private var editPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // Initialize views
        editTitle = findViewById(R.id.editTitle)
        editAmount = findViewById(R.id.editAmount)
        editNotes = findViewById(R.id.editNotes)
        btnBack = findViewById(R.id.btnBack)
        btnSave = findViewById(R.id.btnSave)
        btnExpense = findViewById(R.id.btnExpense)
        btnIncome = findViewById(R.id.btnIncome)
        categorySelector = findViewById(R.id.categorySelector)
        textSelectedCategory = findViewById(R.id.textSelectedCategory)
        dateSelector = findViewById(R.id.dateSelector)
        textSelectedDate = findViewById(R.id.textSelectedDate)
        paymentMethodSelector = findViewById(R.id.paymentMethodSelector)
        textSelectedPaymentMethod = findViewById(R.id.textSelectedPaymentMethod)
        categoryIcon = findViewById(R.id.categoryIcon)

        // Set today's date by default
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        textSelectedDate.text = "Today, $selectedDate"

        // Back button
        btnBack.setOnClickListener { finish() }

        // Expense/Income toggle
        btnExpense.setOnClickListener {
            isExpense = true
            updateTransactionTypeUI()
        }

        btnIncome.setOnClickListener {
            isExpense = false
            updateTransactionTypeUI()
        }

        // Category selection
        categorySelector.setOnClickListener {
            showCategoryDialog()
        }

        // Date selection
        dateSelector.setOnClickListener {
            showDatePicker()
        }

        // Payment method selection
        paymentMethodSelector.setOnClickListener {
            showPaymentMethodDialog()
        }

        // Check if we are editing
        editTransaction = intent.getSerializableExtra("edit_transaction") as? Transaction
        editPosition = intent.getIntExtra("edit_position", -1)

        if (editTransaction != null && editPosition != -1) {
            isEditMode = true

            // Pre-fill data
            editTitle.setText(editTransaction!!.title)
            editAmount.setText(editTransaction!!.amount.toString())
            editNotes.setText(editTransaction!!.notes ?: "")

            isExpense = editTransaction!!.isExpense
            selectedCategory = editTransaction!!.category
            selectedDate = editTransaction!!.date
            selectedPaymentMethod = editTransaction!!.paymentMethod ?: "Cash"

            // Update UI
            updateTransactionTypeUI()
            textSelectedCategory.text = selectedCategory
            textSelectedDate.text = selectedDate
            textSelectedPaymentMethod.text = selectedPaymentMethod
            updateCategoryIcon()
        }

        // Save button
        btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTransactionTypeUI() {
        if (isExpense) {
            btnExpense.backgroundTintList = resources.getColorStateList(R.color.expenseColor, theme)
            btnExpense.setTextColor(resources.getColor(R.color.white, theme))

            btnIncome.backgroundTintList = resources.getColorStateList(R.color.cardBackground, theme)
            btnIncome.setTextColor(resources.getColor(R.color.textPrimary, theme))
        } else {
            btnIncome.backgroundTintList = resources.getColorStateList(R.color.incomeColor, theme)
            btnIncome.setTextColor(resources.getColor(R.color.white, theme))

            btnExpense.backgroundTintList = resources.getColorStateList(R.color.cardBackground, theme)
            btnExpense.setTextColor(resources.getColor(R.color.textPrimary, theme))
        }
    }

    private fun showCategoryDialog() {
        val categories = if (isExpense) {
            arrayOf("Food & Groceries", "Transport", "Entertainment", "Shopping", "Bills", "Health", "Education", "Other")
        } else {
            arrayOf("Salary", "Investment", "Refund", "Gift", "Other Income")
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                selectedCategory = categories[which]
                textSelectedCategory.text = selectedCategory
                updateCategoryIcon()
            }
            .create()

        dialog.show()
    }

    private fun updateCategoryIcon() {
        val iconResId = when (selectedCategory.lowercase()) {
            "food & groceries", "groceries" -> R.drawable.ic_shopping
            "transport", "transportation" -> R.drawable.ic_transport
            "bills", "utilities" -> R.drawable.ic_notification
            "entertainment" -> R.drawable.ic_entertainment
            "shopping" -> R.drawable.ic_shopping
            "health", "healthcare" -> R.drawable.ic_health
            "education" -> R.drawable.ic_education
            "salary", "investment", "refund", "gift", "other income" -> R.drawable.ic_import
            else -> R.drawable.ic_export
        }

        categoryIcon.setImageResource(iconResId)
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)

                // Check if it's today
                val today = Calendar.getInstance()
                if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                    textSelectedDate.text = "Today, $selectedDate"
                } else {
                    textSelectedDate.text = selectedDate
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun showPaymentMethodDialog() {
        val paymentMethods = if (isExpense) {
            arrayOf("Cash", "Debit Card", "Credit Card", "UPI", "Bank Transfer", "Other")
        } else {
            arrayOf("Cash", "Bank Transfer", "Paypal", "Check", "Other")
        }

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Select Payment Method")
            .setItems(paymentMethods) { _, which ->
                selectedPaymentMethod = paymentMethods[which]
                textSelectedPaymentMethod.text = selectedPaymentMethod
            }
            .create()

        dialog.show()
    }

    private fun saveTransaction() {
        val title = editTitle.text.toString().trim()
        val amountText = editAmount.text.toString().trim()
        val notes = editNotes.text.toString().trim()

        // Validation
        if (title.isBlank()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Create transaction object
        val transaction = Transaction(
            id = editTransaction?.id ?: System.currentTimeMillis(),
            title = title,
            amount = amount,
            category = selectedCategory,
            date = selectedDate,
            isExpense = isExpense,
            paymentMethod = selectedPaymentMethod,
            notes = if (notes.isBlank()) null else notes
        )

        // Return result
        val resultIntent = Intent()
        if (isEditMode) {
            resultIntent.putExtra("edited_transaction", transaction)
            resultIntent.putExtra("edit_position", editPosition)
        } else {
            resultIntent.putExtra("new_transaction", transaction)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
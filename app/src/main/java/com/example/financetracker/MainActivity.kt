package com.example.financetracker

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.adapters.TransactionAdapter
import com.example.financetracker.models.Transaction
import com.example.financetracker.utils.NotificationUtils
import com.example.financetracker.utils.SharedPrefHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnSetBudget: ImageButton
    private lateinit var btnViewChart: ImageButton
    private lateinit var btnExport: ImageButton
    private lateinit var btnImport: ImageButton
    private lateinit var textIncome: TextView
    private lateinit var textExpense: TextView
    private lateinit var textBudget: TextView

    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private var budget = 0.0

    private val addTransactionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val newTransaction = data.getSerializableExtra("new_transaction") as? Transaction
            if (newTransaction != null) {
                transactions.add(0, newTransaction)
                SharedPrefHelper.saveTransactions(this, transactions)
                transactionAdapter.notifyItemInserted(0)
                updateSummary()
                return@registerForActivityResult
            }

            val editedTransaction = data.getSerializableExtra("edited_transaction") as? Transaction
            val editPosition = data.getIntExtra("edit_position", -1)
            if (editedTransaction != null && editPosition >= 0) {
                transactions[editPosition] = editedTransaction
                SharedPrefHelper.saveTransactions(this, transactions)
                transactionAdapter.notifyItemChanged(editPosition)
                updateSummary()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestNotificationPermission()

        transactionRecyclerView = findViewById(R.id.transactionRecyclerView)
        fabAdd = findViewById(R.id.fabAdd)
        btnSetBudget = findViewById(R.id.btnSetBudget)
        btnViewChart = findViewById(R.id.btnViewChart)
        btnExport = findViewById(R.id.btnExport)
        btnImport = findViewById(R.id.btnImport)
        textIncome = findViewById(R.id.textIncome)
        textExpense = findViewById(R.id.textExpense)
        textBudget = findViewById(R.id.textBudget)

        transactionAdapter = TransactionAdapter(
            transactions = transactions,
            onEditClick = { transaction, position -> openEditTransaction(transaction, position) },
            onDeleteClick = { _, position -> confirmDelete(position) }
        )

        transactionRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = transactionAdapter
        }

        fabAdd.setOnClickListener { openAddTransaction() }
        btnSetBudget.setOnClickListener { showBudgetDialog() }
        btnViewChart.setOnClickListener {
            val intent = Intent(this, ChartActivity::class.java)
            startActivity(intent)
        }

        btnExport.setOnClickListener { exportTransactionsToInternalStorage() }
        btnImport.setOnClickListener { importTransactionsFromInternalStorage() }

        loadSavedData()
        updateSummary()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
    }

    private fun loadSavedData() {
        // Load from shared preferences
        transactions.clear()
        transactions.addAll(SharedPrefHelper.loadTransactions(this))

        budget = SharedPrefHelper.getBudget(this)
    }

    private fun updateSummary() {
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (transaction in transactions) {
            if (transaction.isExpense) totalExpense += transaction.amount
            else totalIncome += transaction.amount
        }

        textIncome.text = "$%.2f".format(totalIncome)
        textExpense.text = "$%.2f".format(totalExpense)
        textBudget.text = "$%.2f".format(budget)

        if (totalExpense > budget) {
            NotificationUtils.showBudgetExceededNotification(this)
        }
    }

    private fun openAddTransaction() {
        val intent = Intent(this, AddTransactionActivity::class.java)
        addTransactionLauncher.launch(intent)
    }

    private fun openEditTransaction(transaction: Transaction, position: Int) {
        val intent = Intent(this, AddTransactionActivity::class.java)
        intent.putExtra("edit_transaction", transaction)
        intent.putExtra("edit_position", position)
        addTransactionLauncher.launch(intent)
    }

    private fun confirmDelete(position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete this transaction?")
            .setPositiveButton("Delete") { _, _ ->
                transactions.removeAt(position)
                SharedPrefHelper.saveTransactions(this, transactions)
                transactionAdapter.notifyItemRemoved(position)
                updateSummary()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBudgetDialog() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(budget.toString())
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Set Monthly Budget")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newBudget = input.text.toString().toDoubleOrNull()
                if (newBudget != null) {
                    budget = newBudget
                    SharedPrefHelper.saveBudget(this, budget)
                    textBudget.text = "$%.2f".format(budget)
                    updateSummary()
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNotImplementedToast() {
        Toast.makeText(this, "This feature is not implemented yet", Toast.LENGTH_SHORT).show()
    }
    private fun exportTransactionsToInternalStorage() {
        val transactions = SharedPrefHelper.loadTransactions(this)
        val jsonString = Gson().toJson(transactions) // Convert to JSON

        try {
            val fileName = "finance_backup.json"
            val fileOutputStream = openFileOutput(fileName, MODE_PRIVATE)
            fileOutputStream.write(jsonString.toByteArray())
            fileOutputStream.close()
            Toast.makeText(this, "Backup saved to internal storage", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to export data", Toast.LENGTH_SHORT).show()
        }
    }
    private fun importTransactionsFromInternalStorage() {
        try {
            val fileName = "finance_backup.json"
            val fileInputStream = openFileInput(fileName)
            val jsonString = fileInputStream.bufferedReader().use { it.readText() }
            fileInputStream.close()

            val transactionList = Gson().fromJson(jsonString, Array<Transaction>::class.java).toList()

            SharedPrefHelper.saveTransactions(this, transactionList.toMutableList())
            transactions.clear()
            transactions.addAll(transactionList)
            transactionAdapter.notifyDataSetChanged()
            updateSummary()

            Toast.makeText(this, "Data restored successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to import data", Toast.LENGTH_SHORT).show()
        }
    }


}

package com.example.financetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.utils.SharedPrefHelper

class BudgetActivity : AppCompatActivity() {

    private lateinit var editBudget: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        editBudget = findViewById(R.id.editBudget)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnBack = findViewById(R.id.btnBack)

        // 🔙 Back button click
        btnBack.setOnClickListener {
            finish()
        }

        // Load current budget if set
        val currentBudget = SharedPrefHelper.getBudget(this)
        if (currentBudget > 0) {
            editBudget.setText(currentBudget.toString())
        }

        // Save new budget
        btnSaveBudget.setOnClickListener {
            val amount = editBudget.text.toString().toDoubleOrNull()
            if (amount != null && amount > 0) {
                SharedPrefHelper.saveBudget(this, amount)
                Toast.makeText(this, "Budget saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

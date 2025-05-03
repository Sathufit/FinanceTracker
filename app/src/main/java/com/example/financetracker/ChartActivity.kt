package com.example.financetracker

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.models.Transaction
import com.example.financetracker.utils.SharedPrefHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis



class ChartActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        // 🔧 Set up toolbar with back arrow
        topAppBar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            finish() // Go back to MainActivity
        }

        pieChart = findViewById(R.id.pieChart)

        val transactions = SharedPrefHelper.loadTransactions(this)

        // Group non-income categories
        val categoryTotals = HashMap<String, Float>()
        for (transaction in transactions) {
            if (!transaction.category.equals("income", ignoreCase = true)) {
                val total = categoryTotals[transaction.category] ?: 0f
                categoryTotals[transaction.category] = total + transaction.amount.toFloat()
            }
        }

        val entries = categoryTotals.map { PieEntry(it.value, it.key) }

        if (entries.isEmpty()) {
            Toast.makeText(this, "No expense data to display", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val dataSet = PieDataSet(entries, "Expenses by Category").apply {
            setColors(*ColorTemplate.COLORFUL_COLORS)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        pieChart.apply {
            data = PieData(dataSet)
            setUsePercentValues(true)
            centerText = "Spending Breakdown"
            animateY(1000)
            description = Description().apply { text = "" }
            invalidate()
        }
        val barChart = findViewById<BarChart>(R.id.barChart)

// Calculate totals
        var totalIncome = 0f
        var totalExpense = 0f

        for (transaction in transactions) {
            val amount = transaction.amount.toFloat()

            if (transaction.isExpense) {
                totalExpense += amount
            } else {
                totalIncome += amount
            }
        }

        val barEntries = listOf(
            BarEntry(0f, totalIncome),
            BarEntry(1f, totalExpense)
        )

        val barDataSet = BarDataSet(barEntries, "Income vs Expense").apply {
            setColors(ColorTemplate.MATERIAL_COLORS, 255)
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(barDataSet)
        barChart.data = barData

// Set custom x-axis labels
        val labels = listOf("Income", "Expense")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.text = ""
        barChart.animateY(1000)
        barChart.invalidate()

    }
}

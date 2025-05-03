package com.example.financetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.R
import com.example.financetracker.models.Transaction
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TransactionAdapter(
    private val transactions: MutableList<Transaction>,
    private val onEditClick: (Transaction, Int) -> Unit,
    private val onDeleteClick: (Transaction, Int) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.textTitle)
        val amountText: TextView = itemView.findViewById(R.id.textAmount)
        val categoryText: TextView = itemView.findViewById(R.id.textCategory)
        val dateText: TextView = itemView.findViewById(R.id.textDate)
        val paymentMethodText: TextView = itemView.findViewById(R.id.textPaymentMethod)
        val categoryIcon: ImageView = itemView.findViewById(R.id.categoryIcon)
        val categoryIconContainer: MaterialCardView = itemView.findViewById(R.id.categoryIconContainer)
        val actionButtonsContainer: LinearLayout = itemView.findViewById(R.id.actionButtonsContainer)
        val buttonEdit: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)

        init {
            itemView.setOnLongClickListener {
                // Show action buttons
                if (actionButtonsContainer.visibility == View.GONE) {
                    actionButtonsContainer.visibility = View.VISIBLE
                } else {
                    actionButtonsContainer.visibility = View.GONE
                }
                true
            }

            buttonEdit.setOnClickListener {
                onEditClick(transactions[adapterPosition], adapterPosition)
                actionButtonsContainer.visibility = View.GONE
            }

            buttonDelete.setOnClickListener {
                onDeleteClick(transactions[adapterPosition], adapterPosition)
                actionButtonsContainer.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        // Set basic data
        holder.titleText.text = transaction.title
        holder.categoryText.text = transaction.category
        holder.dateText.text = transaction.date

        // Set amount with color based on expense or income
        val context = holder.itemView.context
        if (transaction.isExpense) {
            holder.amountText.text = "$%.2f".format(transaction.amount)
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.expenseColor))
        } else {
            holder.amountText.text = "$%.2f".format(transaction.amount)
            holder.amountText.setTextColor(ContextCompat.getColor(context, R.color.incomeColor))
        }

        // Set payment method if available
        if (transaction.paymentMethod.isNullOrBlank()) {
            holder.paymentMethodText.visibility = View.GONE
        } else {
            holder.paymentMethodText.visibility = View.VISIBLE
            holder.paymentMethodText.text = transaction.paymentMethod
        }

        // Set appropriate icon based on category
        val iconResId = getCategoryIcon(transaction.category)
        holder.categoryIcon.setImageResource(iconResId)

        // Set icon container color based on expense or income
        if (transaction.isExpense) {
            holder.categoryIconContainer.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.secondaryLightColor)
            )
        } else {
            holder.categoryIconContainer.setCardBackgroundColor(
                ContextCompat.getColor(context, R.color.incomeColor)
            )
        }

        // Reset action buttons visibility
        holder.actionButtonsContainer.visibility = View.GONE
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>) {
        transactions.clear()
        transactions.addAll(newTransactions)
        notifyDataSetChanged()
    }

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction)
        notifyItemInserted(0)
    }

    fun updateTransaction(transaction: Transaction, position: Int) {
        transactions[position] = transaction
        notifyItemChanged(position)
    }

    fun removeTransaction(position: Int) {
        transactions.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun getCategoryIcon(category: String): Int {
        return when (category.lowercase()) {
            "food", "food & groceries", "groceries" -> R.drawable.ic_shopping
            "transport", "transportation" -> R.drawable.ic_transport
            "bills", "utilities" -> R.drawable.ic_notification

            "entertainment" -> R.drawable.ic_entertainment
            "shopping" -> R.drawable.ic_shopping
            "health", "healthcare" -> R.drawable.ic_health
            "education" -> R.drawable.ic_education
            "income", "salary" -> R.drawable.ic_import
            else -> R.drawable.ic_export
        }
    }
}
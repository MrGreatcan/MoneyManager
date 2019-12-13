package com.greatcan.moneysaver.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greatcan.moneysaver.CategoryEnum
import com.greatcan.moneysaver.R
import com.greatcan.moneysaver.models.ExpensesModels

class ExpenseAdapter(
        var expensesList: ArrayList<ExpensesModels>
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private val TAG: String = "CategoryAdapter"

    override fun getItemCount(): Int = expensesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.layout_expense, parent, false)
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model: ExpensesModels = expensesList[position]

        Log.d(TAG, "onBindViewHolder: starting ")
        holder.ivCategoryIcon.setImageResource(getIcon(model.category))
        holder.tvCategory.text = model.category
        holder.tvDate.text = model.data
        holder.tvAmount.text = "- $" + model.amount
    }

    private fun getIcon(name: String): Int {
        var b: CategoryEnum = CategoryEnum.valueOf(name)
        return b.resources
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parentLayout: RelativeLayout = itemView.findViewById(R.id.parentLayout)
        val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

}
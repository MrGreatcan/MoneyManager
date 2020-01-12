package com.greatcan.moneysaver.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.greatcan.moneysaver.R
import com.greatcan.moneysaver.configuration.CategoryEnum
import com.greatcan.moneysaver.dialogs.ViewExpenseDialog
import com.greatcan.moneysaver.models.FinanceModel

class ExpenseAdapter(
        var expensesList: ArrayList<FinanceModel>,
        var fragmentActivity: FragmentActivity
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    private val TAG: String = "CategoryAdapter"

    override fun getItemCount(): Int = expensesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.layout_expense, parent, false)
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model: FinanceModel = expensesList[position]

        Log.d(TAG, "onBindViewHolder: starting ")
        holder.ivCategoryIcon.setImageResource(getIcon(model.category))
        holder.tvCategory.text = model.category
        holder.tvDate.text = model.date
        holder.tvAmount.text = "- $" + model.amount

        holder.parentLayout.setOnClickListener {
            if (model.note != "" && model.note != model.category){
                val viewExpenseDialog = ViewExpenseDialog(model.note)
                viewExpenseDialog.show(fragmentActivity.supportFragmentManager, "View expense")
            }
        }
    }

    private fun getIcon(name: String): Int {
        var b: CategoryEnum = CategoryEnum.valueOf(name)
        return b.resource
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val parentLayout: RelativeLayout = itemView.findViewById(R.id.parentLayout)
        val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
    }

}
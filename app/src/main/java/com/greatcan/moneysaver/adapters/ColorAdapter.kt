package com.greatcan.moneysaver.adapters

import android.app.PendingIntent.getActivity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.greatcan.moneysaver.MainMenuActivity
import com.greatcan.moneysaver.R
import com.greatcan.moneysaver.configuration.CategoryEnum
import com.greatcan.moneysaver.dialogs.ConfirmAddingDialog
import com.greatcan.moneysaver.dialogs.ViewExpenseDialog
import com.greatcan.moneysaver.models.ColorModel
import com.greatcan.moneysaver.models.FinanceModel

class ColorAdapter(
        var expensesList: ArrayList<ColorModel>
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private val TAG: String = "ColorAdapter"

    override fun getItemCount(): Int = expensesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.layout_expense_color, parent, false)
        return ViewHolder(view!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model: ColorModel = expensesList[position]

        Log.d(TAG, "onBindViewHolder: starting ")
        holder.vColor.setBackgroundColor(model.bg_color)
        holder.tvColorName.text = model.name_color
    }

    private fun getIcon(name: String): Int {
        var b: CategoryEnum = CategoryEnum.valueOf(name)
        return b.resource
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vColor: View = itemView.findViewById(R.id.vColor)
        val tvColorName: TextView = itemView.findViewById(R.id.tvColorName)
    }

}
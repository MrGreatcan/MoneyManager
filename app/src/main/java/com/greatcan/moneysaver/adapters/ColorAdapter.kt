package com.greatcan.moneysaver.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.greatcan.moneysaver.R
import com.greatcan.moneysaver.configuration.CategoryEnum
import com.greatcan.moneysaver.models.ColorModel

class ColorAdapter(
        var context: Context,
        var expensesList: ArrayList<ColorModel>
) : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private val TAG: String = "ColorAdapter"

    override fun getItemCount(): Int = expensesList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.layout_expense_color, parent, false)
        return ViewHolder(view!!)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var model: ColorModel = expensesList[position]

        Log.d(TAG, "onBindViewHolder: starting ")
        holder.vColor.setBackgroundColor(model.bg_color)
        holder.tvColorName.text = getTitle(model.name_color) + "(${model.percent})"
    }

    private fun getTitle(current: String): String {
        for (item in CategoryEnum.values()) {
            val title = context.resources.getString(item.title)
            if (item.name.contains(current)) {
                return title
            }
        }
        return ""
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val vColor: View = itemView.findViewById(R.id.vColor)
        val tvColorName: TextView = itemView.findViewById(R.id.tvColorName)
    }

}
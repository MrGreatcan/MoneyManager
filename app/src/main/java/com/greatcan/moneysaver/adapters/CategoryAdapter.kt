package com.greatcan.moneysaver.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.greatcan.moneysaver.R
import com.greatcan.moneysaver.interfaces.OnCategoryCallback
import com.greatcan.moneysaver.models.CategoryModel
import kotlinx.android.synthetic.main.layout_category.view.*

class CategoryAdapter(
        var categoryList: ArrayList<CategoryModel>,
        var context: Context,
        var callbackOn: OnCategoryCallback
) : BaseAdapter() {

    private val TAG: String = "CategoryAdapter"

    override fun getCount(): Int = categoryList.size

    override fun getItem(p0: Int): Any {
        return categoryList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val category = this.categoryList[p0]

        var inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var view = inflator.inflate(R.layout.layout_category, null)
        view.ivIcon.setImageResource(category.icon_of_category)
        view.tvTextCategory.text = context.resources.getString(category.name_of_category)

        view.parentLayout.setOnClickListener {
            Log.d(TAG, "getView: clicked on ${category.name_of_category}")

            if (callbackOn != null){
                callbackOn.onItemClicked(
                        text = context.resources.getString(category.name_of_category),
                        icon = category.icon_of_category)
            }
        }
        return view
    }
}
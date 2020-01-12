package com.greatcan.moneysaver.configuration.date

import java.text.SimpleDateFormat
import java.util.*

class CurrentDate {

    companion object {
        @JvmStatic
        fun getCurrentDate(): String {
            val dateFormat = SimpleDateFormat("MM.yyyy")
            val date = Date()
            return dateFormat.format(date)
        }
    }
}